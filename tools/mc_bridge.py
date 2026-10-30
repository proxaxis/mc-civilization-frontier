#!/usr/bin/env python3
"""MCP stdio bridge -> BCN client HTTP control endpoint.

Devin CLI launches this as a stdio MCP server. Each MCP tool call is proxied
to the Minecraft client mod's HTTP endpoint:

    POST http://<MC_HOST>:<MC_PORT>/call {"tool": ..., "args": ...}

Env:
    MC_HOST  default 192.168.2.248
    MC_PORT  default 25580
    MC_TOKEN optional bearer token (matches -Dbcn.mcp.token on the client)
"""
import json
import os
import sys
import urllib.request
import urllib.error

HOST = os.environ.get("MC_HOST", "192.168.2.248")
PORT = int(os.environ.get("MC_PORT", "25580"))
TOKEN = os.environ.get("MC_TOKEN", "")
BASE = f"http://{HOST}:{PORT}"

TOOLS = [
    {
        "name": "status",
        "description": "Get the Minecraft client's player status: position, dimension, health, gamemode, server, the block/entity being looked at, and whether a GUI screen is open.",
        "inputSchema": {"type": "object", "properties": {}},
    },
    {
        "name": "say",
        "description": "Send a chat message from the player. If it starts with '/', it is executed as a command (requires the appropriate permission level on the server).",
        "inputSchema": {
            "type": "object",
            "properties": {
                "message": {"type": "string", "description": "Chat text or a command starting with '/'"}
            },
            "required": ["message"],
        },
    },
    {
        "name": "look",
        "description": "Set the player's view direction. yaw: 0=south, 90=west, 180/-180=north, -90=east. pitch: -90=up, 90=down.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "yaw": {"type": "number"},
                "pitch": {"type": "number"},
            },
        },
    },
    {
        "name": "press",
        "description": "Press and hold a control key for a duration, then release. Use 'attack' to break/mine the looked-at block (hold long enough), 'use' to place/interact with the held item, movement keys to walk.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "key": {
                    "type": "string",
                    "enum": ["forward", "back", "left", "right", "jump", "sneak", "sprint", "use", "attack", "drop", "swap_offhand", "pick_block"],
                },
                "ms": {"type": "integer", "description": "Hold duration in milliseconds", "default": 100},
            },
            "required": ["key"],
        },
    },
    {
        "name": "key",
        "description": "Set a control key's held state explicitly (down=true/false). Same key names as 'press'. Useful for continuous movement.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "key": {
                    "type": "string",
                    "enum": ["forward", "back", "left", "right", "jump", "sneak", "sprint", "use", "attack", "drop", "swap_offhand", "pick_block"],
                },
                "down": {"type": "boolean"},
            },
            "required": ["key", "down"],
        },
    },
    {
        "name": "hotbar",
        "description": "Select a hotbar slot (0-8) so subsequent 'use' presses use that item.",
        "inputSchema": {
            "type": "object",
            "properties": {"slot": {"type": "integer", "minimum": 0, "maximum": 8}},
            "required": ["slot"],
        },
    },
    {
        "name": "inventory",
        "description": "List the player's inventory contents (slot, item id, count) and the selected hotbar slot.",
        "inputSchema": {"type": "object", "properties": {}},
    },
    {
        "name": "screenshot",
        "description": "Capture the current game view as a PNG image.",
        "inputSchema": {"type": "object", "properties": {}},
    },
]


def call_client(tool, args):
    body = json.dumps({"tool": tool, "args": args or {}}).encode("utf-8")
    req = urllib.request.Request(
        BASE + "/call",
        data=body,
        headers={"Content-Type": "application/json", "Authorization": f"Bearer {TOKEN}"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def result_text(text, is_error=False):
    return {"content": [{"type": "text", "text": text}], "isError": is_error}


def handle_tools_call(params):
    name = params.get("name", "")
    args = params.get("arguments") or {}
    try:
        resp = call_client(name, args)
    except urllib.error.URLError as e:
        return result_text(f"client unreachable ({BASE}): {e}", True)
    except Exception as e:
        return result_text(f"bridge error: {e}", True)
    if not resp.get("ok"):
        return result_text(f"client error: {resp.get('error', 'unknown')}", True)
    result = resp.get("result")
    if name == "screenshot" and isinstance(result, dict) and "png_base64" in result:
        return {"content": [{"type": "image", "data": result["png_base64"], "mimeType": "image/png"}]}
    return result_text(json.dumps(result, ensure_ascii=False, indent=2))


def reply(msg_id, result):
    sys.stdout.write(json.dumps({"jsonrpc": "2.0", "id": msg_id, "result": result}) + "\n")
    sys.stdout.flush()


def reply_error(msg_id, code, message):
    sys.stdout.write(json.dumps({"jsonrpc": "2.0", "id": msg_id, "error": {"code": code, "message": message}}) + "\n")
    sys.stdout.flush()


def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            msg = json.loads(line)
        except json.JSONDecodeError:
            continue
        method = msg.get("method", "")
        msg_id = msg.get("id")
        if msg_id is None or method.startswith("notifications/"):
            continue
        if method == "initialize":
            params = msg.get("params") or {}
            reply(msg_id, {
                "protocolVersion": params.get("protocolVersion", "2025-03-26"),
                "capabilities": {"tools": {"listChanged": False}},
                "serverInfo": {"name": "bcn-minecraft-client", "version": "0.1.0"},
            })
        elif method == "ping":
            reply(msg_id, {})
        elif method == "tools/list":
            reply(msg_id, {"tools": TOOLS})
        elif method == "tools/call":
            reply(msg_id, handle_tools_call(msg.get("params") or {}))
        else:
            reply_error(msg_id, -32601, f"method not found: {method}")


if __name__ == "__main__":
    main()
