# Branching

## Why trunk-based, not GitFlow

GitFlow's overhead (long-lived `develop`, release branches, hotfix branches) solves
coordination problems that exist on multi-person teams with staged releases. As a solo
dev, that overhead has no payoff — there's no one else to coordinate with, and it adds
branches you have to remember to keep in sync. Simplified trunk-based development gives
the same safety (nothing ships without review/CI) with far less bookkeeping.

The rule: **`main` is always deployable.** Every other branch is short-lived and exists
only until its PR merges.

## Naming convention

```
<type>/<issue-number>-<short-slug>
```

`<type>` matches the issue's type label: `feature`, `fix`, `chore`, `docs`, `refactor`, `test`, `design`.

Examples:
```
feature/15-add-coupon-search-endpoint
fix/12-jobrunr-property-prefix
chore/20-bump-spring-boot-version
docs/23-add-adr-for-jobrunr-storage
refactor/30-extract-course-data-extractor-interface
```

The issue number in the branch name is what lets you (or GitHub) trace a branch back to
its issue at a glance, without opening anything.

## Lifecycle

1. Branch from `main` — always from the latest `main`, not from another feature branch.
2. Work on it. Commit as often as you want on the branch itself (WIP commits are fine
   here — they get squashed away at merge, see `pull-requests.md`).
3. Open a PR early if the work will take more than a session — an open (even draft) PR
   is a status signal on the board, not just a final artifact.
4. Keep it short-lived. Days, not weeks. If it's dragging, the issue was probably `size:
   13` and should have been split into smaller linked issues first.
5. If `main` moves significantly while your branch is open, rebase your branch onto the
   latest `main` before merging (`git rebase main` from the feature branch) — this keeps
   the eventual squash-merge clean and avoids surprise conflicts at merge time.
6. Merge (squash), then delete the branch. GitHub can auto-delete on merge — turn that
   setting on so stale branches don't accumulate.

## What NOT to do

- Don't commit directly to `main`, even for "tiny" fixes — a one-line fix still gets a
  branch and a PR, both so CI runs against it and so there's a record of why.
- Don't let a branch outlive its issue's relevance — if priorities shifted and the branch
  is stale, either finish it, close the PR explicitly with a note why, or delete it. A
  zombie branch sitting open for a month is worse than no branch.
- Don't branch from another feature branch (branch-off-a-branch) — it couples two units of
  work together and makes the eventual merge order confusing. Branch from `main` every time.
- Don't reintroduce a long-lived `develop` branch — the repo's CI previously triggered on
  both `main` and `develop`, but `develop` never actually existed as a real branch; that
  trigger has been removed (see `ci-cd.md`) so the workflow config matches this doc.
