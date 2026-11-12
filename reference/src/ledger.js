// Spec 09: ledger lines "ts | id | status | evidence | by | new_due". Append-only; latest ts wins.
export const STATUSES = ['open', 'moved', 'kept', 'missed', 'released', 'fixed-by'];
export function parseLedger(text) {
  const rows = [];
  for (const raw of text.split('\n')) {
    const line = raw.trim();
    if (!line || line.startsWith('```') || line.startsWith('ts |')) continue;
    const [ts, id, status, evidence = '', by = '', newDue = ''] = line.split('|').map((c) => c.trim());
    if (!ts || !id || !STATUSES.includes(status)) throw new Error(`Bad ledger line: ${line}`);
    if ((status === 'moved' || status === 'fixed-by') && !newDue) throw new Error(`new_due required for ${status}: ${line}`);
    if (status !== 'moved' && status !== 'fixed-by' && newDue) throw new Error(`new_due only for moved/fixed-by: ${line}`);
    rows.push({ ts: Date.parse(ts), id, status, evidence: evidence ? evidence.split(',') : [], by, newDue: newDue || null });
  }
  return rows;
}
// Merge every device's lines (one writer per file means no conflicts), de-duplicate, order by ts.
export function reduceLedger(rows) {
  const seen = new Set();
  const sorted = [...rows].sort((a, b) => a.ts - b.ts || STATUSES.indexOf(a.status) - STATUSES.indexOf(b.status));
  const out = new Map();
  for (const r of sorted) {
    const key = `${r.ts}|${r.id}|${r.status}`;
    if (seen.has(key)) continue;
    seen.add(key);
    const prev = out.get(r.id);
    out.set(r.id, { id: r.id, status: r.status, due: r.newDue ?? prev?.due ?? null, evidence: r.evidence.length ? r.evidence : prev?.evidence ?? [], history: [...(prev?.history ?? []), r.status] });
  }
  return out;
}
