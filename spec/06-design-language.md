# 06. Design language

Owner role: visual / UI designer. Inputs: 04, 05, D-15 (cheerful), D-22 to D-27. Reference screens: `/design/screens`.

## Principles

1. **Calm look, firm words.** Thin type, quiet surfaces, mission-control layout (D-27). Bold appears only where urgency matters: status words and buttons.
2. **A slip must never hide.** It uses colour, a shape and a word, never colour alone.
3. **Three things, not thirty.** Today shows three tasks. Cards show one decision.
4. **One face only: the icon.** She has a robot face on the app icon and nowhere else. Inside the app she is a voice and a colour, not a drawing. No mascot in cards, empty states or notifications.
5. **Reuse first.** Cards, list rows, dialogs, toasts and icons from Private Folder are reused and restyled with these tokens. New components only where listed in the last section.

## Colour tokens: light (default)

Teal family (D-22). Light is the default (D-23). Contrast figures are measured values, WCAG ratio.

| Token | Hex | Use | Check |
| --- | --- | --- | --- |
| `bg` | `#F2F8F7` | App background | – |
| `surface` | `#FFFFFF` | Cards | – |
| `ink` | `#12302F` | Main text | 13.12 on bg, 14.10 on surface |
| `muted` | `#4B6663` | Secondary text | 5.78 on bg |
| `teal` | `#23706B` | Primary fill, buttons, links, today highlight | white on teal 5.83; teal on bg 5.43 |
| `teal-tint` | `#D6ECE8` | Selected, info background | ink 11.43 |
| `mint` | `#5FDDB0` | Glow and accents only (icon, eyes), never text | – |
| `amber` | `#F5A524` | Due soon. Fill only, with `ink` text | ink on amber 6.91 |
| `amber-tint` | `#FFE9B8` | Due-soon card background | ink 11.82 |
| `berry` | `#B3265B` | Missed, doesn't match | white on berry 6.28; berry on bg 5.84 |
| `berry-tint` | `#FBE1EA` | Missed card background | ink 11.45 |
| `leaf` | `#3F7A1F` | Kept, done | white on leaf 5.23; leaf on bg 4.87 |
| `leaf-tint` | `#E3F1D8` | Kept card background | ink 11.98 |
| `sky` | `#2563B8` | Care items | white on sky 5.91; sky on bg 5.50 |
| `sky-tint` | `#DCE8FA` | Care card background | ink 11.39 |

Rules:
- Amber is never used for text on light backgrounds, only as a fill with `ink` text.
- Teal, leaf and sky are close in brightness (contrast 1.01 to 1.11 between them). A state is never shown by colour alone: Kept carries a check icon and the word "Kept"; Missed carries the "!" icon and the word "Missed"; Care carries the heart icon.
- Amber and berry are also close (3.08), so Due soon carries a clock icon and its time.

## Colour tokens: dark

| Token | Hex | Check |
| --- | --- | --- |
| `bg` | `#0B1B1D` | – |
| `surface` | `#122A2C` | – |
| `ink` | `#E8F5F2` | 15.79 on bg, 13.47 on surface |
| `muted` | `#A9C4C0` | 9.54 on bg |
| `teal` | `#5FD0C0` | on bg 9.48; ink-dark on teal 9.48 |
| `amber` | `#FFB938` | on bg 10.30 |
| `berry` (text) | `#FF7AA8` | 7.24 on bg |
| `leaf` (text) | `#9FD66B` | 10.35 on bg |
| `sky` (text) | `#7FB0FF` | 8.04 on bg |

Appearance setting: Light (default), Dark, Follow phone. Nayom starts in Light and never switches on its own (D-23). It does not ask for location, so it cannot time dark by sunset.

## Type (D-25)

Two families, both Google Fonts, bundled in the app (no network fetch).

- **Cormorant Garamond** (serif, thin, calligraphic): headings only. Regular and italic. Roman for firm moments ("Hold on.", "Missed."), italic for warm ones (date on Today and Evening).
- **Sora** (sans, geometric, futuristic): everything else. Regular (400) for body, 200 for large numerals, 700 for urgency (status words, buttons).

| Style | Family | Size / line | Weight | Use |
| --- | --- | --- | --- | --- |
| Hero | Cormorant, roman | 72 / 72 | 500 | Gate and Slip title |
| Display | Cormorant, italic | 46 / 48 | 500 | Date on Today and Evening |
| Heading | Cormorant, roman | 56 / 58 | 500 | Setup step title |
| Numeral | Sora | 56 to 88 | 200 | Countdown, "2 of 3", "42 min" |
| Body | Sora | 16 to 18 / 1.45 | 400 | Everything else |
| Status | Sora, caps, +0.16em | 11 | 700 | MISSED, KEPT, DUE, AT RISK |
| Label | Sora, caps, +0.18em | 11 | 400 | Card labels, tab names, links |
| Button | Sora, caps, +0.2em | 13 | 700 filled, 400 outline | CTAs |

Rules: no more than one serif element per screen. Numerals use tabular figures. Layouts hold at 200% font size and with 30% longer text (Hinglish).

## Surfaces and shape (D-27)

- Background is a flat gradient. No grid, no texture, no illustration, no mascot.
- Group content in one **card** per idea: radius 22, padding 24 (list cards 4 × 20). Light: white, soft shadow. Dark: 5% white fill with a 1-point inset line.
- Inside a card, separate items with a 1-point hairline, not more boxes.
- Screen edge padding 28. Cards align to the same edge as the text above them.
- Buttons: radius 6, height 56, full width, caps label. Primary is filled teal. Secondary is outline. Disabled is 45% opacity.
- Status icons sit in a 44 square tile, radius 12, tinted with the status colour at 13%.
- Each screen has one focal point: a hero number or a hero word.
- Minimum touch target 48 × 48. Primary buttons sit in the bottom third.

