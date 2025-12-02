# 🧰 Development Guide

## Code Style
- Ktlint + Detekt must pass
- Kotlin naming conventions
- Document repositories & ViewModels

## Git Flow
`feature/*` → `dev` → `staging` → `main`

### Pulling Latest Changes
When you need to update your local branch with remote changes, see our comprehensive guide:
- **[Git Workflow Guide](./git-workflow.md)** - Complete guide for pulling changes, handling conflicts, and best practices

**Quick command for pulling:**
```bash
git pull origin feature/your-branch-name
```

For detailed scenarios and troubleshooting, refer to the [Git Workflow Guide](./git-workflow.md).

## Testing
```bash
./gradlew test
