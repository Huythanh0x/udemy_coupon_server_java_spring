# Commit Conventions — Conventional Commits

## Format

```
<type>(<scope>): <short description>

<optional longer body>

<optional footer>
```

## Types

| Type | Use for |
|---|---|
| `feat` | a new feature |
| `fix` | a bug fix |
| `docs` | documentation only (README, ADRs, comments-as-docs) |
| `style` | formatting only, no logic change (whitespace, ktlint fixes) |
| `refactor` | code change that's neither a fix nor a feature — same behavior, different structure |
| `perf` | a change that specifically improves performance |
| `test` | adding or correcting tests, no production code change |
| `build` | build system or dependency changes (Gradle, version catalog, Dockerfile) |
| `ci` | CI configuration/scripts (GitHub Actions workflows, `scripts/`) |
| `chore` | everything else maintenance-shaped that doesn't fit above |
| `revert` | reverting a previous commit |

## Scope

Free text, kept short — the module or area affected. For this repo that's usually one of
the Gradle module names or a cross-cutting concern: `api`, `crawler`, `domain`, `infra`,
`scraper`, `identity`, `notification`, `external-api`, `common`, `ci`. Omit the scope
entirely if the change is broad enough that no single scope fits (`chore: update all
dependencies`).

## Examples

```
feat(api): add search-by-keyword filter to coupon list endpoint
fix(crawler): correct JobRunr config property prefix (org.jobrunr -> jobrunr)
docs(adr): record decision to keep JobRunr on SQL storage over Redis
chore(deps): bump Spring Boot to 3.5.8
refactor(scraper): extract CourseDataExtractor interface
test(identity): add coverage for expired refresh token rotation
ci(actions): drop dead develop branch trigger from ci-cd.yml
```

## Breaking changes

Mark a breaking change with `!` right after the type/scope, and explain it in the footer:

```
feat(api)!: change coupon list response shape to cursor-based pagination

BREAKING CHANGE: clients paginating via `page`/`totalPage` must migrate to the new
`cursor`/`hasNext` response fields.
```

This matters specifically because it's what drives an automatic MAJOR version bump if
you're using changelog/version automation (see `versioning.md`) — don't skip the `!` just
because the API has few consumers today.

## Formatting rules

- Subject line under ~72 characters.
- Subject line: imperative mood, lowercase after the colon, no trailing period —
  `fix(crawler): correct coupon limit interpolation`, not `Fixed the coupon limit bug.`
- If you need to explain *why*, not just *what*, put it in the body (blank line after the
  subject, then free-form prose). The subject answers "what changed"; the body answers
  "why did this need to happen."
- One logical change per commit. A commit that touches five unrelated things should be
  five commits — this doesn't matter much on the branch itself (it'll get squashed), but
  it matters if you ever need to `git revert` or `git bisect` something specific.

## Why bother, if it's all getting squashed anyway

The *branch's* individual commits get squashed away at merge (see `pull-requests.md`),
but the final squashed commit on `main` should itself follow this format — that's the
commit that actually matters for `git log` readability and for auto-generating the
changelog. Following the convention on the branch too is just good practice for your own
sanity while working, and makes writing that final squashed message trivial (often it's
just your best individual commit message, cleaned up).
