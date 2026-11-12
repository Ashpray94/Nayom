# 13. Copy library

Owner role: content designer. Inputs: 07, 08. Every card follows: fact, then what she expects, then one next step. Maximum 40 words per card. These are also the fallback templates when a model answer is rejected.

Placeholders: `{person}`, `{what}`, `{due}`, `{n}`, `{time}`, `{fix}`.

## Banned words and moves

Never: poor you, it's okay, don't worry, no big deal, you got this, amazing, awesome, proud of you, lazy, useless, hopeless, failure, liar, stupid, again?!, "why can't you", sarcasm, exclamation marks on slips, comparisons with other people, threats.

## Gate card (I-01)

| Part | English | Hinglish |
| --- | --- | --- |
| Title | Hold on. | Ruko. |
| Fact | {n} promises due in 48 hours. {what_pushed} is due {due}. | 48 ghante mein {n} promise due hain. {what_pushed} {due} tak hai. |
| Expect | Saying yes puts it at risk. | Haan bolne se wo risk mein aa jayega. |
| Buttons | Yes · Later · No | Haan · Baad mein · Na |
| Link | Why am I seeing this | Ye kyun dikh raha hai |
| Light gate | Does this push anything out? Open and due soon: {n}. | Isse kuch aur peeche toh nahi jayega? Jaldi due: {n}. |
| House rule hit | Your rule says no yes right now. | Tumhara rule kehta hai abhi haan nahi. |

Drafts, after the choice:

| Choice | English | Hinglish |
| --- | --- | --- |
| Later | Can do {fix}, not today. | {fix} tak kar sakta hu, aaj nahi. |
| No | I can't take this on right now. | Abhi ye nahi le sakta. |
| Yes, with load | Yes. It will be ready {fix}. | Haan. {fix} tak ready hoga. |

## Slip card (I-02): Missed

| Level | English | Hinglish |
| --- | --- | --- |
| L1 | {what} for {person} was due {due}. It isn't sent. | {person} ko {what} {due} tak dena tha. Nahi gaya. |
| L2 | {n} missed this week. Each one was a yes you gave in under a minute. | Is hafte {n} miss hue. Har ek mein tumne ek minute se kam mein haan bola. |
| L3 | {n} in a week. Sit with me for three minutes now. | Ek hafte mein {n}. Abhi teen minute mere saath baitho. |
| Buttons | Fix it by… · Release | Itne tak karunga… · Chhod do |

Drafts:

| Choice | English | Hinglish |
| --- | --- | --- |
| Fix | I missed {due}. You'll have it {fix}. | {due} miss ho gaya. {fix} tak mil jayega. |
| Release | I can't deliver {what}. I'm telling you now so you can plan. | {what} nahi de paunga. Abhi bata raha hu taaki tum plan kar sako. |

## Mismatch card

| Level | English | Hinglish |
| --- | --- | --- |
| L1 | You told {person} {claim}. There has been no work on it today. | Tumne {person} ko {claim} kaha. Aaj us par koi kaam nahi hua. |
| L2 | Second time this week what you said didn't match what you did. | Is hafte doosri baar jo kaha aur jo kiya wo match nahi hua. |
| L3 | Three this week. Tell the truth first, then work. | Is hafte teen. Pehle sach batao, phir kaam karo. |
| Draft | I haven't started it. I can have it {fix}. | Maine shuru nahi kiya. {fix} tak de sakta hu. |
| Clash | You told {a} {time_a} and {b} {time_b} for the same thing. | Tumne {a} ko {time_a} aur {b} ko {time_b} kaha, ek hi kaam ke liye. |

## Focus card (I-03)

| English | Hinglish |
| --- | --- |
| {n} minutes in {app}. {what} for {person} is due {due}. Back to it. | {n} minute {app} mein. {person} ka {what} {due} tak hai. Wapas kaam par. |

## Care nudges (I-04)

