# Git & GitHub Complete Guide
## Basic to Advanced — Windows PowerShell

A practical reference for learning Git, managing GitHub repositories, collaborating on projects, and fixing common Git errors.

## Table of Contents

1. Git Basics
2. Installation and Configuration
3. Creating a Repository
4. Git Status and Changes
5. Add, Commit and Push
6. Git Branches
7. Merge and Rebase
8. GitHub Remote Commands
9. Pull and Fetch
10. Undo and Restore Changes
11. Reset and Revert
12. Stash
13. Cherry-Pick
14. Tags and Releases
15. Logs and History
16. Merge Conflicts
17. .gitignore
18. GitHub Collaboration and Pull Requests
19. Advanced Git Commands
20. Common Errors and Solutions
21. Recovering from Mistakes
22. Complete Daily Workflow
23. Git Cheat Sheet

---

## 1. Git Basics

Git is a version-control system that tracks changes in your code.

GitHub is an online service where Git repositories can be hosted and shared.

| Term | Meaning |
|---|---|
| Repository | Project tracked by Git |
| Working directory | Your current project files |
| Staging area | Changes selected for the next commit |
| Commit | A saved snapshot of changes |
| Branch | An independent line of development |
| Merge | Combines changes from branches |
| Remote | A repository hosted elsewhere, such as GitHub |
| Clone | Downloads a repository and its history |
| Push | Sends local commits to a remote |
| Pull | Fetches and integrates remote changes |
| Fetch | Downloads remote information without integrating it |
| Pull request | A GitHub proposal to merge changes |

### Basic Git workflow

```text
Edit files
    ↓
git add
    ↓
git commit
    ↓
git push
    ↓
GitHub
```

---

## 2. Installation and Configuration

### Check whether Git is installed

```powershell
git --version
```

Example output:

```text
git version 2.x.x.windows.x
```

### Set your Git username

```powershell
git config --global user.name "Your Name"
```

### Set your Git email

```powershell
git config --global user.email "your-email@example.com"
```

Use the email associated with your GitHub account if you want commits linked to that account.

### Check your configuration

```powershell
git config --global --list
```

### View a particular setting

```powershell
git config --global user.name
git config --global user.email
```

### Set the default branch name

```powershell
git config --global init.defaultBranch main
```

### Enable colored output

```powershell
git config --global color.ui auto
```

### Open Git configuration in an editor

```powershell
git config --global --edit
```

### Remove a configuration setting

```powershell
git config --global --unset user.name
```

This removes that global username setting. Configure it again if necessary.

---

## 3. Creating a Repository

### Initialize Git in an existing project

```powershell
cd "E:\Coding\MyProject"
git init
```

`git init` creates a local Git repository in the current directory.

### Check the current directory

```powershell
Get-Location
```

### Clone a GitHub repository

```powershell
git clone https://github.com/USERNAME/REPOSITORY.git
```

Example:

```powershell
git clone https://github.com/ashish2024-28/DERP_Backend_Digital-Education-Records-Platform.git
```

### Clone into a custom folder

```powershell
git clone https://github.com/USERNAME/REPOSITORY.git my-project
```

### Clone a particular branch

```powershell
git clone --branch main --single-branch https://github.com/USERNAME/REPOSITORY.git
```

### Check repository status

```powershell
git status
```

### Find the repository root

```powershell
git rev-parse --show-toplevel
```

### Check whether the current folder is a Git repository

```powershell
git rev-parse --is-inside-work-tree
```

---

## 4. Git Status and Changes

### Show changed files

```powershell
git status
```

### Short status output

```powershell
git status --short
```

Typical status codes:

| Code | Meaning |
|---|---|
| `??` | Untracked file |
| ` M` | Modified but not staged |
| `M ` | Modified and staged |
| `A ` | Added and staged |
| `D ` | Deleted and staged |

### Show unstaged changes

```powershell
git diff
```

### Show staged changes

```powershell
git diff --staged
```

### Compare two branches

```powershell
git diff main..feature-branch
```

### Show changes in a particular file

```powershell
git diff -- src/main/java/Example.java
```

### List untracked files

```powershell
git ls-files --others --exclude-standard
```

### List tracked files

```powershell
git ls-files
```

---

## 5. Add, Commit and Push

These are the commands you will use most often.

### Add one file

```powershell
git add README.md
```

