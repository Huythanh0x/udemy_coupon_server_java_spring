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
- **Multi-repo**: a single GitHub Projects (v2) board ("Course Deals",
  `https://github.com/users/Huythanh0x/projects/4`) pulls issues from both this repo
  (`course_deals_backend`) and `course_deals_ComposeMultiplatform` — one board, not one
  per repo, so "what's next" has a single answer instead of two. Set up via
  [`scripts/gh-setup-project-board.sh`](../scripts/gh-setup-project-board.sh) (run once
  from each repo to add it as a source — already done for both as of 2026-08-30).
- **Auto-add is UI-only**: getting new issues/PRs onto the board isn't automatic just
  because a repo is linked as a source — that only makes its issues *eligible*. Turn on
  **Auto-add to project** per repo (Project → `⋯` → Workflows → Auto-add to project →
  add each repo with a filter, e.g. `is:issue` and `is:pull-request`) — there's no
  `gh`/GraphQL way to configure this, it's a one-time manual step in the Projects UI.
  Without it, `new-ticket`'s `--project` flag (see below) still adds each issue
  individually at creation time, so the board stays accurate either way — auto-add just
  covers issues created some other way (via the web UI, etc.).
- **Automation worth turning on**: auto-move to "In Review" when a linked PR opens,
  auto-move to "Done" when the linked issue closes. Less manual dragging, and the board
  stays accurate even on days you forget to update it by hand.

## Sub-issues are intentionally excluded from the board

Only top-level issues represent a unit of work on the board — a sub-issue's parent is
the card, not each child individually. GitHub's Sub-issues feature **auto-adds a
sub-issue to the board the moment its parent is already a board item**, regardless of
how the sub-issue itself was created or whether it was explicitly added — this isn't
something `--project`/auto-add settings can prevent, since it's tied to the parent-child
relationship itself.

`new-ticket` handles this by never passing `--project` when creating a sub-issue
(`--parent <N>`), then checking whether GitHub cascaded it onto the board anyway and
removing it (`gh project item-delete`) if so — see that skill for the exact commands.
If a sub-issue ever ends up on the board some other way (e.g. added manually, or a
parent gets added to the board after its children already exist — this happened once
when the board was first backfilled from pre-existing issues), the fix is the same:
look up its project item id and `gh project item-delete` it.

## Daily execution handoff

The board is the planning layer, not the daily-execution layer — whatever you use to
track "what am I doing today" is a separate, personal concern outside this repo's docs.
Each day (or each planning session):

1. Look at **Ready** and **In Progress**.
2. Pick what you'll actually touch today.
3. Pull those specific items into your own daily-execution tool of choice.
4. Work from that day-level list — not from the board directly — so "what am I doing
   right now" is a short, concrete list instead of a scroll through the whole backlog.
