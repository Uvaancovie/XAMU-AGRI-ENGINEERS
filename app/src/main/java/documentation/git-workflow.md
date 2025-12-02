# 🔄 Git Workflow Guide

## Table of Contents
- [Pulling Latest Changes](#pulling-latest-changes)
- [Common Scenarios](#common-scenarios)
- [Handling Conflicts](#handling-conflicts)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Pulling Latest Changes

When you see a notification that your branch has recent pushes (e.g., "feature/client-crud-and-project-filtering had recent pushes 4 seconds ago"), follow these steps to update your local copy.

### Quick Start (Clean Working Tree)

If you have no uncommitted changes:

```bash
# Check current status
git status

# Fetch latest changes from remote
git fetch origin

# Switch to the target branch (if not already on it)
git checkout feature/client-crud-and-project-filtering

# Pull the latest changes
git pull origin feature/client-crud-and-project-filtering
```

### One-Line Command (if already on the branch)

```bash
git pull origin feature/client-crud-and-project-filtering
```

---

## Common Scenarios

### Scenario 1: Clean Working Tree

**When:** You have no uncommitted changes and want to pull the latest updates.

```bash
# Verify you have a clean working tree
git status

# Fetch and pull
git fetch origin
git checkout feature/client-crud-and-project-filtering
git pull origin feature/client-crud-and-project-filtering
```

**Expected output:**
```
Updating abc1234..def5678
Fast-forward
 app/src/main/java/SomeFile.kt | 10 ++++++++--
 1 file changed, 8 insertions(+), 2 deletions(-)
```

---

### Scenario 2: You Have Uncommitted Local Changes

**When:** You've made local changes but haven't committed them yet.

**Option A: Commit your changes first (Recommended)**

```bash
# Check what files you've changed
git status

# Add your changes
git add .

# Commit with a descriptive message
git commit -m "WIP: Your description of changes"

# Now pull the latest changes
git pull origin feature/client-crud-and-project-filtering
```

**Option B: Stash your changes temporarily**

```bash
# Save your uncommitted changes
git stash push -m "WIP: Description of what I was working on"

# Pull the latest changes
git pull origin feature/client-crud-and-project-filtering

# Restore your changes
git stash pop
```

**When to use stash:**
- Your changes are exploratory/experimental
- You want to test something before committing
- You need to quickly switch contexts

---

### Scenario 3: Pull with Rebase (Linear History)

**When:** You want to maintain a clean, linear commit history.

```bash
# Pull with rebase instead of merge
git pull --rebase origin feature/client-crud-and-project-filtering
```

**Benefits:**
- Cleaner commit history
- Easier to understand project timeline
- Preferred for feature branches

**Note:** If conflicts occur, see [Handling Conflicts](#handling-conflicts) below.

---

### Scenario 4: Force Update (⚠️ Destructive)

**When:** You want to completely replace your local branch with the remote version (discards local commits).

**⚠️ WARNING:** This will delete any local commits that haven't been pushed!

```bash
# Fetch latest remote information
git fetch origin

# Switch to your branch
git checkout feature/client-crud-and-project-filtering

# Reset your branch to match remote exactly
git reset --hard origin/feature/client-crud-and-project-filtering
```

**Only use this when:**
- You're certain you don't need your local changes
- Your local commits are already on remote and you want to sync
- You're troubleshooting and need a clean slate

---

## Handling Conflicts

### During Normal Pull

If you get conflicts during a pull:

```bash
# Git will show conflicting files
# Conflicting files will contain markers like:
# <<<<<<< HEAD
# Your changes
# =======
# Remote changes
# >>>>>>> branch-name

# Open each conflicting file and resolve manually
# Remove the markers and keep the correct code

# After resolving all conflicts:
git add .
git commit -m "Merge remote changes, resolved conflicts"
```

### During Rebase

If conflicts occur during `git pull --rebase`:

```bash
# Resolve conflicts in each file as above

# After resolving:
git add .
git rebase --continue

# If you want to abort the rebase:
git rebase --abort
```

---

## Best Practices

### 1. Check Before You Pull

Always check your status first:

```bash
git status
git branch -vv  # Shows tracking branch info
```

### 2. Fetch Before Pull

Fetching separately lets you see what's coming:

```bash
# See what's new without changing your files
git fetch origin

# Compare your branch with remote
git log HEAD..origin/feature/client-crud-and-project-filtering --oneline

# Now pull if you're ready
git pull origin feature/client-crud-and-project-filtering
```

### 3. Set Up Branch Tracking

Make pulling easier by setting up tracking:

```bash
# When creating a new branch from remote
git checkout -b feature/client-crud-and-project-filtering origin/feature/client-crud-and-project-filtering

# Or set tracking for existing branch
git branch --set-upstream-to=origin/feature/client-crud-and-project-filtering feature/client-crud-and-project-filtering

# Now you can just use:
git pull
```

### 4. Communicate with Team

Before pulling major changes:
- Check team chat or PR descriptions
- Review what's being merged
- Coordinate if working on same files

### 5. Pull Frequently

- Pull at the start of each work session
- Pull before starting new features
- Pull before creating pull requests

---

## Troubleshooting

### "Your branch is behind 'origin/...' by N commits"

```bash
# Simply pull to catch up
git pull origin feature/client-crud-and-project-filtering
```

### "Your local changes would be overwritten by merge"

You have uncommitted changes that conflict. Options:

```bash
# Option 1: Commit them
git add .
git commit -m "My changes"
git pull

# Option 2: Stash them
git stash
git pull
git stash pop
```

### "fatal: refusing to merge unrelated histories"

Rare case when branches don't share history:

```bash
git pull origin feature/client-crud-and-project-filtering --allow-unrelated-histories
```

### "Could not resolve host 'github.com'"

Network/connectivity issue:

```bash
# Check internet connection
ping github.com

# Verify remote URL
git remote -v

# Try again
git fetch origin
```

### "fatal: couldn't find remote ref"

Branch doesn't exist on remote:

```bash
# List all remote branches
git branch -r

# Fetch latest remote information
git fetch origin

# Create local branch from remote if it exists
git checkout -b feature/client-crud-and-project-filtering origin/feature/client-crud-and-project-filtering
```

### Authentication Issues

If asked for credentials repeatedly:

```bash
# For HTTPS (cache credentials for 1 hour)
git config --global credential.helper cache

# Or use SSH keys (recommended)
# See GitHub docs: https://docs.github.com/en/authentication
```

---

## Quick Reference Card

| Scenario | Command |
|----------|---------|
| Pull with clean working tree | `git pull origin <branch>` |
| Pull with uncommitted changes | `git stash && git pull && git stash pop` |
| Pull with rebase | `git pull --rebase origin <branch>` |
| Force update to match remote | `git fetch && git reset --hard origin/<branch>` |
| Check what's new on remote | `git fetch && git log HEAD..origin/<branch>` |
| Set up branch tracking | `git branch --set-upstream-to=origin/<branch>` |
| Abort merge during conflicts | `git merge --abort` |
| Abort rebase during conflicts | `git rebase --abort` |

---

## Related Documentation

- [Development Guide](./development-guide.md) - Overall development workflow
- [Testing Guide](./testing.md) - How to run tests after pulling changes
- [Architecture](./architecture.md) - Understanding the codebase structure

---

## Need Help?

If you're stuck:
1. Check `git status` to understand current state
2. Review this guide for your specific scenario
3. Ask a team member if you're unsure about conflicts
4. When in doubt, create a backup branch: `git branch backup-$(date +%s)`

---

**Last Updated:** 2025-10-27  
**Maintainer:** Development Team
