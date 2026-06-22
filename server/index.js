import http from "http";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { WebSocketServer } from "ws";
import { rooms, forwardAudio } from "./rooms.js";

const PORT = 3000;
const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, "..", "dist");
const MIME = {
  ".html": "text/html;charset=utf-8",
  ".js":   "application/javascript",
  ".css":  "text/css",
  ".svg":  "image/svg+xml",
  ".png":  "image/png",
  ".json": "application/json",
  ".webmanifest": "application/manifest+json",
};

const server = http.createServer((req, res) => {
  let file = req.url === "/" ? "/index.html" : req.url.split("?")[0];
  const filePath = path.join(ROOT, file);
  if (!filePath.startsWith(ROOT)) { res.writeHead(403); res.end(); return; }
  const ext = path.extname(filePath);
  fs.readFile(filePath, (err, data) => {
    if (err) {
      fs.readFile(path.join(ROOT, "index.html"), (e2, d2) => {
        if (e2) { res.writeHead(404); res.end("Not found"); return; }
        res.writeHead(200, { "Content-Type": "text/html;charset=utf-8" });
        res.end(d2);
      });
      return;
    }
    res.writeHead(200, { "Content-Type": MIME[ext] || "application/octet-stream" });
    res.end(data);
  });
});

const wss = new WebSocketServer({ server });

wss.on("connection", (ws, req) => {
  const url = new URL(req.url, "http://localhost");
  const stream = url.searchParams.get("stream") || "unknown";
  const role = url.searchParams.get("role") || "listener";

  if (!rooms.has(stream)) rooms.set(stream, new Set());
  rooms.get(stream).add(ws);
  console.log(`[3000] ${role} connected — ${stream} (total: ${rooms.get(stream).size})`);

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
    console.log(`[3000] ${role} disconnected — ${stream}`);
  });
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`[3000] HTTP + WebSocket on http://0.0.0.0:${PORT}`);
});
