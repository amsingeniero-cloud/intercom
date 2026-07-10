const http = require('http');
const WebSocket = require('ws');

const PORT = process.env.PORT || 8080;

// Credenciales TURN reales (metered.ca). Se configuran como variables de entorno
// en Render (Environment), nunca se suben al repo ni se embeben en la app.
const METERED_SUBDOMAIN = process.env.METERED_SUBDOMAIN;
const METERED_API_KEY = process.env.METERED_API_KEY;

async function handleTurnCredentials(res) {
  if (!METERED_SUBDOMAIN || !METERED_API_KEY) {
    res.writeHead(500, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'METERED_SUBDOMAIN / METERED_API_KEY no configuradas' }));
    return;
  }
  try {
    const url = `https://${METERED_SUBDOMAIN}/api/v1/turn/credentials?apiKey=${METERED_API_KEY}`;
    const response = await fetch(url);
    const body = await response.text();
    res.writeHead(response.status, { 'Content-Type': 'application/json' });
    res.end(body);
  } catch (err) {
    res.writeHead(502, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: String(err) }));
  }
}

const server = http.createServer((req, res) => {
  if (req.url === '/turn-credentials') {
    handleTurnCredentials(res);
    return;
  }

  res.writeHead(200, { 'Content-Type': 'text/plain' });
  res.end('intercom signaling ok');
});

const wss = new WebSocket.Server({ server });

// peerId -> ws
const peers = new Map();
// peerId -> string[] (canales activos de ese peer; nunca toca audio, solo texto)
const peerChannels = new Map();

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
      peerChannels.set(id, []);

      const existing = [...peers.keys()]
        .filter((peerId) => peerId !== id)
        .map((peerId) => ({ id: peerId, channels: peerChannels.get(peerId) || [] }));
      send(ws, { type: 'existing-peers', peers: existing });

      for (const [peerId, peerWs] of peers) {
        if (peerId !== id) send(peerWs, { type: 'peer-joined', id });
      }
      return;
    }

    if (msg.type === 'channels' && id && Array.isArray(msg.channels)) {
      peerChannels.set(id, msg.channels);
      for (const [peerId, peerWs] of peers) {
        if (peerId !== id) send(peerWs, { type: 'peer-channels', id, channels: msg.channels });
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
      peerChannels.delete(id);
      for (const [, peerWs] of peers) send(peerWs, { type: 'peer-left', id });
    }
  });
});

server.listen(PORT, () => {
  console.log(`intercom signaling listening on :${PORT}`);
});
