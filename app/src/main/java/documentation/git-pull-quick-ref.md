# 🚀 Git Pull Quick Reference

**Need to pull latest changes from your branch?** Here's what to do:

---

## 🎯 Most Common Case

```bash
git pull origin feature/client-crud-and-project-filtering
```

Replace `feature/client-crud-and-project-filtering` with your branch name.

---

## ⚡ Step-by-Step (Safe Method)

```bash
# 1. Check your current state
git status

# 2. Fetch latest changes
git fetch origin

# 3. Switch to your branch (if needed)
git checkout feature/client-crud-and-project-filtering

# 4. Pull the changes
git pull origin feature/client-crud-and-project-filtering
```

---

## 💾 If You Have Uncommitted Changes

**Option 1: Commit first (Recommended)**
```bash
git add .
git commit -m "WIP: My changes"
git pull origin feature/client-crud-and-project-filtering
```

**Option 2: Stash temporarily**
```bash
git stash push -m "WIP: My work in progress"
git pull origin feature/client-crud-and-project-filtering
git stash pop
```

---

## 🔧 Common Issues

| Problem | Solution |
|---------|----------|
| "Your local changes would be overwritten" | Commit or stash your changes first |
| "fatal: couldn't find remote ref" | Branch doesn't exist remotely - check branch name |
| "Conflicts occurred during merge" | Edit conflicting files, then `git add .` and `git commit` |

---

## 📚 Need More Help?

See the [complete Git Workflow Guide](./git-workflow.md) for detailed scenarios and troubleshooting.

---

**Pro Tip:** Pull frequently (daily) to avoid large merge conflicts!
