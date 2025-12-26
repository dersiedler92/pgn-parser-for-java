import { describe, it, expect, vi } from "vitest";
import type { Mock } from "vitest";
import { render, screen } from "@testing-library/react";
import ConvertedPgn from "./ConvertedPgn";
import type { Location } from "react-router-dom";

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>(
    "react-router-dom"
  );
  return {
    ...actual,
    useLocation: vi.fn(),
  };
});

import { useLocation } from "react-router-dom";

describe("ConvertedPgn component", () => {
  it("renders combined PGN when location state is provided", () => {
    const mockLocation: Partial<Location> = {
      state: { combinedPgn: "1. e4 e5 2. Nf3 Nc6" },
    };
    (useLocation as unknown as Mock).mockReturnValue(mockLocation);

    render(<ConvertedPgn />);

    expect(screen.getByText("PGN Converter")).toBeDefined();
    expect(screen.getByText("1. e4 e5 2. Nf3 Nc6")).toBeDefined();
    expect(screen.queryByText("No PGN data to display.")).toBeNull();
  });

  it("renders no-data message when combined PGN is undefined", () => {
    const mockLocation: Partial<Location> = { state: undefined };
    (useLocation as unknown as Mock).mockReturnValue(mockLocation);

    render(<ConvertedPgn />);

    expect(screen.getByText("No PGN data to display.")).toBeDefined();
  });
});
