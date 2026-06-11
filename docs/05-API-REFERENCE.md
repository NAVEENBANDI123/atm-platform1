# Phase 12 — API Reference

Base URL: `/api/v1` · All responses use the `ApiResponse` envelope. Auth via `Authorization: Bearer <accessToken>`.

## Auth (`/auth`, public)
| Method | Path | Body | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | `RegisterRequest` | Create user + account, returns tokens |
| POST | `/auth/login` | `LoginRequest` | Authenticate, returns access + refresh tokens |
| POST | `/auth/refresh` | `RefreshTokenRequest` | Exchange refresh token for new access token |
| POST | `/auth/logout` | `RefreshTokenRequest` | Revoke refresh token |
| POST | `/auth/forgot-password` | `ForgotPasswordRequest` | Verify identity (username+mobile), issue reset |
| POST | `/auth/reset-password` | `ResetPasswordRequest` | Set new password |

## Accounts (`/accounts`, CUSTOMER/ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/accounts/me` | Current user's account + balance |
| POST | `/accounts/deposit` | Deposit into own account (atomic) |
| POST | `/accounts/withdraw` | Withdraw (atomic, balance-checked) |
| POST | `/accounts/transfer` | Transfer to another account (atomic, locked) |

## Transactions (`/transactions`, CUSTOMER/ADMIN)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/transactions` | Paged history for own account (`?page&size`) |
| GET | `/transactions/mini-statement` | Last 5 transactions |

## Users (`/users`, authenticated)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/users/profile` | Current user profile |

## Admin (`/admin`, ADMIN only)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/users` | Paged list of all users |
| PATCH | `/admin/accounts/{id}/status` | Lock/unlock/close an account |

## Status codes
`200` OK · `201` Created · `400` validation/bad request · `401` unauthenticated · `403` forbidden · `404` not found · `409` conflict (duplicate / insufficient funds) · `423` account locked · `500` server error.
