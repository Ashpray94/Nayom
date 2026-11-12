import { DEFAULTS } from './settings.js';
import { MIN, HOUR, DAY, at, parts } from './time.js';
import { normalize, hasAny, hasPhrase, SEEDS, SUPPORT } from './text.js';

const WEEKDAYS = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];

// ---- SR-06 Support rule: checked first, suppresses everything else ----
export function supportRule(text) {
  const n = normalize(text);
  return n != null && hasAny(n, SUPPORT)
    ? { suppress: true, card: 'support', helpline: '14416', writeToVault: false }
    : { suppress: false };
}

// ---- SIG-01 Promise capture ----
export function defaultDue(nowMs) {
  const { h } = parts(nowMs);
  return h < 15 ? at(nowMs, 0, 18) : at(nowMs, 1, 11); // 18:00 today if before 15:00, else 11:00 tomorrow
}
export function parseDue(norm, nowMs) {
  const wd = WEEKDAYS.findIndex((d) => hasPhrase(norm, `by ${d}`));
  if (wd >= 0) {
    const ahead = ((wd - parts(nowMs).dow + 7) % 7) || 7;
    return { due: at(nowMs, ahead, 18), source: 'stated' };
  }
  if (hasAny(norm, ['in an hour', 'ek ghante mein'])) return { due: nowMs + HOUR, source: 'stated' };
  if (hasAny(norm, ['tomorrow', 'kal tak'])) return { due: at(nowMs, 1, 11), source: 'stated' };
  if (hasAny(norm, ['tonight', 'raat tak'])) return { due: at(nowMs, 0, 21), source: 'stated' };
  if (hasAny(norm, ['eod', 'today', 'aaj', 'aaj shaam tak'])) return { due: at(nowMs, 0, 18), source: 'stated' };
  if (hasAny(norm, ['subah tak'])) return { due: at(nowMs, 1, 9), source: 'stated' };
  if (hasAny(norm, ['this week'])) return { due: at(nowMs, (5 - parts(nowMs).dow + 7) % 7, 18), source: 'stated' };
  return null;
}
export function capturePromise({ text, person, nowMs }, s = DEFAULTS) {
  const n = normalize(text);
  if (n == null) return null;
  const doing = hasAny(n, SEEDS.doing);
  const stated = parseDue(n, nowMs);
  const accept = hasAny(n, SEEDS.acceptance);
  if (!doing || (!stated && !accept)) return null;
  const confidence = Math.min(0.98, 0.25 + 0.2 + (stated ? 0.25 : 0) + (accept ? 0.1 : 0) + (person ? 0.05 : 0));
  return {
    person: person ?? null,
    due: stated ? stated.due : defaultDue(nowMs),
    due_source: stated ? 'stated' : 'default',
    confidence: Math.round(confidence * 100) / 100,
    draft: confidence < s.promise.draft_below, // goes to "To approve"
  };
}

// ---- SIG-03 Reflex yes (the gate) ----
export function gate({ text, lastAskAgeMin, openDue48h, houseRuleHit, secondsSinceAsk }, s = DEFAULTS) {
  const n = normalize(text);
  if (n == null || lastAskAgeMin == null || lastAskAgeMin > s.gate.ask_window_min) return { fire: false };
  if (!hasAny(n, SEEDS.acceptance)) return { fire: false };
  const full = openDue48h >= s.load.open_due_48h || houseRuleHit || secondsSinceAsk < s.gate.reflex_s;
  return { fire: true, type: full ? 'full' : 'light', pause_ms: s.gate.pause_ms };
}

// ---- SIG-04 Missed promise ----
const keywords = (str) => (normalize(str) ?? '').split(' ').filter((w) => w.length > 3);
export function missedPromise({ promise, events, nowMs, tappedDone }, s = DEFAULTS) {
  const deadline = promise.due + s.promise.grace_min * MIN;
  if (tappedDone) return { status: 'kept', unverified: true, countsTowardEscalation: false, evidence: [] };
  if (nowMs <= deadline) return { status: 'open', evidence: [] };
  const kw = keywords(promise.what);
  const ev = events.filter((e) => e.ts >= promise.created && e.ts <= nowMs && (
    (e.kind === 'sent' && e.person === promise.person && (e.tags ?? []).includes('attachment')) ||
    ((e.kind === 'file-saved' || e.kind === 'file-exported') && kw.some((k) => (e.file ?? '').toLowerCase().includes(k))) ||
    (e.kind === 'sent' && e.person === promise.person && (e.tags ?? []).includes('said-sent'))
  ));
  return ev.length
    ? { status: 'kept', evidence: ev.map((e) => e.id) }
    : { status: 'missed', evidence: [], countsTowardEscalation: true };
}

