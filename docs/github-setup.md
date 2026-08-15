# GitHub CLI & Repository Setup Guide

This guide details how to connect and manage KeyNest via the GitHub CLI (`gh`) and Git.

---

## 1. Environment Status (Active)

GitHub CLI (`gh` v2.67.0) is installed and authenticated in this environment via `GH_TOKEN`:
- **Account:** `luciusrockwing`
- **Protocol:** HTTPS
- **Status:** Verified and active (`gh auth status` passing)

---

## 2. Google AI Studio Native Push (Easiest)

If you are using Google AI Studio Build:
1. Click the **Export / GitHub** button in the top menu or settings panel.
2. Authorize your GitHub account to create or sync directly to a repository.

---

## 2. GitHub CLI (`gh`) Setup (Local Workstation)

### Installation
- **macOS (Homebrew):** `brew install gh`
- **Linux (Debian/Ubuntu):**
  ```bash
  type -p curl >/dev/null || (sudo apt update && sudo apt install curl -y)
  curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
  sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
  sudo apt update
  sudo apt install gh -y
  ```
- **Windows (Winget):** `winget install --id GitHub.cli`

### Authentication
Authenticate with your GitHub account:
```bash
gh auth login
```
Select **GitHub.com** > **HTTPS** or **SSH** > follow the browser/token login prompt.

Alternatively, use a Personal Access Token (`GH_TOKEN` or `GITHUB_TOKEN`):
```bash
export GH_TOKEN="ghp_your_token_here"
```

---

## 3. Creating & Linking Remote Repository

Create a new repository and push the local codebase:
```bash
# Create remote repo under your account
gh repo create keynest-android --public --source=. --remote=origin --push

# Or if linking to an existing repository:
gh repo set-default <username>/<repo-name>
git remote add origin https://github.com/<username>/<repo-name>.git
git push -u origin main
```

---

## 4. Triggering Automated APK Release Workflow

To trigger the automated GitHub Actions CI/CD release workflow (`.github/workflows/build-release.yml`):

```bash
# Tag a new release version
git tag v1.0.0
git push origin v1.0.0

# View workflow run status in CLI
gh run list --workflow=build-release.yml
gh run watch
```
The workflow will compile the debug APK and publish a GitHub Release with the downloadable `.apk` asset attached.
