import { WebSocketServer } from "ws";
import { rooms, forwardAudio } from "./rooms.js";

const PORT = 6789;
const wss = new WebSocketServer({ port: PORT });

wss.on("connection", (ws, req) => {
  const url = new URL(req.url, "http://localhost");
  const stream = url.searchParams.get("stream") || "unknown";
  const role = url.searchParams.get("role") || "listener";

  if (!rooms.has(stream)) rooms.set(stream, new Set());
  rooms.get(stream).add(ws);
  console.log(`[6789] ${role} connected — ${stream} (total: ${rooms.get(stream).size})`);

  ws.send(JSON.stringify({ type: "connected", stream, role }));

  ws.on("message", (data) => {
    try {
      const msg = JSON.parse(data.toString());
      if (msg.type === "audio" && msg.stream) {
        forwardAudio(msg, ws);
      }
    } catch {}
  });

  ws.on("close", () => {
    const room = rooms.get(stream);
    if (room) {
      room.delete(ws);
      if (room.size === 0) rooms.delete(stream);
    }
    console.log(`[6789] ${role} disconnected — ${stream}`);
  });
});

console.log(`[6789] WebSocket relay on ws://0.0.0.0:${PORT}`);
