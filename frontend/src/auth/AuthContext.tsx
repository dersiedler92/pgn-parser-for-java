import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { authApi } from "../api-client/client";

export interface AuthState {
  authenticated: boolean;
  username?: string;
  loading: boolean;
  loginWithLichess: () => void;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [username, setUsername] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      const response = await authApi.getCurrentUser();
      setAuthenticated(Boolean(response.data?.authenticated));
      setUsername(response.data?.username ?? undefined);
    } catch (err) {
      console.error("Failed to load auth status", err);
      setAuthenticated(false);
      setUsername(undefined);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const loginWithLichess = useCallback(() => {
    // Full-page navigation: the backend issues a 302 to Lichess.
    window.location.href = "/api/auth/lichess/login";
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error("Logout failed", err);
    } finally {
      setAuthenticated(false);
      setUsername(undefined);
    }
  }, []);

  const value = useMemo(
    () => ({ authenticated, username, loading, loginWithLichess, logout, refresh }),
    [authenticated, username, loading, loginWithLichess, logout, refresh],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