### Add a particular folder

```powershell
git add src/
```

### Add all current changes

```powershell
git add .
```

Review the staged files before committing, especially if the project contains configuration files or secrets.

### Commit changes

```powershell
git commit -m "Add login feature"
```

A commit saves the staged changes in local Git history.

### Add and commit tracked changes in one command

```powershell
git commit -am "Fix login validation"
```

**Important:** `-am` does not stage new, untracked files. Use `git add` first when adding new files.

### View the current branch

```powershell
git branch --show-current
```

### Push the current branch

```powershell
git push
```

If it has no upstream branch configured yet:

```powershell
git push -u origin main
```

Replace `main` with your actual branch name if different.

The `-u` option configures the upstream tracking branch for subsequent pushes and pulls.

### Full basic workflow

```powershell
git status
git add .
git status
git commit -m "Describe your changes"
git push
```

---

## 6. Git Branches

Branches let you work on features without immediately changing the main development branch.

### List local branches

```powershell
git branch
```

### List local and remote-tracking branches

```powershell
git branch -a
```

### Create a branch

```powershell
git branch feature-login
```

### Switch to a branch

```powershell
git switch feature-login
```

### Create and switch in one command

```powershell
git switch -c feature-login
```

### Rename the current branch

```powershell
git branch -m feature-login-v2
```

### Delete a merged local branch

```powershell
git branch -d feature-login
```

### Force-delete a local branch

```powershell
git branch -D feature-login
```

`-D` can discard branch-only commits from your normal branch workflow. Use it only when you are sure you no longer need them.

### Delete a remote branch

```powershell
git push origin --delete feature-login
```

### Track a remote branch

```powershell
git switch --track origin/feature-login
```

### Example feature workflow

```powershell
git switch main
git pull --ff-only origin main
git switch -c feature-forgot-password
```

Make your changes, then:

```powershell
git add .
git commit -m "Add forgot password flow"
git push -u origin feature-forgot-password
```

You can then open a pull request on GitHub.

---

## 7. Merge and Rebase

### Merge a feature branch into main

First switch to the destination branch:

```powershell
git switch main
git pull --ff-only origin main
git merge feature-login
```

If the merge succeeds, push the updated branch:

```powershell
git push origin main
```

For a shared project, it is often preferable to open a pull request rather than push directly to `main`.

### Abort a merge in progress

```powershell
git merge --abort
```

Use this when a merge is in progress and you want to cancel it.

### Rebase your feature branch onto main

```powershell
git switch feature-login
git fetch origin
git rebase origin/main
```

Rebase replays your branch commits on top of another base.

### Continue a rebase after resolving conflicts

```powershell
git add .
git rebase --continue
```

### Abort a rebase

```powershell
git rebase --abort
```

### Important difference

- **Merge:** combines branch histories and may create a merge commit.
- **Rebase:** rewrites the commits being replayed, producing a different history.

Avoid rebasing commits that other people are already relying on unless your team agrees on the workflow.

---

## 8. GitHub Remote Commands

A remote is a named reference to another repository.

### List remotes

```powershell
git remote -v
```

### Add the origin remote

```powershell
git remote add origin https://github.com/USERNAME/REPOSITORY.git
```

### Change the origin URL

```powershell
git remote set-url origin https://github.com/USERNAME/REPOSITORY.git
```

### Show origin details

```powershell
git remote show origin
```

### Rename a remote

```powershell
git remote rename origin upstream
```

Only do this if you intentionally want to change the remote's name.

### Remove a remote

```powershell
git remote remove origin
```

This removes the local remote configuration. It does not delete the GitHub repository.

### Common fork setup

For open-source contributions, you often use:

- `origin`: your own fork.
- `upstream`: the original project.

```powershell
git remote add upstream https://github.com/ORIGINAL-OWNER/REPOSITORY.git
git remote -v
```

Get updates from the original project:

```powershell
git fetch upstream
```

Update your local main branch:

```powershell
git switch main
git merge upstream/main
```

Push the update to your fork:

```powershell
git push origin main
```

Confirm the branch names and remotes before running these commands.

---

## 9. Pull and Fetch

### Download remote changes and integrate them

```powershell
git pull
```

### Fetch remote information only

```powershell
git fetch origin
```

Fetch updates remote-tracking references without merging them into your current branch.

