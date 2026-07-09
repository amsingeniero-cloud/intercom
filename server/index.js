const http = require('http');
const WebSocket = require('ws');

const PORT = process.env.PORT || 8080;

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('intercom signaling ok');
});

const wss = new WebSocket.Server({ server });

// peerId -> ws
const peers = new Map();

function send(ws, obj) {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(obj));
  }
}

wss.on('connection', (ws) => {
  let id = null;

  ws.on('message', (raw) => {
    let msg;
    try {
      msg = JSON.parse(raw);
    } catch (err) {
      return;
    }

    if (msg.type === 'join' && msg.id) {
      id = msg.id;
      peers.set(id, ws);

      const existing = [...peers.keys()].filter((peerId) => peerId !== id);
      send(ws, { type: 'existing-peers', peers: existing });

      for (const [peerId, peerWs] of peers) {
        if (peerId !== id) send(peerWs, { type: 'peer-joined', id });
      }
      return;
    }

    if (msg.type === 'signal' && msg.to && id) {
      const target = peers.get(msg.to);
      if (target) send(target, { type: 'signal', from: id, data: msg.data });
      return;
    }
  });

  ws.on('close', () => {
    if (id) {
      peers.delete(id);
      for (const [, peerWs] of peers) send(peerWs, { type: 'peer-left', id });
    }
  });
});

server.listen(PORT, () => {
  console.log(`intercom signaling listening on :${PORT}`);
});
