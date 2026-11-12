// Spec 10 "Language handling": lower-case, squeeze repeated letters, drop punctuation.
// Devanagari text is not read.
const DEVANAGARI = /[ऀ-ॿ]/;
export function normalize(s) {
  if (DEVANAGARI.test(s) && !/[a-z]/i.test(s)) return null; // ignored
  return s
    .toLowerCase()
    .replace(/(.)\1{2,}/g, '$1') // sureee -> sure; "tomorrow" and "hello" are untouched
    .replace(/[^\p{L}\p{N}\s']/gu, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}
export function hasPhrase(norm, phrase) {
  if (norm == null) return false;
  const p = phrase.toLowerCase().replace(/[^\p{L}\p{N}\s']/gu, ' ').replace(/\s+/g, ' ').trim();
  return new RegExp(`(^|\\s)${p.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(\\s|$)`, 'u').test(norm);
}
export const hasAny = (norm, list) => list.some((p) => hasPhrase(norm, p));

export const SEEDS = {
  acceptance: ['sure', 'ok', 'okay', 'yes', 'yep', 'will do', "i'll do it", 'done', 'no problem', 'haan', 'ha kar dunga', 'ho jayega', 'kar deta hu', 'kar dunga', 'theek hai', 'pakka', 'bilkul'],
  time: ['today', 'tonight', 'tomorrow', 'eod', 'this week', 'in an hour', 'aaj', 'aaj shaam tak', 'kal tak', 'subah tak', 'ek ghante mein', 'raat tak', 'by monday', 'by tuesday', 'by wednesday', 'by thursday', 'by friday', 'by saturday', 'by sunday'],
  claim: ['almost done', 'almost there', 'on it', 'working on it', 'nearly', 'bas ho gaya', 'ho gaya', 'kar raha hu', 'bhej raha hu', 'sent', 'done'],
  undersell: ['free of cost', 'no charge', "no problem i'll adjust", "i'll just do it", "no worries it's quick"],
  doing: ['send', 'share', 'finish', 'complete', 'call', 'fix', 'review', 'submit', 'deliver', 'update', 'bhej', 'bhejunga', 'bhej dunga', 'kar dunga', 'kar deta hu', 'dekh lunga', 'ho jayega'],
  imperative: ['can you', 'could you', 'please', 'kar do', 'kar dena'],
};
// SR-06. The only list that suppresses everything else (spec 08 Support rule).
export const SUPPORT = ['kill myself', 'end my life', 'want to die', 'suicide', 'khud ko khatam', 'jeena nahi'];