## Icons (D-26)

Own icon set, drawn for Nayom, not a pack. Files in `/design/icons/svg`.

- Grid 48, live area 40, stroke 2.25 (matches Sora's line weight at 24 px), round caps and joins, corner radius about 5.
- Active tab version: same icon with a 16% tint fill.
- One idea per icon, with one charm detail. Add an icon only when a screen needs it.

| Icon | Meaning | Idea |
| --- | --- | --- |
| today | Today tab | Sunrise: the day beginning |
| promises | Promises tab | Open hand offering a heart |
| people | People tab | Me in front, another behind |
| notes | Notes tab | Note with a pencil |
| mic | Debrief | Capsule with grille and stand |
| check | Kept | Check in a circle |
| missed | Missed, at risk | "!" in a circle |
| clock | Due soon | Stopwatch with the time left shaded |
| shield | Never read, private | Shield with keyhole |

Bottom bar: four tabs (Today, Promises, People, Notes), icon above label, active tab in teal with the tinted icon.

## Motion

- Standard duration 200 ms, ease-out. Cards slide up from the bottom.
- The gate pause is a thin line that fills over 5 seconds, with the text "Yes and Later unlock in N s". With reduced motion on, the line is replaced by the number counting 5 to 1.
- One firm haptic tick when a full-screen card appears. No sound.
- Nothing bounces or wiggles.

## Component states (each component has all of these)

default, pressed, disabled, focused (2-point sky ring), loading (loader icon swapped in), error (toast).

## Key screens in detail

Reference renders, light and dark: `/design/screens`.

### Today (S-01)
Date in italic serif, then one hero card: the next promise, its countdown in thin numerals, due time. Below it one list card with missed and kept items, each with a tinted icon tile and a status word. Debrief is a filled button with the mic icon. Bottom bar with four tabs.

### Gate card (I-01), full screen
1. "Hold on." in the hero serif. One line: "Ravi asks: Banner tweak, today."
2. One card with two facts: "3 promises due in 48 h", and "Pushes out Meena's file, 17:00 · AT RISK" with the missed icon tile.
3. A thin 5 second line with "Yes and Later unlock in 3 s". Buttons stacked: Yes and Later (outline, disabled until unlocked), No (filled). Text link "Why am I seeing this".
4. Back gesture is ignored for 5 seconds.

### Slip card (I-02)
"Missed." in the hero serif. One line of fact. One card: "Late" with the number in thin numerals ("42 min"), a hairline, one evidence line in Body ("No file saved or sent since 09:15"). Buttons: "Fix it by…" (filled), "Release" (outline). Text link "Wrong" opens S-20.

### Evening sheet
Date in italic serif. One hero card: "Kept 2 of 3", a thin progress line and "9 of 11 this week". One card with three counts: Missed, Moved, Reflex yes. A closing line ("Phone down.") and a Done button.

### Setup (F8)
Six progress segments at the top. Step title in the serif. One sentence of what the permission does. One card "Never read" with the shield icon and the list of blocked app types. Buttons: Open settings, Back.

### Lock-screen widget (I-05)
Date, day, the next due item and time. No private text, because a lock screen is public.

## Accessibility

- Every colour pair above passes 4.5:1 for text; large text passes 3:1.
- Every state has a text label and a shape, not only a colour.
- Screen reader names for every control; the gate announces "Pause, 5 seconds" then "Buttons ready".
- Works with one hand, with large fonts, and without sound.

## New components (everything else is reused)

| Component | Why it is new |
| --- | --- |
| Full-screen interrupt card with pause line | No existing equivalent |
| Today three-task stack | Specific to Nayom |
| Proof wall entry | Specific to Nayom |
| Heartbeat chip ("Reading: on") | Specific to the readers |

Changes: Removed sunset timing for dark mode, because Nayom has no location permission. D-25 to D-27 replaced Nunito, Material icons, the warm outline and the left-bar slip card.

## Premium system (D-28, D-29)

Quiet luxury. Rolls-Royce restraint, Tesla minimalism: few things, each one perfect.

**Hero rule (D-28).** Every screen has one hero and it owns the screen. It is centred, has at least 40 px of air on every side, and nothing else competes with it. Today's hero is the Dial: a 3 px hairline ring that fills as the deadline nears (24 h window), calm teal, berry in the last hour, with the countdown in Sora ExtraLight inside it. Evening's hero is the serif line "Phone down."

**Illustration language (D-29).** One character, the robot mum: slim head with a bun and a teal hair cap, soft eyes with a highlight dot, ear lights, eyes drawn as a comma whose tail is the brow (D-30), and one long sweep of a body that tapers into a curled tail (no legs). She cradles the scene object in one arm. One fine uniform stroke (1.6 on a 240 grid), round caps, no fills on her. Each scene adds one concept fill in a single accent at 38 % (sun = amber, heart = berry, moon = blue), and only when that object is the idea. Scenes: clear sky, all kept, nothing owed, no one yet, blank page, quiet day. Used for empty states and celebrations only, never as decoration. Source: `design/illustrations/ill6.py`; the app draws a line layer in ink and a fill layer in the accent, so dark mode needs nothing extra.

**Serif.** Short headings, empty-state lines, celebrations and completions. Never body, never buttons, never numbers.

**Spacing.** 8 px grid. Edge 24, between ideas 24, inside cards 24.

**Surfaces.** Tone, not shadow, separates layers. Hero radius 28, cards 22, buttons 6.
