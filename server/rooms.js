export const rooms = new Map();

export function arrayToBase64(arr) {
  const bytes = new Uint8Array(arr);
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

export function forwardAudio(msg, sender) {
  const targetRoom = rooms.get(msg.stream);
  if (!targetRoom) return;
  let payload;
  if (Array.isArray(msg.data)) {
    payload = JSON.stringify({ type: "audio", data: arrayToBase64(msg.data), sampleRate: msg.sampleRate || 44100 });
  } else if (typeof msg.data === "string") {
    payload = JSON.stringify({ type: "audio", data: msg.data, sampleRate: msg.sampleRate || 44100 });
  }
  if (!payload) return;
  for (const client of targetRoom) {
    if (client !== sender && client.readyState === 1) {
      client.send(payload);
    }
  }
}
