# Contributing

Thanks for contributing to the ATM Platform!

## Branching
- `main` is protected and always deployable.
- Create feature branches: `feature/<short-name>`, `fix/<short-name>`, `chore/<short-name>`.

## Commit messages
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
feat(account): add scheduled transfer support
fix(auth): reject expired refresh tokens
chore(ci): cache maven dependencies
```

## Before opening a PR
- Backend: `cd backend && mvn verify`
- Frontend: `cd frontend && npm run lint && npm run build`
- Keep PRs focused; update docs in `docs/` when behavior changes.

## Code style
- Java: 4-space indent, follow the existing layered package structure (`controller → service → repository`).
- React: 2-space indent, ESLint + Prettier (`npm run lint`, `npm run format`).
- `.editorconfig` enforces the basics across editors.

## Local development
See [`README.md`](README.md) for Docker and local run instructions.
