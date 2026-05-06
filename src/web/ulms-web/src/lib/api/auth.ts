import "server-only";

import { apiFetch, type FetchOptions } from "./client";
import type { components } from "./types";

export type LoginRequest = components["schemas"]["LoginRequest"];
export type RegisterRequest = components["schemas"]["RegisterRequest"];
export type AuthResponse = components["schemas"]["AuthResponse"];
export type UserResponse = components["schemas"]["UserResponse"];
export type UserRole = NonNullable<AuthResponse["role"]>;

export function login(body: LoginRequest, opts: FetchOptions = {}) {
  return apiFetch<AuthResponse>("/api/auth/login", {
    ...opts,
    method: "POST",
    body,
  });
}

export function register(body: RegisterRequest, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>("/api/auth/register", {
    ...opts,
    method: "POST",
    body,
  });
}

export function me(token: string, opts: FetchOptions = {}) {
  return apiFetch<UserResponse>("/api/auth/me", { ...opts, token });
}
