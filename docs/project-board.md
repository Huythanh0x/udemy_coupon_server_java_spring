# Project Board

## Columns

```
Backlog → Ready → In Progress → In Review → Done
```

| Column | Meaning | Entry condition |
|---|---|---|
| **Backlog** | Not yet scoped or prioritized | Anything, unfiltered |
| **Ready** | Prioritized, scoped small enough to actually start | Must have a priority label and NOT be `size: 13` (split first) |
| **In Progress** | Actively being worked | A branch exists for it |
| **In Review** | PR open | PR link visible on the card |
| **Done** | Merged AND deployed | Not just merged — deployed and verified |

## Working the board

- **One or two cards in "In Progress" at a time**, even though it's just you. More than
  that and it stops being a signal of what you're actually doing right now and becomes a
  second backlog.
- **Multi-repo**: a single GitHub Projects (v2) board pulls issues from both this repo
  (`course_deals_backend`) and `course_deals_ComposeMultiplatform` — one board, not one
  per repo, so "what's next" has a single answer instead of two. Not created yet — see
  [`scripts/gh-setup-project-board.sh`](../scripts/gh-setup-project-board.sh); it needs
  both repos added as sources, so run the "add source" step from wherever you have the
  client repo checked out too.
- **Automation worth turning on**: auto-move to "In Review" when a linked PR opens,
  auto-move to "Done" when the linked issue closes. Less manual dragging, and the board
  stays accurate even on days you forget to update it by hand.

## Daily execution handoff

The board is the planning layer, not the daily-execution layer — whatever you use to
track "what am I doing today" is a separate, personal concern outside this repo's docs.
Each day (or each planning session):

1. Look at **Ready** and **In Progress**.
2. Pick what you'll actually touch today.
3. Pull those specific items into your own daily-execution tool of choice.
4. Work from that day-level list — not from the board directly — so "what am I doing
   right now" is a short, concrete list instead of a scroll through the whole backlog.