// ---- SIG-05 Claim versus activity ----
export function claimMismatch({ claim, taskText, spans, events, askedAt, pcOn }, s = DEFAULTS) {
  const out = (m, extra = {}) => ({ mismatch: m, provisional: !pcOn, ...extra });
  const n = normalize(claim.text);
  const kw = keywords(taskText);
  if (hasAny(n, ['sent', 'done', 'ho gaya', 'bhej raha hu']) && !hasAny(n, ['almost done', 'almost there', 'working on it', 'on it', 'nearly'])) {
    const ev = events.filter((e) => e.ts >= askedAt && e.ts <= claim.ts &&
      (e.kind === 'file-saved' || e.kind === 'file-exported' || (e.kind === 'sent' && (e.tags ?? []).includes('attachment'))));
    return out(ev.length === 0, { evidence: ev.map((e) => e.id) });
  }
  const from = claim.ts - s.claim.window_h * HOUR;
  let minutes = 0;
  for (const sp of spans) {
    const a = Math.max(sp.start, from), b = Math.min(sp.end, claim.ts);
    if (b > a && kw.some((k) => `${sp.title ?? ''} ${sp.app ?? ''}`.toLowerCase().includes(k))) minutes += (b - a) / MIN;
  }
  return out(minutes < s.claim.min_minutes, { minutes: Math.round(minutes) });
}

// ---- SIG-06 Statement clash ----
export function statementClash(a, b, s = DEFAULTS) {
  if (a.person === b.person) return { clash: false };
  const ka = new Set(keywords(a.what));
  const shared = keywords(b.what).filter((w) => ka.has(w));
  if (shared.length < s.clash.min_shared_keywords) return { clash: false };
  return { clash: Math.abs(a.due - b.due) > s.clash.min_gap_h * HOUR, evidence: [a.id, b.id] };
}

// ---- SIG-07 Avoidance ----
export function readUnanswered({ tier, readAt, nowMs, inWorkHours }, s = DEFAULTS) {
  return inWorkHours && nowMs - readAt >= s.avoid.read_h[tier] * HOUR;
}
export function typedAndDeleted(deleteTimes, nowMs, s = DEFAULTS) {
  return deleteTimes.filter((t) => nowMs - t <= s.avoid.deleted_window_min * MIN).length >= s.avoid.deleted_count;
}
export function missedCallUnreturned(callAt, returnedAt, nowMs, s = DEFAULTS) {
  return returnedAt == null && nowMs - callAt >= s.avoid.call_h * HOUR;
}

// ---- SIG-08 Distraction ----
export function distraction({ app, continuousMin, switchesLastHour, dueWithin24h, workBlockActive, cardsToday }, s = DEFAULTS) {
  if (!dueWithin24h || !workBlockActive || cardsToday >= s.drift.max_per_day) return false;
  const feed = s.feedApps.includes(String(app).toLowerCase()) && continuousMin >= s.drift.feed_min;
  return feed || switchesLastHour >= s.drift.switches_h;
}

// ---- SIG-12 Escalation (spec 08) ----
export function escalationLevel(count) { return count >= 4 ? 'L3' : count >= 2 ? 'L2' : count >= 1 ? 'L1' : 'none'; }
export function nextCount(count, event) {
  if (event === 'slip') return count + 1;
  if (event === 'kept-on-time') return Math.max(0, count - 1);
  return count; // 'wrong-tap' never raises the level
}

// ---- Learning from Wrong (spec 08): 3 Wrong taps silence a pattern ----
export const patternQuiet = (wrongTaps) => wrongTaps >= 3;

// ---- Scaffold fade (FR-72) ----
export function reminders({ weeklyKeptShare }) {
  // weeklyKeptShare: share of promises kept without a reminder, most recent week last
  const last = weeklyKeptShare.at(-1);
  if (last !== undefined && last < 0.6) return { twoHour: true, dueTime: true, tier23: true };
  const run = (min, weeks) => weeklyKeptShare.length >= weeks && weeklyKeptShare.slice(-weeks).every((v) => v > min);
  if (run(0.9, 4)) return { twoHour: false, dueTime: true, tier23: false }; // due-time only, tier 2 and 3
  if (run(0.8, 2)) return { twoHour: false, dueTime: true, tier23: true };
  return { twoHour: true, dueTime: true, tier23: true };
}