### Pull a specific branch

```powershell
git pull origin main
```

### Safer fast-forward-only pull

```powershell
git pull --ff-only origin main
```

This refuses to create a merge commit when the histories cannot be integrated by a fast-forward.

### See commits available remotely

```powershell
git fetch origin
git log HEAD..origin/main --oneline
```

This lists commits reachable from `origin/main` that are not reachable from the current `HEAD`.

### Check whether local and remote branches differ

```powershell
git status -sb
```

Example:

```text
## main...origin/main [ahead 2, behind 1]
```

This means the local branch has two commits not on the tracked remote branch and is missing one remote commit. Inspect the history before choosing how to integrate them.

---

## 10. Undo and Restore Changes

Git has several different ways to undo work. Choose the command based on whether the change is unstaged, staged, committed locally, or already pushed.

### Discard unstaged changes to one tracked file

```powershell
git restore README.md
```

This discards local changes in that file that have not been staged.

### Unstage a file

```powershell
git restore --staged README.md
```

The changes remain in your working directory.

### Unstage all staged changes

```powershell
git restore --staged .
```

### Restore a file from a specific commit

```powershell
git restore --source=HEAD~1 -- README.md
```

Review the result before committing. This replaces the working copy of that file with the version from the selected commit.

### Recover a deleted tracked file

```powershell
git restore -- README.md
```

This works when the deletion has not been committed and the file is tracked.

### Important warning

`git restore` can permanently discard uncommitted changes to the target file. Check `git diff` and back up valuable work first.

---

## 11. Reset and Revert

These commands affect commits, but in different ways.

### View recent commits

```powershell
git log --oneline -5
```

### Soft reset

```powershell
git reset --soft HEAD~1
```

Moves the current branch back one commit while keeping the undone commit's changes staged.

### Mixed reset

```powershell
git reset HEAD~1
```

Moves the branch back one commit and leaves the changes in the working directory, unstaged. This is the default reset mode.

### Hard reset

```powershell
git reset --hard HEAD~1
```

Moves the branch back one commit and resets tracked working files to match it.

**Danger:** This can destroy uncommitted changes. It can also remove commits from the current branch's visible history. Do not run it blindly.

### Revert a commit

```powershell
git revert COMMIT_HASH
```

Creates a new commit that reverses the selected commit's changes.

For changes already pushed to a shared branch, `git revert` is usually safer than rewriting shared history.

### Revert the latest commit

```powershell
git revert HEAD
```

Git may open an editor for the revert commit message.

### Reset versus revert

| Command | Main purpose |
|---|---|
| `git reset` | Move the current branch reference |
| `git revert` | Create a new commit that reverses another commit |
| `git restore` | Restore files in the working tree or staging area |

---

## 12. Stash

Stash temporarily saves changes so you can switch branches or perform another task.

### Save tracked changes

```powershell
git stash
```

### Save changes with a description

```powershell
git stash push -m "Work in progress"
```

### Include untracked files

```powershell
git stash push -u -m "Save feature work"
```

Ignored files are not included by `-u`.

### List stashes

```powershell
git stash list
```

### Apply the most recent stash

```powershell
git stash apply
```

### Apply a particular stash

```powershell
git stash apply stash@{0}
```

### Apply and remove the stash entry

```powershell
git stash pop
```

### Delete a stash

```powershell
git stash drop stash@{0}
```

### Delete all stashes

```powershell
git stash clear
```

This removes all stash entries. Make sure you no longer need them before clearing.

**PowerShell note:** If PowerShell interprets braces or special characters unexpectedly, quote the stash reference:

```powershell
git stash apply 'stash@{0}'
```

---

## 13. Cherry-Pick

Cherry-pick applies the changes from a particular commit onto your current branch.

### Find the commit hash

```powershell
git log --oneline
```

### Apply a commit

```powershell
git switch main
git cherry-pick COMMIT_HASH
```

### Apply without committing immediately

```powershell
git cherry-pick --no-commit COMMIT_HASH
```

### Continue after resolving conflicts

```powershell
git add .
git cherry-pick --continue
```

### Abort the cherry-pick

```powershell
git cherry-pick --abort
```

Use cherry-pick when you need a specific change without merging an entire feature branch.

---

## 14. Tags and Releases

Tags mark specific points in history, often for releases.

### List tags

```powershell
git tag
```