| Case | English | Hinglish |
| --- | --- | --- |
| Eat | Eat something before the next task. | Agle kaam se pehle kuch khao. |
| Water | Water. Now. | Paani piyo. Abhi. |
| Stand | Two hours seated. Stand up for five minutes. | Do ghante se baithe ho. Paanch minute khade ho jao. |
| Outside | Five minutes outside. | Paanch minute bahar jao. |
| Bedtime 1 | It is {time}. Phone down. | {time} ho gaye. Phone rakho. |
| Bedtime 2 | One hour past. Sleep. | Ek ghanta ho gaya. Sona hai. |

## Respect card

English: "Is this in scope? What does it cost you?"
Hinglish: "Ye scope mein hai? Tumhe isse kya cost hota hai?"

## Avoidance

English: "{person} wrote at {time}. You read it and haven't answered."
Hinglish: "{person} ne {time} par likha. Tumne padha, jawab nahi diya."

## Stretch

| Case | English | Hinglish |
| --- | --- | --- |
| Today's rung | Today: {rung}. One small step. | Aaj: {rung}. Ek chhota kadam. |
| Done with evidence | Done. It's on your proof wall. | Ho gaya. Proof wall par hai. |
| Done, no evidence | Marked. It doesn't count toward the next rung. | Mark ho gaya. Agli rung ke liye nahi ginega. |

## Praise (evidence only)

English: "Kept {n} of {m} this week."
Hinglish: "Is hafte {m} mein se {n} nibhaye."
After three kept in a row: "Three kept in a row." No other praise lines exist.

## Evening sheet

English: "{kept} kept. {missed} missed. {moved} moved with notice. {mismatch} don't match. {gate} reflex yeses stopped."
Hinglish: "{kept} nibhaye. {missed} miss. {moved} bataakar aage badhaye. {mismatch} match nahi hue. {gate} reflex haan roke."

## Support card (SR-06)

"If things feel too heavy, you can talk to someone now. Call 14416. It's free and open at any hour."
Button: Call. Text link: Not now.
Hinglish: "Agar sab bahut bhaari lag raha hai, abhi kisi se baat kar sakte ho. 14416 par call karo. Free hai aur har waqt khula hai."

## Toasts and system messages

| Moment | Text |
| --- | --- |
| Promise logged | Logged for {person}, {due}. |
| Draft promise | Not sure about this one. It's in To approve. |
| Vault write failed | Couldn't save. Trying again. |
| Reader off | Reading is off. Turn on. |
| Paused | Paused until {time}. |
| Private set | {what} is Private. Only time and app are kept. |
| Sync behind | Phone is {n} min behind the PC. |
| Wrong tapped | Noted. Why was it wrong? |

## Wrong sheet reasons (S-20)

Not a promise · Not for me · Already done · Wrong person · Wrong time · Joke or sarcasm · Other

## Setup screen text (F8)

| Step | Text |
| --- | --- |
| Restricted settings | Android blocks this until you allow it. Open App info, tap the three dots, tap Allow restricted settings. |
| Accessibility | Lets me see what you type and what's on screen. Blocked apps are never read. |
| Notifications | Lets me see messages when they arrive. |
| Overlay | Lets me stop you before you say yes. |
| Usage | Lets me see how long you stay in each app. |
| Vault | Choose the Private Folder that holds your vault. |
| Done | Reading is on. Heartbeat OK. |

## Empty states

| Screen | Text |
| --- | --- |
| Today | Nothing due. Check Promises for next week. |
| Promises | No promises yet. Say one out loud and it will be here. |
| People | No one yet. They appear when you talk to them. |
| Proof wall | Nothing kept yet. The first one counts. |

## Rules for new copy

1. Fact first. Then expectation. Then one next step.
2. Name the person and the time.
3. Under 40 words. No lists on a card.
4. No banned words.
5. Match the language of the trigger.

Changes: none yet.
