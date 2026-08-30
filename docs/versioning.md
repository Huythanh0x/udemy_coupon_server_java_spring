# Versioning & Changelog

## Semantic Versioning

Format: `MAJOR.MINOR.PATCH` (e.g. `1.4.2`)

| Segment | Bump when |
|---|---|
| **MAJOR** | a breaking change — anything marked `!` in a Conventional Commit (API response shape change, incompatible DB migration, removed endpoint) |
| **MINOR** | new backward-compatible functionality (`feat` commits) |
| **PATCH** | backward-compatible bug fix (`fix` commits) |

Before `1.0.0`: it's fine to stay in `0.x.y` while the service is genuinely pre-stable — a
personal project doesn't need to rush to `1.0.0` just to look mature. Move to `1.0.0` once
it's something you'd consider a real, stable release.

## Tagging a release

```bash
git tag -a v1.4.2 -m "v1.4.2"
git push origin v1.4.2
```

Tag on `main`, at the exact commit you're releasing — not on a feature branch.

## Changelog

Maintain [`CHANGELOG.md`](../CHANGELOG.md) at the repo root, following the
[Keep a Changelog](https://keepachangelog.com) format:

```markdown
# Changelog

## [Unreleased]

## [1.4.2] - 2026-08-30
### Added
- Search-by-keyword filter on the coupon list endpoint (#42)

### Fixed
- JobRunr background worker no longer stalls with jobs stuck in `ENQUEUED` (#51)
```

Group entries under `Added`, `Changed`, `Fixed`, `Removed` (skip empty groups). Reference
the issue number so anyone (including future you) can jump straight to the full context.

## Automating it

Because commits already follow Conventional Commits (`commit-conventions.md`), most of
the changelog can be generated instead of hand-written:

- `standard-version` or `semantic-release` can read commit history since the last tag,
  determine the next version number automatically (feat → minor, fix → patch, `!` →
  major), and generate the changelog section for you.
- Worth adopting once release cadence becomes frequent enough that hand-writing feels
  like real overhead — not necessary to set up on day one.

## Docker image tagging

Both deployables are published as Docker images from a single Docker Hub repository
(`${DOCKER_HUB_USERNAME}/training-coupon-services`), distinguished by tag prefix rather
than by separate repositories — see `.github/workflows/ci-cd.yml`:

| Tag pattern | When it's produced |
|---|---|
| `api-<branch>` / `crawler-<branch>` | Every push to a branch |
| `api-pr-<number>` / `crawler-pr-<number>` | Every PR (image built, not pushed — see `ci-cd.md`) |
| `api-v<version>` / `crawler-v<version>` | Pushing a `vX.Y.Z` git tag — mirrors the SemVer tag exactly |
| `api-<major>.<minor>` / `crawler-<major>.<minor>` | Same, floating major.minor tag |
| `api-latest` / `crawler-latest` | Every push to `main` |

Keep the git tag and the resulting `api-vX.Y.Z`/`crawler-vX.Y.Z` image tags in sync — that
floating correspondence is what lets a running container be traced back to its exact
source commit via `git tag`.