### Create a lightweight tag

```powershell
git tag v1.0.0
```

### Create an annotated tag

```powershell
git tag -a v1.0.0 -m "Release version 1.0.0"
```

### Push a tag

```powershell
git push origin v1.0.0
```

### Push all local tags

```powershell
git push origin --tags
```

### Delete a local tag

```powershell
git tag -d v1.0.0
```

### Delete a remote tag

```powershell
git push origin --delete v1.0.0
```

Use tags such as `v1.0.0` for Git releases. Docker image tags such as `2.10` are separate concepts, even if both represent the same application release.

---

## 15. Logs and History

### View commit history

```powershell
git log
```

### Compact history

```powershell
git log --oneline
```

### Show the last five commits

```powershell
git log -5 --oneline
```

### Show a graph of branches

```powershell
git log --oneline --graph --decorate --all
```

### Show changes introduced by a commit

```powershell
git show COMMIT_HASH
```

### Show the latest commit

```powershell
git show HEAD
```

### Show who changed each line

```powershell
git blame README.md
```

### Find a commit by message

```powershell
git log --all --grep="forgot password"
```

### Find commits that changed a file

```powershell
git log --oneline -- README.md
```

### Search tracked files for text

```powershell
git grep "forgotPassword"
```

### Show the contents of a file from a commit

```powershell
git show HEAD:README.md
```

### View reflog history

```powershell
git reflog
```

Reflog records recent movements of local references, which can help recover commits after resets or branch changes.

---

## 16. Merge Conflicts

A merge conflict occurs when Git cannot automatically combine overlapping changes.

### Step 1: Check the conflicted files

```powershell
git status
```

### Step 2: Open each conflicted file

Git may show markers like:

```text
<<<<<<< HEAD
Your current branch's version
=======
Incoming branch's version
>>>>>>> feature-login
```

Edit the file to keep the correct final content and remove all conflict markers.

### Step 3: Stage the resolved files

```powershell
git add path/to/file
```

### Step 4: Complete the merge

```powershell
git commit
```

Git may automatically supply a merge message.

### If you want to cancel a merge

```powershell
git merge --abort
```

### If the conflict occurred during a rebase

After resolving and staging:

```powershell
git rebase --continue
```

Or cancel:

```powershell
git rebase --abort
```

Do not run merge-abort commands when no merge is in progress.

---

## 17. .gitignore

`.gitignore` tells Git which untracked files should normally be ignored.

### Example for a Java Spring Boot project

```gitignore
# Java build output
target/
*.class

# Environment and secret files
.env
.env.*
!.env.example
.env2

# Logs
*.log
logs/

# IDE
.idea/
.vscode/
*.iml

# Operating system
.DS_Store
Thumbs.db
```

Adapt this file to your project's needs. Do not ignore files that your application actually requires to be versioned, such as a safe example configuration.

### Check whether a file is ignored

```powershell
git check-ignore -v .env2
```

### Remove a file from Git tracking but keep it locally

```powershell
git rm --cached -- .env2
```

Then commit the change:

```powershell
git add .gitignore
git commit -m "Stop tracking environment file"
```

This does not erase the file from previous commits.

### Remove a tracked directory from the index

```powershell
git rm -r --cached path/to/folder
```

Use this only when you intentionally want Git to stop tracking that directory while keeping the local files.

### Important secret-removal warning

If a password or API secret was committed or pushed, adding it to `.gitignore` is not enough.

1. Revoke or rotate the exposed credential.
2. Stop tracking the secret file.
3. Check commit history and remote branches.
4. If needed, follow GitHub's supported procedure for removing sensitive data from history.

Do not force-push rewritten history until you understand the impact on collaborators and backups.

---

## 18. GitHub Collaboration and Pull Requests

A common contribution workflow:

### Step 1: Fork the repository

On GitHub, use the **Fork** button to create a copy under your account.

### Step 2: Clone your fork

```powershell
git clone https://github.com/YOUR-USERNAME/REPOSITORY.git
cd REPOSITORY
```

### Step 3: Add the original repository

```powershell
git remote add upstream https://github.com/ORIGINAL-OWNER/REPOSITORY.git
```

### Step 4: Create a feature branch

```powershell
git switch -c fix-login-validation
```

### Step 5: Make and commit changes

```powershell
git status
git add .
git commit -m "Fix login validation"
```

