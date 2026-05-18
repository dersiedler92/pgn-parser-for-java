import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";

const getCurrentUserMock = vi.fn();
const logoutMock = vi.fn();

vi.mock("../api-client/client", () => ({
  authApi: {
    getCurrentUser: (...args: unknown[]) => getCurrentUserMock(...args),
    logout: (...args: unknown[]) => logoutMock(...args),
  },
}));

import { AuthProvider } from "./AuthContext";
import { useAuth } from "./useAuth";

function Probe() {
  const { authenticated, username, loading } = useAuth();
  return (
    <div>
      <span data-testid="loading">{String(loading)}</span>
      <span data-testid="auth">{String(authenticated)}</span>
      <span data-testid="user">{username ?? "<none>"}</span>
    </div>
  );
}

describe("AuthContext", () => {
  beforeEach(() => {
    getCurrentUserMock.mockReset();
    logoutMock.mockReset();
  });

  it("reflects unauthenticated state when /auth/me returns authenticated=false", async () => {
    getCurrentUserMock.mockResolvedValueOnce({ data: { authenticated: false } });

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );

    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
    expect(screen.getByTestId("auth").textContent).toBe("false");
    expect(screen.getByTestId("user").textContent).toBe("<none>");
  });

  it("reflects authenticated state and username from backend", async () => {
    getCurrentUserMock.mockResolvedValueOnce({
      data: { authenticated: true, username: "alice" },
    });

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );

    await waitFor(() => expect(screen.getByTestId("auth").textContent).toBe("true"));
    expect(screen.getByTestId("user").textContent).toBe("alice");
  });

  it("treats a failed /auth/me call as unauthenticated", async () => {
    getCurrentUserMock.mockRejectedValueOnce(new Error("boom"));

    render(
      <AuthProvider>
        <Probe />
      </AuthProvider>,
    );

    await waitFor(() => expect(screen.getByTestId("loading").textContent).toBe("false"));
    expect(screen.getByTestId("auth").textContent).toBe("false");
  });
});
