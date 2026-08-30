#!/usr/bin/env bash
# Enables branch protection on `main`: PR required before merge, CI required to pass,
# branch auto-delete on merge. NOT run automatically — review, then run yourself:
#   ./scripts/gh-setup-branch-protection.sh
#
# Requires: gh CLI, authenticated, repo admin access.
#
# Prerequisite: push .github/workflows/ci-cd.yml to `main` at least once first, so the
# "Build and Push Docker Images" check name below actually exists for GitHub to require —
# `gh api` will reject unknown check names for `contexts` on some setups otherwise.
set -euo pipefail

REPO="$(gh repo view --json nameWithOwner -q .nameWithOwner)"
echo "Configuring branch protection on ${REPO}#main"

gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "repos/${REPO}/branches/main/protection" \
  --input - <<'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["Build and Push Docker Images"]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 0
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
EOF

# Auto-delete head branches after merge, and restrict the merge button to squash-only
# (repo-level settings, separate endpoint) — matches docs/pull-requests.md's "squash is
# the only strategy you actually want" so it's not just a personal habit to remember.
gh api --method PATCH "repos/${REPO}" \
  -f delete_branch_on_merge=true \
  -f allow_squash_merge=true \
  -f allow_merge_commit=false \
  -f allow_rebase_merge=false

echo "Done. Review under: Settings > Branches > main"