### Step 6: Push the branch to your fork

```powershell
git push -u origin fix-login-validation
```

### Step 7: Open a pull request

Open your fork on GitHub and create a pull request from `fix-login-validation` to the original repository's target branch.

### Step 8: Update your branch when requested

```powershell
git fetch upstream
git switch fix-login-validation
git merge upstream/main
```

Resolve conflicts if necessary, test your changes, and push the updated branch.

The exact target branch might be `master` or another branch, depending on the project.

---

## 19. Advanced Git Commands

### A. Find the current commit hash

```powershell
git rev-parse HEAD
```

### B. Show the latest commit hash in short form

```powershell
git rev-parse --short HEAD
```

### C. List branches merged into main

```powershell
git branch --merged main
```

### D. List branches not merged into main

```powershell
git branch --no-merged main
```

### E. Check which commits are missing from a remote branch

```powershell
git fetch origin
git log HEAD..origin/main --oneline
```

### F. Compare the current branch with main

```powershell
git diff main...HEAD
```

This shows the changes on the current branch since the common ancestor with `main`.

### G. Show files changed by a commit

```powershell
git diff-tree --no-commit-id --name-only -r COMMIT_HASH
```

### H. Find large tracked files

```powershell
git ls-files
```

This lists tracked paths; use PowerShell file-size inspection or Git history tools to identify large files. The command alone does not calculate file sizes.

### I. Check repository object storage

```powershell
git count-objects -vH
```

### J. Verify repository objects

```powershell
git fsck --full
```

This checks repository object connectivity and validity. It may report dangling objects that are not necessarily errors.

### K. Remove stale remote-tracking references

```powershell
git fetch --prune origin
```

This removes remote-tracking references for branches that no longer exist on the remote. It does not delete the actual remote branches.

### L. Rename a remote-tracking branch only after investigating

If your repository reports an invalid reference, first inspect:

```powershell
git status
git branch -avv
git remote -v
git show-ref
```

Do not delete Git reference files manually or run hard resets as a first troubleshooting step.

### M. Create a bundle backup

```powershell
git bundle create repository-backup.bundle --all
```

A Git bundle stores repository history and references that are included in the bundle. It does not automatically include uncommitted working files or untracked files.

### N. Verify the bundle

```powershell
git bundle verify repository-backup.bundle
```

### O. Clone from a bundle

```powershell
git clone repository-backup.bundle cloned-repository
```

### P. Search for commits containing a change

```powershell
git log -S"someFunctionName" --oneline --all
```

This searches for commits where the number of occurrences of the specified string changes.

### Q. Search commit patches with a regular expression

```powershell
git log -G"somePattern" --oneline --all
```

### R. Check Git version

```powershell
git --version
```

---

## 20. Common Errors and Solutions

### Error 1: `fatal: not a git repository`

Check:

```powershell
Get-Location
git rev-parse --show-toplevel
```

Navigate to the correct project folder or initialize Git if this is a new project.

### Error 2: `remote origin already exists`

Check:

```powershell
git remote -v
```

If the URL is wrong, update it:

```powershell
git remote set-url origin https://github.com/USERNAME/REPOSITORY.git
```

Do not add another `origin` unless you intentionally use a different remote name.

### Error 3: `src refspec main does not match any`

Check:

```powershell
git branch
git status
git log --oneline -5
```

You may have a different current branch or no commit yet. Create an initial commit if necessary and push the correct branch name.

### Error 4: `failed to push some refs`

The remote may contain commits you do not have locally.

Inspect:

```powershell
git fetch origin
git status -sb
git log --oneline --graph --decorate --all
```

Integrate the remote changes safely before pushing. Do not immediately force-push.

### Error 5: `non-fast-forward`

Your local branch is behind or has diverged from the remote.

A common safe approach, when it fits your workflow, is:

```powershell
git fetch origin
git pull --ff-only origin main
```

If histories have diverged, inspect them and decide whether a merge or rebase is appropriate.

### Error 6: Authentication failed

Use GitHub's supported authentication method, such as Git Credential Manager or SSH. A GitHub account password generally cannot be used as an ordinary HTTPS Git password.

Do not put access tokens directly into repository URLs or share them in terminal screenshots.

### Error 7: Merge conflict

```powershell
git status
```

