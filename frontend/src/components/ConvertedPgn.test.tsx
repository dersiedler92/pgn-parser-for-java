import { describe, it, expect, vi } from "vitest";
import type { Mock } from "vitest";
import { render, screen } from "@testing-library/react";
import type { Location } from "react-router-dom";

vi.mock("react-router-dom", async () => {
  const actual =
    await vi.importActual<typeof import("react-router-dom")>("react-router-dom");
  return {
    ...actual,
    useLocation: vi.fn(),
  };
});

vi.mock("../api-client/client", () => ({
  api: {
    createStudyAndUploadPgn: vi.fn().mockResolvedValue({ data: {} }),
  },
  authApi: {
    getCurrentUser: vi
      .fn()
      .mockResolvedValue({ data: { authenticated: false } }),
    logout: vi.fn().mockResolvedValue({}),
  },
}));

import { useLocation } from "react-router-dom";
import { MemoryRouter } from "react-router-dom";
import ConvertedPgn from "./ConvertedPgn";
import { AuthProvider } from "../auth/AuthContext";

function renderWithAuth(ui: React.ReactElement) {
  return render(
    <MemoryRouter>
      <AuthProvider>{ui}</AuthProvider>
    </MemoryRouter>,
  );
}

describe("ConvertedPgn component", () => {
  it("renders combined PGN when location state is provided", () => {
    const mockLocation: Partial<Location> = {
      state: { combinedPgn: "1. e4 e5 2. Nf3 Nc6" },
    };
    (useLocation as unknown as Mock).mockReturnValue(mockLocation);

    renderWithAuth(<ConvertedPgn />);

    expect(screen.getByText("PGN Converter")).toBeDefined();
    expect(screen.getByText("1. e4 e5 2. Nf3 Nc6")).toBeDefined();
    expect(screen.queryByText("No PGN data to display.")).toBeNull();
  });

  it("renders no-data message when combined PGN is undefined", () => {
    const mockLocation: Partial<Location> = { state: undefined };
    (useLocation as unknown as Mock).mockReturnValue(mockLocation);

    renderWithAuth(<ConvertedPgn />);

    expect(screen.getByText("No PGN data to display.")).toBeDefined();
  });
});
