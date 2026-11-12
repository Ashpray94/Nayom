// Spec 10 "Day shape learning (D-09)" and spec 08 praise/proof rules.
import { MIN } from './time.js';

export const INITIAL_SHAPE = { wake: '07:30', bedtime: '23:30', work: ['09:30', '18:30'], eveningSheet: '20:30' };
const toMin = (hhmm) => { const [h, m] = hhmm.split(':').map(Number); return h * 60 + m; };
const toHHMM = (min) => { const m = ((Math.round(min) % 1440) + 1440) % 1440; return `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`; };

// samples: [{ min: minutes after 00:00 (bedtimes after midnight use +1440), weight }]
export function weightedMedian(samples) {
  if (!samples.length) return null;
  const s = [...samples].sort((a, b) => a.min - b.min);
  const total = s.reduce((t, x) => t + x.weight, 0);
  let acc = 0;
  for (const x of s) { acc += x.weight; if (acc >= total / 2) return x.min; }
}
// Wake: median first unlock for a weekday over 14 days. A correction by the person counts as 3 nights.
export function learnWake(firstUnlocks, corrections = [], { minNights = 5 } = {}) {
  const samples = [...firstUnlocks.map((t) => ({ min: toMin(t), weight: 1 })), ...corrections.map((t) => ({ min: toMin(t), weight: 3 }))];
  const n = firstUnlocks.length + corrections.length * 3;
  return n < minNights ? INITIAL_SHAPE.wake : toHHMM(weightedMedian(samples));
}
// Sleep: median start of the longest screen-off and PC-idle gap each night (after midnight counts as +24 h).
export function learnBedtime(starts, corrections = [], { minNights = 5 } = {}) {
  const v = (t) => { const m = toMin(t); return m < 360 ? m + 1440 : m; };
  const samples = [...starts.map((t) => ({ min: v(t), weight: 1 })), ...corrections.map((t) => ({ min: v(t), weight: 3 }))];
  const n = starts.length + corrections.length * 3;
  return n < minNights ? INITIAL_SHAPE.bedtime : toHHMM(weightedMedian(samples));
}
// Work block: spans where work apps or project windows are active for 20 minutes or more, merged when gaps are under 10 min.
export function workBlocks(spans, { minMin = 20, mergeGapMin = 10 } = {}) {
  const sorted = [...spans].sort((a, b) => a.start - b.start), merged = [];
  for (const s of sorted) {
    const last = merged.at(-1);
    if (last && s.start - last.end <= mergeGapMin * MIN) last.end = Math.max(last.end, s.end); else merged.push({ ...s });
  }
  return merged.filter((b) => (b.end - b.start) / MIN >= minMin);
}

// ---- Weekly proof and praise (spec 08 Praise rules) ----
export function weekCount(ledgerMap, ids) {
  const kept = ids.filter((id) => ['kept', 'fixed-by'].includes(ledgerMap.get(id)?.status)).length;
  return { kept, total: ids.length, line: `Kept ${kept} of ${ids.length}` };
}
// Never after one kept promise; first at three in a row; evidence in numbers; never alongside a slip.
export function praiseAllowed({ keptInARow, slipInSameCard, lastPraisedAtStreak = 0 }) {
  if (slipInSameCard) return false;
  return keptInARow >= 3 && keptInARow > lastPraisedAtStreak;
}
