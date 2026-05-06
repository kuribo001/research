# Git Rule

## Objective

This document standardizes how the team works with Git so development, review, and release processes stay stable.

## Branch

Main branches and their environments:

- `GDN-main`: `production` environment.
- `GDN-dev`: `develop` environment.

Supporting branches:

- `feature/<ticketID>-<short-name>`: develop a new feature, branching from `GDN-dev`.
- `feature/<ticketID>-<short-name>-<child>`: branch from the main `feature/*` branch when multiple people work on the same feature or when fixing an issue within that feature.
- `bugfix/<ticketID>-<short-name>`: fix a bug in the normal development flow, branching from `GDN-dev`.
- `hotfix/<ticketID>-<short-name>`: fix an urgent issue that must go directly to production, branching from `GDN-main`.

## Branch and environment mapping

- `GDN-dev` <-> `develop`
- `GDN-main` <-> `production`

## General Principles

- Do not commit directly to `GDN-main` or `GDN-dev`.
- All changes must go through pull requests.
- Pull requests must clearly link to a ticket or issue.
- Use `Squash merge`.

`Conventional Commits` convention:

- General format: `<ticketID> <type>(<scope>): <short-description>`
- If `scope` is not needed, this format is allowed: `<ticketID> <type>: <short-description>`
- `ticketID` is mandatory and must appear at the beginning of the commit message.
- `short-description` must be concise, meaningful, lowercase, and accurately describe the change.
- Additional detail may be added in the body when the commit needs to explain rationale, impact, or migration direction.

Available `type` values:

- `feat`: add a new feature.
- `fix`: fix a bug.
- `refactor`: restructure code without changing behavior.
- `perf`: improve performance.
- `test`: add or update tests.
- `docs`: update documentation.
- `build`: changes related to build tooling or build dependencies.
- `ci`: changes to the CI/CD pipeline.
- `chore`: other supporting work not covered above.
- `style`: code formatting, whitespace, lint, without logic changes.

Correct examples:

- `GDN-123 feat(auth): add login API integration`
- `GDN-234 fix(order): handle duplicate order submission`
- `GDN-345 docs(git-rule): add conventional commits guideline`
- `GDN-456 test(customer): add unit test for customer facade`

## Feature workflow

1. Create a `feature/*` or `bugfix/*` branch from `GDN-dev`.
2. Code, test, and update documentation if needed.
3. Create a pull request into `GDN-dev`.
4. After review and successful CI, merge.

Additional rules for feature branches:

- `feature/*` or `bugfix/*` branches must be updated with the latest code from `GDN-dev` every day to avoid drift and reduce merge conflicts.
- The team must proactively `merge` or `rebase` the latest code from `GDN-dev` into the active branch before continuing development whenever `GDN-dev` has changed.
- If multiple people contribute to one feature, child branches must branch from that feature's main `feature/*` branch, not directly from `GDN-dev`.
- Child branches may only be merged back into the parent `feature/*` branch before that parent branch is merged into `GDN-dev`.
- The main `feature/*` branch is the integration point for the full feature when multiple people are working on it.
- If an issue appears after the `feature/*` branch has already been merged into `GDN-dev`, the team must fix it on that same `feature/*` branch and merge back into `GDN-dev`.
- Do not fix directly on `GDN-dev` for issues that belong to a feature still being developed on a `feature/*` branch.

## Production release workflow

1. When the feature set on `GDN-dev` is ready for release, create a pull request from `GDN-dev` to `GDN-main`.
2. After review and successful CI, merge into `GDN-main`.
3. Deploy `GDN-main` to the `production` environment.
4. Tag the version if needed for the production release.

## Hotfix workflow

1. Create a `hotfix/*` branch from `GDN-main`.
2. Fix the issue, run quick validation, and create a pull request into `GDN-main`.
3. After `GDN-main` is merged, it must be merged back into `GDN-dev` to prevent code divergence.

## Branch synchronization workflow

- Every change released to `GDN-main` must be synchronized back into `GDN-dev`.
- Do not edit code directly in the runtime environment; every change must still go through Git branches.

## Code review checklist

- At least 1 reviewer is mandatory.
- The PR must include a `ticketID`, a clear description of the goal, and the impact scope.
- PRs larger than 500 lines should be split when possible.
- Reviewers should prioritize behavior risks, security issues, regressions, and missing tests.
- The PR author is responsible for addressing feedback and updating the description if the scope changes.

Reviewers must check the following:

- Is the scope aligned with the ticket, without mixing unrelated goals?
- Do the branch name, commit message, PR title, and PR description follow the `git rule`?
- Does the code follow the architecture and layer rules defined in `01-architecture.md`?
- Are business rules placed correctly, not duplicated, and not changing behavior outside the ticket scope?
- Are variable, function, class, component, and file names meaningful and consistent with conventions?
- Has the API contract changed, and if so, does it comply with `02-api-guideline.md` and include a backward compatibility plan?
- Does the backend expose entities directly, place transaction boundaries incorrectly, call the wrong module, or misuse `application/domain/infrastructure`?
- Does the frontend follow `feature-based architecture`, avoid direct API calls in components, avoid parsing raw responses in components, and place code correctly within `pages/features/shared/services/hooks`?
- Does the database usage violate `03-database-guideline.md`, enable `ddl-auto`, map entities incorrectly, create unauthorized schema changes, or introduce N+1 / inefficient queries?
- Are validation, permission checks, security rules, sensitive logging, and exception handling correct?
- Are datetime, error format, status codes, field naming, and pagination consistent with conventions?
- Have loading states, empty states, error states, optimistic updates, responsive UI, and target browsers been handled for FE-related changes?
- Have logging, trace IDs, new config, feature flags, health checks, smoke tests, or operational impact been considered?
- Are tests sufficient for happy paths, unhappy paths, regression risks, and bug fixes?
- If this is a bug fix, is there a regression test or a clear reason why it cannot be added yet?

Reviewers should ask questions if they see one of these signs:

- The code uses workarounds that are hard to explain or relies on comments instead of a clear design.
- A class, component, hook, or service is doing too many things.
- The PR changes many files but the description does not explain the impact well enough.
- There are changes to contracts, config, queries, security, or deployment behavior that are not mentioned in the PR.