Edit conflicted files, remove conflict markers, stage the resolved files, and complete the merge or rebase.

### Error 8: Accidentally staged a file

```powershell
git restore --staged filename
```

This unstages it without discarding the working copy.

### Error 9: Accidentally committed the wrong changes

If the commit is local and has not been shared, inspect it first:

```powershell
git show --stat HEAD
git status
```

Choose a suitable reset or amend workflow only after checking whether the changes must be preserved. If the commit is already shared, consider `git revert`.

### Error 10: Secret scanning blocks a GitHub push

If GitHub detects a credential, stop trying to push the same commit. Rotate the exposed credential, remove it from the relevant files and history using an appropriate procedure, then verify the new commits before pushing. A new commit that deletes the secret does not remove it from an earlier commit.

---

## 21. Recovering from Mistakes

### Scenario A: You deleted a local branch

Check:

```powershell
git reflog
```

If you identify the lost commit, recreate a branch at its hash:

```powershell
git branch recovered-branch COMMIT_HASH
```

### Scenario B: You accidentally reset a commit

Check:

```powershell
git reflog
```

Find the previous commit reference. Before resetting anything again, save any current work and consider creating a recovery branch:

```powershell
git branch recovery-backup COMMIT_HASH
```

### Scenario C: You want to undo the latest commit but keep its changes

```powershell
git reset --soft HEAD~1
```

Use only after confirming that rewriting the local branch history is appropriate.

### Scenario D: You want to undo a pushed commit

```powershell
git revert COMMIT_HASH
git push
```

For a merge commit, the revert requires additional care because you must select the mainline parent.

### Scenario E: You want to recover a file from an earlier commit

```powershell
git log --oneline -- path/to/file
git restore --source=COMMIT_HASH -- path/to/file
```

Inspect the restored file, then commit the recovery if desired.

### Recovery rule

Before performing a destructive operation, inspect the working tree and commit history. If the changes matter, make a backup branch or copy the files first.

---

## 22. Complete Daily Workflow

For a simple project workflow:

### Start the day

```powershell
git status -sb
git fetch origin
git pull --ff-only
```

### Create a branch for a new task

```powershell
git switch -c feature-new-task
```

### Work and review

```powershell
git status
git diff
git add .
git diff --staged
```

### Commit

```powershell
git commit -m "Implement new task"
```

### Push

```powershell
git push -u origin feature-new-task
```

### Open a pull request

Use GitHub's interface to create a pull request, review the changes, run tests, and merge according to the project's workflow.

### After merging

```powershell
git switch main
git pull --ff-only origin main
git branch -d feature-new-task
```

Only delete the local feature branch after confirming the work is merged and no longer needed.

---

## 23. Git Cheat Sheet

| Task | Command |
|---|---|
| Check version | `git --version` |
| Initialize repository | `git init` |
| Clone repository | `git clone URL` |
| Check status | `git status` |
| View changes | `git diff` |
| Stage file | `git add FILE` |
| Stage all changes | `git add .` |
| Commit | `git commit -m "MESSAGE"` |
| Push | `git push` |
| Pull | `git pull` |
| Fetch | `git fetch origin` |
| List branches | `git branch` |
| Create branch | `git switch -c BRANCH` |
| Switch branch | `git switch BRANCH` |
| Merge branch | `git merge BRANCH` |
| Rebase branch | `git rebase main` |
| List remotes | `git remote -v` |
| View history | `git log --oneline` |
| Undo staged change | `git restore --staged FILE` |
| Restore working file | `git restore FILE` |
| Undo commit safely | `git revert COMMIT_HASH` |
| Temporarily save changes | `git stash` |
| Apply saved changes | `git stash pop` |
| Create tag | `git tag -a v1.0 -m "Release"` |
| Push tag | `git push origin v1.0` |
| View recent reference history | `git reflog` |
| Remove stale remote refs | `git fetch --prune origin` |

---

## Final Advice

Learn these commands in this order:

1. `status`, `add`, `commit`, `push`, `pull`
2. `branch`, `switch`, `merge`
3. `diff`, `log`, `restore`, `revert`
4. `fetch`, `remote`, `stash`
5. `rebase`, `cherry-pick`, `reflog`, tags and recovery

**Golden rule:** Before running a command that deletes files, resets history, rewrites commits or force-pushes, inspect your repository and back up important work.

