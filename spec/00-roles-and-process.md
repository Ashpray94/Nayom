# 00. Roles and process

One person is building Nayom, with Claude playing every role in order. Each role hands a finished, checkable deliverable to the next. A role cannot start until the one before it has passed its exit check.

## Order of roles

| # | Role | Question it answers | Deliverable | Exit check | Status |
| --- | --- | --- | --- | --- | --- |
| 1 | Founder / product strategist | Why build this, for whom, what does success look like? | [01](01-vision-and-problem.md) | Every problem from the owner's notes maps to a feature | Done |
| 2 | UX researcher | Who is the user, what is a real day like? | [02](02-personas.md) | A real day shows each problem area at least once | Done |
| 3 | Product manager | What is in, what is out, in what order, how do we test it? | [03](03-scope-and-features.md) | Every feature has an ID, a phase and a pass/fail test | Done |
| 4 | Information architect | What are the screens and how do they connect? | [04](04-information-architecture.md) | No screen is unreachable; every feature has a home | Done |
| 5 | Interaction designer | What happens, step by step, in each situation? | [05](05-user-flows.md) | One straight main line per flow, branches off it, no dead ends | Done |
| 6 | Visual / UI designer | What does it look like? | [06](06-design-language.md) | Every colour pair passes contrast, every component has states | Done |
| 7 | Brand designer | Who is Nayom as a character? | [07](07-brand-and-identity.md) | Voice, name and logo direction agree with each other | Done |
| 8 | Content designer | What are the exact words? | [13](13-copy-library.md) | Every screen and every escalation level has copy | Done |
| 9 | Agent / AI behaviour designer | How does she think and behave? | [08](08-agent-rules-and-context.md), [10](10-detection-signals.md) | Every flag needs evidence; every rule has a number | Done |
| 10 | Data architect | Where does each fact live? | [09](09-vault-data-model.md) | Every feature reads and writes only defined files | Done |
| 11 | Security and privacy officer | What can go wrong with the data? | [11](11-privacy-safety-boundaries.md) | Every captured thing has a limit, a blocklist entry or a reason | Done |
| 12 | Solution architect | How do the parts fit and talk? | [12](12-architecture-and-sync.md) | Every feature maps to a component and a device | Done |
| 13 | QA lead | How do we try to break it? | [14](14-edge-cases.md), [17](17-test-plan.md) | Every feature has at least one test and one edge case | Done |
| 14 | Delivery manager | In what order do we build, and when is each step done? | [15](15-roadmap.md) | Each phase works on its own and has a done-when test | Done |
| 15 | Release and operations | How is it installed, updated, backed up and fixed? | [18](18-build-release-operations.md) | A clean install and a rollback are both written down | Done |
| 16 | Sceptic / red team | What will not work, and what did we avoid saying? | [16](16-known-limits.md) | Every limit has a stated workaround or an accepted risk | Done |
| 17 | Engineers | Build it | Code, phase by phase | Each phase's done-when test passes on the real devices | Next |

## How roles hand over

- Each file begins with `Owner role:` and `Inputs:` so you can see where it comes from.
- If a later role finds a problem in an earlier file, it fixes that file and writes the change in the file's "Changes" line at the bottom. It does not work around it.
- The engineer role starts with Phase 0 in [15](15-roadmap.md). The first work is changes to Private Folder, not Nayom itself.
