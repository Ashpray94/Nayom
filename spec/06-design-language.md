# 06. Design language

Owner role: visual / UI designer. Inputs: 04, 05, decision D-15 (warm, colourful, cheerful).

## Principles

1. **Warm, not soft.** Sunny colours, firm content. The look is cheerful; the words are plain and direct.
2. **A slip must never hide.** It uses colour, a shape and a word, never colour alone.
3. **Three things, not thirty.** Today shows three tasks. Cards show one decision.
4. **No faces, no mascot.** She is a voice and a colour, not a drawing of a person.
5. **Reuse first.** Cards, list rows, dialogs, toasts and icons from Private Folder are reused and restyled with these tokens. New components only where listed in the last section.

## Colour tokens: light

Contrast figures are measured values, WCAG ratio.

| Token | Hex | Use | Check |
| --- | --- | --- | --- |
| `bg` | `#FFF8EE` | App background (cream) | – |
| `surface` | `#FFFFFF` | Cards | – |
| `ink` | `#2B2118` | Main text | 14.93 on bg, 15.75 on surface |
| `muted` | `#6B5A4A` | Secondary text | 6.25 on bg |
| `marigold` | `#F5A524` | Primary fill, buttons, today highlight | ink on marigold 7.72 |
| `berry` | `#B3265B` | Missed, doesn't match | white on berry 6.28; berry on bg 5.95 |
| `leaf` | `#1F7A4D` | Kept, done | white on leaf 5.32; leaf on bg 5.04 |
| `sky` | `#2563B8` | Info, links, care | white on sky 5.91; sky on bg 5.60 |
| `berry-tint` | `#FBE1EA` | Missed card background | ink 12.79 |
| `leaf-tint` | `#DDF2E6` | Kept card background | ink 13.44 |
| `sky-tint` | `#DCE8FA` | Info background | ink 12.72 |
| `marigold-tint` | `#FFE9B8` | Due-soon background | ink 13.20 |

Rule: marigold is never used for text on light backgrounds, only as a fill with `ink` text.
Marigold and berry are close in brightness (contrast 3.08 between them), so a missed item always carries the "!" shape, a thick left bar and the word "Missed".

## Colour tokens: dark

| Token | Hex | Check |
| --- | --- | --- |
| `bg` | `#1E1812` | – |
| `surface` | `#2A2119` | text on surface 14.40 |
| `ink` | `#FFF3E0` | 16.03 on bg |
| `muted` | `#C9B8A3` | 9.10 on bg |
| `marigold` | `#FFB938` | on bg 10.25; ink-dark on marigold 10.25 |
| `berry` (text) | `#FF7AA8` | 7.20 on bg |
| `leaf` (text) | `#5FD38D` | 9.37 on bg |
| `sky` (text) | `#7FB0FF` | 8.00 on bg |

Dark mode follows the phone's own light and dark setting. Nayom does not ask for location, so it cannot time it by sunset.

## Type

Family: Nunito (open licence, rounded, friendly), bundled in the app. Numbers use tabular figures so due times line up.

| Style | Size / line | Weight | Use |
| --- | --- | --- | --- |
| Display | 32 / 40 | ExtraBold | Date on Today |
| Title | 22 / 28 | Bold | Card headings |
| Body | 16 / 24 | Regular | Everything else |
| Label | 14 / 20 | SemiBold | Buttons, chips |
| Caption | 12 / 16 | Regular | Evidence line, timestamps |

Layouts must hold at 200% font size and at 30% longer text, because Hinglish is often longer than English.

## Space, shape, depth

- 4-point grid. Common steps: 4, 8, 12, 16, 24, 32.
- Screen edge padding 16. Card padding 16. Gap between cards 12.
- Radius: cards 16, sheets 24, buttons 999 (pill), chips 999.
- Depth: flat. A 1-point warm outline `#E8D9C3` in light mode, `#3A2E22` in dark.
- Minimum touch target 48 × 48.
- Primary buttons sit in the bottom third of the screen for one-hand use.

## Icons

Rounded outline set, 24 size, 2 stroke. The Private Folder service already ships Material icons, so the same family is used here.

| Meaning | Icon |
| --- | --- |
| Missed | Triangle with "!" |
| Kept | Check in circle |
| Due soon | Clock |
| Care | Heart |
| Private | Eye with a line |
| Pause | Two bars |
| Support | Phone |

## Motion

- Standard duration 200 ms, ease-out. Cards slide up from the bottom.
- The gate pause is a ring that fills over 5 seconds. With reduced motion on, the ring becomes a number counting 5 to 1.
- One firm haptic tick when a full-screen card appears. No sound.
- Nothing bounces or wiggles.

## Component states (each component has all of these)

default, pressed, disabled, focused (2-point sky ring), loading (loader icon swapped in), error (toast).

## Key screens in detail

### Gate card (I-01), full screen
1. Top: Title "Hold on." (see 13), then the ask in one line: "Ravi · Banner tweak · Today".
2. Middle: a leaf or berry strip. "Open and due in 48 h: 3". "Pushes out: Meena's file, 17:00" in a berry-tint box with the "!" icon.
3. Bottom: three buttons, full width, stacked: Yes, Later, No. All disabled for 5 seconds with the ring above them. A fourth text link: "Why am I seeing this".
4. Back gesture is ignored for 5 seconds.

### Slip card (I-02)
Same shell. Berry bar on the left. Title "Missed". One line of fact. One line of evidence in Caption. Two buttons: "Fix it by…" and "Release". A text link "Wrong" opens S-20.

### Today (S-01)
Display date and day at top. Three task cards under it, each with a person chip, a due time and a state colour. One care card in sky-tint at the bottom. A round Debrief button, marigold, bottom right.

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
| Full-screen interrupt card with pause ring | No existing equivalent |
| Today three-task stack | Specific to Nayom |
| Proof wall entry | Specific to Nayom |
| Heartbeat chip ("Reading: on") | Specific to the readers |

Changes: Removed sunset timing for dark mode, because Nayom has no location permission.
