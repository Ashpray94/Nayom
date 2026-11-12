# Nayom reference rules

Executable version of spec 08 (escalation, fade, support rule, output guard), 09 (ledger, vault) and 10 (signals). Plain Node 22, no dependencies.

Run: `npm test` (38 tests). The Kotlin app must give the same answers for the same inputs; port these tests as its vectors.

## Where the spec was unclear (decided here, please confirm)
- SIG-01 says capture needs a time phrase, and also says no time phrase gives the default due. Implemented: a verb of doing plus either a time phrase or an acceptance phrase. No time phrase means default due.
- Confidence is not defined in the spec. Implemented: 0.25 base, +0.2 verb, +0.25 time phrase, +0.1 acceptance, +0.05 known person. Below 0.6 is a draft.
- Repeated-letter squeeze affects runs of 3 or more only, so "tomorrow" and "hello" stay intact.

## Not yet done
- 24-word recovery phrase (needs a 2048-word list; plan: BIP39 via a vetted package, not hand-rolled).
- Weekly proof, day files, verdict file formats.
