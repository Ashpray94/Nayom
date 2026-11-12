// Spec 08 "Output" and "Guards on herself": nothing a model returns reaches the screen unchecked.
import { escalationLevel } from './signals.js';

const DECISIONS = ['none', 'notice', 'gate', 'slip', 'mismatch', 'care'];
const PITY = ['poor you', "it's okay", "don't worry", 'no big deal', 'you got this'];
const SHAME = ['lazy', 'useless', 'hopeless', 'failure', 'stupid', 'pathetic', "why can't you", 'again?!'];
const LIE = ['lied', 'lying', 'liar', 'you lie'];
const FLATTER = ['amazing', 'awesome', 'proud of you', 'great job', 'well done'];
const THREAT = ['or else', 'last warning', "i'll tell", 'i will tell', "you'll regret"];
const COMPARE = ['other people', 'everyone else', 'others manage', 'your friends'];
const PET = ['beta', 'baby', 'sweetie', 'sweetheart', 'dear'];

const has = (t, list) => list.filter((p) => new RegExp(`(^|\\W)${p.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(\\W|$)`, 'i').test(t));
const words = (c) => [c.title, c.fact, c.expect, c.next].join(' ').trim().split(/\s+/).filter(Boolean).length;

export function validate(o, ctx) {
  const r = []; // reasons, each tagged soft (regenerate may fix) or hard
  const bad = (code, hard = false) => r.push({ code, hard });
  if (!o || typeof o !== 'object') return { ok: false, reasons: [{ code: 'shape', hard: true }] };
  if (!DECISIONS.includes(o.decision) || !Array.isArray(o.evidence) || typeof o.confidence !== 'number' || o.confidence < 0 || o.confidence > 1
    || !o.card || ['title', 'fact', 'expect', 'next'].some((k) => typeof o.card[k] !== 'string') || typeof o.draft !== 'string') bad('shape', true);
  if (r.length) return { ok: false, reasons: r };
  if (o.evidence.length === 0 && o.decision !== 'none') bad('no-evidence', true); // no evidence, no flag
  if (o.evidence.some((id) => !ctx.evidenceIds.has(id))) bad('invented-evidence', true); // every id must be in the context
  if (o.decision !== 'none' && ![1, 2, 3].includes(o.level)) bad('level');
  if (['slip', 'mismatch'].includes(o.decision) && o.level > escalationLevelNumber(ctx.count)) bad('level-too-high', true);
  const text = [o.card.title, o.card.fact, o.card.expect, o.card.next, o.draft].join(' \n ');
  const card = [o.card.title, o.card.fact, o.card.expect, o.card.next].join(' \n ');
  if (words(o.card) > 40) bad('too-long');
  if (has(text, PITY).length) bad('pity');
  if (has(text, SHAME).length) bad('shame');
  if (has(text, LIE).length) bad('says-lied');
  if (has(text, THREAT).length) bad('threat');
  if (has(text, COMPARE).length) bad('comparison');
  if (has(card, PET).length) bad('pet-name');
  if (has(card, FLATTER).length && ['slip', 'mismatch'].includes(o.decision)) bad('praise-with-slip');
  if (['slip', 'mismatch'].includes(o.decision) && card.includes('!')) bad('exclamation-on-slip');
  if (/^\s*([-*•]|\d+[.)])\s/m.test(card)) bad('list-on-card');
  return { ok: r.length === 0, reasons: r };
}
const escalationLevelNumber = (count) => ({ none: 0, L1: 1, L2: 2, L3: 3 })[escalationLevel(count)] || 1; // a first slip may be L1

// "regenerated once; after that a plain template from 13 is used"
export function resolve(first, second, ctx, template) {
  const a = validate(first, ctx);
  if (a.ok) return { source: 'model', output: first };
  if (a.reasons.some((x) => x.hard) || second === undefined) return { source: 'template', output: template, reasons: a.reasons };
  const b = validate(second, ctx);
  return b.ok ? { source: 'model-retry', output: second } : { source: 'template', output: template, reasons: b.reasons };
}
