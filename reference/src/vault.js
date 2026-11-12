// Spec 09: vault layout, one-writer rule, note formats, quote encryption, adoption, retention.
import { createCipheriv, createDecipheriv, randomBytes } from 'node:crypto';
import { iso, parts, DAY } from './time.js';

// ---- IDs: <kind>-<YYYYMMDD>-<HHMMSS>-<dev>-<nn> ----
export function makeId(kind, tMs, dev, nn) {
  const p = parts(tMs), z = (n, w = 2) => String(n).padStart(w, '0');
  const s = new Date(tMs + 330 * 60000);
  return `${kind}-${p.y}${z(p.mo + 1)}${z(p.d)}-${z(p.h)}${z(p.mi)}${z(s.getUTCSeconds())}-${dev}-${z(nn)}`;
}

// ---- Who may write what (one writer per file) ----
const DEVICES = ['ph', 'pc', 'lt'];
export function canWrite(path, dev, { isBrain = dev === 'pc' } = {}) {
  const m = path.match(/^Nayom\/([^/]+)\/(?:([^/]+)\/)?/);
  if (!m) return false;
  const [, top, sub] = m;
  if (top === 'events' || top === 'ledger') return DEVICES.includes(sub) && sub === dev; // own folder only
  if (top === 'verdicts' || top === 'days' || top === 'proof') return isBrain;
  if (top === 'people') return isBrain;
  if (top === 'proposals') return sub === 'pending' ? isBrain : true; // any device moves it on approval
  if (['promises', 'asks', 'debriefs'].includes(top)) return true; // capturing device, create-only (see createOnce)
  if (top === 'slips') return true; // brain, or phone marked provisional
  if (top === '_system' || top === 'stretch' || top === 'trash') return true;
  return false;
}

// ---- Notes ----
const yamlValue = (v) => (v === null ? 'null' : typeof v === 'boolean' || typeof v === 'number' ? String(v) : /^\d{4}-\d{2}-\d{2}T/.test(v) ? v : JSON.stringify(v));
export function frontmatter(obj) {
  return `---\n${Object.entries(obj).map(([k, v]) => `${k}: ${Array.isArray(v) ? `[${v.map((x) => JSON.stringify(x)).join(', ')}]` : yamlValue(v)}`).join('\n')}\n---\n`;
}
export function parseFrontmatter(text) {
  const m = text.match(/^---\n([\s\S]*?)\n---\n?([\s\S]*)$/);
  if (!m) return { props: {}, body: text };
  const props = {};
  for (const line of m[1].split('\n')) {
    const i = line.indexOf(':'); if (i < 0) continue;
    const k = line.slice(0, i).trim(); let v = line.slice(i + 1).trim();
    if (v === 'null') v = null; else if (v === 'true') v = true; else if (v === 'false') v = false;
    else if (/^-?\d+(\.\d+)?$/.test(v)) v = Number(v);
    else if (v.startsWith('"') || v.startsWith('[')) { try { v = JSON.parse(v); } catch { /* keep text */ } }
    props[k] = v;
  }
  return { props, body: m[2] };
}
export function promiseNote(p) {
  return frontmatter({
    nayom: 'promise', id: p.id, person: `[[${p.person}]]`, what: p.what, due: iso(p.due), due_source: p.due_source,
    created: iso(p.created), source_app: p.source_app, quote_enc: p.quote_enc ?? null, language: p.language ?? 'en',
    confidence: p.confidence, private: !!p.private,
  });
}
// Private sources never carry a quote or text, only ts | kind | app (spec 09 event lines).
export function eventLine(e) {
  return e.private ? `${iso(e.ts)} | ${e.kind} | ${e.app}`
    : [e.id, iso(e.ts), e.kind, e.app, e.person ?? '', e.project ?? '', (e.tags ?? []).join(','), e.quote_enc ?? ''].join(' | ');
}

// ---- Quote encryption (D-18): "v1:" + base64(12-byte nonce + ciphertext + tag) ----
export const newKey = () => randomBytes(32);
export function encryptQuote(key, text) {
  const q = text.trim();
  if (q.length > 140) throw new RangeError('Quote is longer than 140 characters');
  if (/[.!?]\s+\S/.test(q)) throw new RangeError('Quote must be one sentence');
  const nonce = randomBytes(12), c = createCipheriv('aes-256-gcm', key, nonce);
  const ct = Buffer.concat([c.update(q, 'utf8'), c.final()]);
  return `v1:${Buffer.concat([nonce, ct, c.getAuthTag()]).toString('base64')}`;
}
export function decryptQuote(key, enc) {
  if (!enc?.startsWith('v1:')) throw new Error('Unknown quote format');
  const b = Buffer.from(enc.slice(3), 'base64');
  const d = createDecipheriv('aes-256-gcm', key, b.subarray(0, 12));
  d.setAuthTag(b.subarray(b.length - 16));
  return Buffer.concat([d.update(b.subarray(12, b.length - 16)), d.final()]).toString('utf8');
}

// ---- Adoption (D-13). fs = { read(path), write(path, text), move(from, to), exists(path) } ----
export class NeedsProposal extends Error {}
export const isAdopted = (text) => parseFrontmatter(text).props.nayom === 'adopted';
export function adopt(text) {
  const { props, body } = parseFrontmatter(text);
  if (props.nayom === 'adopted') return text;
  const raw = text.startsWith('---\n') ? text.replace(/^---\n/, '---\nnayom: adopted\n') : `---\nnayom: adopted\n---\n${text}`;
  return props && body !== undefined ? raw : text;
}
const stamp = (t) => iso(t).replace(/[-:]/g, '').replace('+0530', '').replace('T', '-');
export const snapshotPath = (path, t) => `Nayom/trash/${stamp(t)}-${path.replace(/\//g, '__')}`;
export function editExisting(fs, path, newText, nowMs, { createdByNayom = false } = {}) {
  const old = fs.read(path);
  if (!createdByNayom && !isAdopted(old)) throw new NeedsProposal(`${path} is not adopted: write a proposal`);
  fs.write(snapshotPath(path, nowMs), old); // snapshot first, kept 30 days
  fs.write(path, newText);
}
export function deleteNote(fs, path, nowMs, { createdByNayom = false, userTapped = false } = {}) {
  const adopted = isAdopted(fs.read(path));
  if (!createdByNayom && !(adopted && userTapped)) throw new NeedsProposal(`${path}: deleting needs your tap`);
  if (adopted && !userTapped) throw new NeedsProposal(`${path}: adopted notes need your tap to delete`);
  fs.move(path, snapshotPath(path, nowMs)); // never removed outright
}

// ---- Retention ----
export function retention(files, nowMs, { openSlipIds = new Set(), proofIds = new Set() } = {}) {
  const out = [];
  for (const f of files) { // f: { path, ts, id? }
    const age = (nowMs - f.ts) / DAY;
    if (f.path.startsWith('Nayom/events/') && age > 30) out.push({ path: f.path, action: 'reduce-to-daily-counts' });
    else if (f.path.startsWith('Nayom/trash/') && age > 30) out.push({ path: f.path, action: 'purge' });
    else if (f.path.startsWith('Nayom/asks/') && age > 90) out.push({ path: f.path, action: 'move-to-trash' });
    if (f.hasQuote && age > 30 && !openSlipIds.has(f.id) && !proofIds.has(f.id)) out.push({ path: f.path, action: 'blank-quote' });
  } // promises, slips and proof are kept forever
  return out;
}
