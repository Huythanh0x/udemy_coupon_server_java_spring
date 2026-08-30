# Architecture Decision Records (ADRs)

## What counts as "a real decision"

Not every choice needs an ADR — that turns it into a chore you'll abandon after the
second one. Write one when the decision has a genuine tradeoff you'd want to justify
later, in either direction. A useful filter:

> If someone (an interviewer, a future collaborator, future-you in a year) asked
> "why did you do it this way instead of the obvious alternative?" — would you want a
> written answer already sitting there instead of trying to reconstruct your reasoning
> from memory?

If yes, write the ADR. If the decision was essentially arbitrary or trivially reversible
(which Gradle module to put a util in, which HTTP client library to use for a one-off
call), skip it.

Examples that clear the bar: choosing JobRunr's SQL storage backend over adding a Redis
storage provider, picking a specific pagination strategy for the coupon list, picking a
specific auth/token-refresh strategy, choosing squash merge as the default merge
strategy (the doc you're reading came from exactly this kind of reasoning).

## Where they live

`/docs/adr/` in the relevant repo. One file per decision.

## Naming

```
NNNN-short-title.md
```
Sequential, zero-padded, never reused even if a decision is later superseded (see
Status below) — e.g. `0001-jobrunr-sql-storage-over-redis.md`,
`0002-squash-merge-default.md`.

## Template

```markdown
# NNNN: Title

## Status
Proposed | Accepted | Deprecated | Superseded by NNNN

## Context
What situation or problem led to needing this decision. What were the real constraints
(time, existing code, team size, target platform)?

## Decision
What you actually decided, stated plainly.

## Consequences
What this makes easier, what it makes harder, what you're explicitly trading away. Good
ADRs name the downside honestly, not just the upside.
```

## Status lifecycle

- **Proposed** — you're still deciding, written down to think it through
- **Accepted** — this is the actual decision in effect
- **Deprecated** — no longer relevant (e.g. the feature it applied to was removed)
- **Superseded by NNNN** — a later ADR replaced this decision; link forward to it, and
  update the newer ADR to link back

Never delete or silently edit an old ADR to reflect a new decision — write a new one and
mark the old one Superseded. The history of *changing your mind* is often as valuable as
the decision itself.

## Worked example

See [`adr/0001-jobrunr-sql-storage-over-redis.md`](adr/0001-jobrunr-sql-storage-over-redis.md)
for a real ADR from this repo's own history, following the template above.
