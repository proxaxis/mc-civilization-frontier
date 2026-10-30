package com.civilizationfrontier.bcn.client.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Minimal HTTP control endpoint embedded in the client.
 *
 * POST /call {"tool": "...", "args": {...}} -> {"ok": true, "result": ...}
 * GET  /ping -> {"ok": true}
 *
 * Port: system property bcn.mcp.port (default 25580).
 * Optional shared token: -Dbcn.mcp.token=... required as "Authorization: Bearer ...".
 */
public final class McpHttpd {
    private static final Logger LOG = LoggerFactory.getLogger("bcn-mcp");
    private static final int MAX_BODY = 4 * 1024 * 1024;
    private static final ScheduledExecutorService SCHED = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "bcn-mcp-sched");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean started;

    private McpHttpd() {
    }

    public static void start() {
        if (started) {
            return;
        }
        started = true;
        int port = Integer.getInteger("bcn.mcp.port", 25580);
        Thread t = new Thread(() -> serve(port), "bcn-mcp-http");
        t.setDaemon(true);
        t.start();
        LOG.info("[bcn-mcp] control endpoint listening on 0.0.0.0:{}", port);
    }

    private static void serve(int port) {
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("0.0.0.0", port));
            while (!server.isClosed()) {
                Socket s;
                try {
                    s = server.accept();
                } catch (Exception e) {
                    return;
                }
                Thread h = new Thread(() -> handle(s), "bcn-mcp-conn");
                h.setDaemon(true);
                h.start();
            }
        } catch (Exception e) {
            LOG.warn("[bcn-mcp] http server failed", e);
        }
    }

    private static void handle(Socket s) {
        try (s) {
            s.setSoTimeout(15000);
            InputStream in = s.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
            String requestLine = reader.readLine();
            if (requestLine == null) {
                return;
            }
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                respond(s, 400, "{\"ok\":false,\"error\":\"bad request\"}");
                return;
            }
            String method = parts[0];
            String path = parts[1];
            int contentLength = 0;
            String auth = "";
            for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
                int c = line.indexOf(':');
                if (c < 0) {
                    continue;
                }
                String name = line.substring(0, c).trim();
                String value = line.substring(c + 1).trim();
                if (name.equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(value);
                } else if (name.equalsIgnoreCase("Authorization")) {
                    auth = value;
                }
            }

            String requiredToken = System.getProperty("bcn.mcp.token", "");
            if (!requiredToken.isEmpty() && !auth.equals("Bearer " + requiredToken)) {
                respond(s, 401, "{\"ok\":false,\"error\":\"unauthorized\"}");
                return;
            }

            if (method.equals("GET") && (path.equals("/ping") || path.equals("/"))) {
                respond(s, 200, "{\"ok\":true,\"service\":\"bcn-mcp\"}");
                return;
            }
            if (method.equals("GET")) {
                respond(s, 405, "{\"ok\":false,\"error\":\"method not allowed\"}");
                return;
            }
            if (!method.equals("POST") || !path.equals("/call")) {
                respond(s, 404, "{\"ok\":false,\"error\":\"not found\"}");
                return;
            }
            if (contentLength <= 0 || contentLength > MAX_BODY) {
                respond(s, 400, "{\"ok\":false,\"error\":\"bad content-length\"}");
                return;
            }

            // BufferedReader may have consumed body bytes into its buffer; read the
            // exact byte count through it (ISO-8859-1 keeps a 1:1 byte mapping).
            char[] bodyChars = new char[contentLength];
            int off = 0;
            while (off < contentLength) {
                int n = reader.read(bodyChars, off, contentLength - off);
                if (n < 0) {
                    respond(s, 400, "{\"ok\":false,\"error\":\"truncated body\"}");
                    return;
                }
                off += n;
            }
            byte[] bodyBytes = new String(bodyChars).getBytes(StandardCharsets.ISO_8859_1);
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            JsonObject req;
            try {
                req = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                respond(s, 400, "{\"ok\":false,\"error\":\"bad json\"}");
                return;
            }
            String tool = req.has("tool") ? req.get("tool").getAsString() : "";
            JsonObject args = req.has("args") && req.get("args").isJsonObject()
                    ? req.getAsJsonObject("args") : new JsonObject();

            JsonObject result = dispatch(tool, args);
            respond(s, 200, result.toString());
        } catch (Exception e) {
            try {
                respond(s, 500, "{\"ok\":false,\"error\":\"internal\"}");
            } catch (Exception ignored) {
            }
        }
    }

    private static void respond(Socket s, int code, String body) throws Exception {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        String reason = switch (code) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            default -> "Internal Server Error";
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(("HTTP/1.1 " + code + " " + reason + "\r\n"
                + "Content-Type: application/json\r\n"
                + "Content-Length: " + payload.length + "\r\n"
                + "Connection: close\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
        out.write(payload);
        OutputStream os = s.getOutputStream();
        os.write(out.toByteArray());
        os.flush();
    }

    private static JsonObject err(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("error", msg);
        return o;
    }

    private static JsonObject ok(JsonObject result) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", true);
        o.add("result", result);
        return o;
    }

    private static JsonObject dispatch(String tool, JsonObject args) {
        return switch (tool) {
            case "status" -> runOnClient(McpHttpd::toolStatus);
            case "say" -> runOnClient(() -> toolSay(args));
            case "look" -> runOnClient(() -> toolLook(args));
            case "key" -> runOnClient(() -> toolKey(args));
            case "press" -> runOnClient(() -> toolPress(args));
            case "hotbar" -> runOnClient(() -> toolHotbar(args));
            case "inventory" -> runOnClient(McpHttpd::toolInventory);
            case "screenshot" -> toolScreenshot();
            default -> err("unknown tool: " + tool);
        };
    }

    private static JsonObject runOnClient(java.util.function.Supplier<JsonObject> fn) {
        CompletableFuture<JsonObject> f = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                f.complete(fn.get());
            } catch (Throwable t) {
                f.complete(err(t.getClass().getSimpleName() + ": " + t.getMessage()));
            }
        });
        try {
            return f.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            return err("timeout");
        }
    }

    private static JsonObject toolStatus() {
        Minecraft mc = Minecraft.getInstance();
        JsonObject r = new JsonObject();
        if (mc.player == null || mc.level == null) {
            r.addProperty("in_world", false);
            r.addProperty("server", mc.isMultiplayerServer() ? "multiplayer" : "menu/singleplayer");
            return ok(r);
        }
        r.addProperty("in_world", true);
        r.addProperty("name", mc.player.getGameProfile().name());
        r.addProperty("x", mc.player.getX());
        r.addProperty("y", mc.player.getY());
        r.addProperty("z", mc.player.getZ());
        r.addProperty("yaw", mc.player.getYRot());
        r.addProperty("pitch", mc.player.getXRot());
        r.addProperty("health", mc.player.getHealth());
        r.addProperty("food", mc.player.getFoodData().getFoodLevel());
        r.addProperty("dimension", mc.player.level().dimension().identifier().toString());
        r.addProperty("dead", mc.player.isDeadOrDying());
        if (mc.gameMode != null && mc.gameMode.getPlayerMode() != null) {
            r.addProperty("gamemode", mc.gameMode.getPlayerMode().toString());
        }
        if (mc.getCurrentServer() != null) {
            r.addProperty("server", mc.getCurrentServer().ip);
        } else {
            r.addProperty("server", mc.hasSingleplayerServer() ? "singleplayer" : "unknown");
        }
        HitResult hit = mc.hitResult;
        if (hit != null) {
            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
                var pos = bhr.getBlockPos();
                var state = mc.level.getBlockState(pos);
                JsonObject t = new JsonObject();
                t.addProperty("type", "block");
                t.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                t.addProperty("x", pos.getX());
                t.addProperty("y", pos.getY());
                t.addProperty("z", pos.getZ());
                r.add("looking_at", t);
            } else if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult ehr) {
                JsonObject t = new JsonObject();
                t.addProperty("type", "entity");
                t.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(ehr.getEntity().getType()).toString());
                r.add("looking_at", t);
            }
        }
        return ok(r);
    }

    private static JsonObject toolSay(JsonObject args) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return err("not connected");
        }
        String msg = args.get("message").getAsString();
        if (msg.startsWith("/")) {
            mc.getConnection().sendCommand(msg.substring(1));
        } else {
            mc.getConnection().sendChat(msg);
        }
        JsonObject r = new JsonObject();
        r.addProperty("sent", msg);
        return ok(r);
    }

    private static JsonObject toolLook(JsonObject args) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return err("no player");
        }
        if (args.has("yaw")) {
            float yaw = args.get("yaw").getAsFloat();
            mc.player.setYRot(yaw);
            mc.player.setYHeadRot(yaw);
        }
        if (args.has("pitch")) {
            mc.player.setXRot(args.get("pitch").getAsFloat());
        }
        JsonObject r = new JsonObject();
        r.addProperty("yaw", mc.player.getYRot());
        r.addProperty("pitch", mc.player.getXRot());
        return ok(r);
    }

    private static KeyMapping mappingFor(Minecraft mc, String key) {
        return switch (key) {
            case "forward" -> mc.options.keyUp;
            case "back" -> mc.options.keyDown;
            case "left" -> mc.options.keyLeft;
            case "right" -> mc.options.keyRight;
            case "jump" -> mc.options.keyJump;
            case "sneak" -> mc.options.keyShift;
            case "sprint" -> mc.options.keySprint;
            case "use" -> mc.options.keyUse;
            case "attack" -> mc.options.keyAttack;
            case "drop" -> mc.options.keyDrop;
            case "swap_offhand" -> mc.options.keySwapOffhand;
            case "pick_block" -> mc.options.keyPickItem;
            default -> null;
        };
    }

    private static JsonObject toolKey(JsonObject args) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping km = mappingFor(mc, args.get("key").getAsString());
        if (km == null) {
            return err("unknown key");
        }
        km.setDown(args.get("down").getAsBoolean());
        JsonObject r = new JsonObject();
        r.addProperty("key", args.get("key").getAsString());
        r.addProperty("down", km.isDown());
        return ok(r);
    }

    private static JsonObject toolPress(JsonObject args) {
        Minecraft mc = Minecraft.getInstance();
        String key = args.get("key").getAsString();
        KeyMapping km = mappingFor(mc, key);
        if (km == null) {
            return err("unknown key");
        }
        long ms = args.has("ms") ? args.get("ms").getAsLong() : 100;
        km.setDown(true);
        SCHED.schedule(() -> Minecraft.getInstance().execute(() -> km.setDown(false)),
                Math.max(20, ms), TimeUnit.MILLISECONDS);
        JsonObject r = new JsonObject();
        r.addProperty("pressed", key);
        r.addProperty("ms", ms);
        return ok(r);
    }

    private static JsonObject toolHotbar(JsonObject args) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return err("no player");
        }
        int slot = args.get("slot").getAsInt();
        Inventory inv = mc.player.getInventory();
        if (slot < 0 || slot >= Inventory.getSelectionSize()) {
            return err("slot out of range");
        }
        inv.setSelectedSlot(slot);
        JsonObject r = new JsonObject();
        r.addProperty("selected", inv.getSelectedSlot());
        return ok(r);
    }

    private static JsonObject toolInventory() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return err("no player");
        }
        Inventory inv = mc.player.getInventory();
        JsonArray items = new JsonArray();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            JsonObject it = new JsonObject();
            it.addProperty("slot", i);
            it.addProperty("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            it.addProperty("count", stack.getCount());
            items.add(it);
        }
        JsonObject r = new JsonObject();
        r.addProperty("selected_slot", inv.getSelectedSlot());
        r.add("items", items);
        return ok(r);
    }

    private static JsonObject toolScreenshot() {
        CompletableFuture<JsonObject> f = new CompletableFuture<>();
        Minecraft.getInstance().execute(() -> {
            try {
                Screenshot.takeScreenshot(Minecraft.getInstance().gameRenderer.mainRenderTarget(), image -> {
                    try {
                        Path tmp = Files.createTempFile("bcn-mcp-shot", ".png");
                        image.writeToFile(tmp);
                        image.close();
                        byte[] png = Files.readAllBytes(tmp);
                        Files.deleteIfExists(tmp);
                        JsonObject r = new JsonObject();
                        r.addProperty("png_base64", Base64.getEncoder().encodeToString(png));
                        r.addProperty("bytes", png.length);
                        f.complete(ok(r));
                    } catch (Throwable t) {
                        f.complete(err("encode: " + t.getMessage()));
                    }
                });
            } catch (Throwable t) {
                f.complete(err(t.getClass().getSimpleName() + ": " + t.getMessage()));
            }
        });
        try {
            return f.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            return err("screenshot timeout");
        }
    }
}
