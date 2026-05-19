// App.test.tsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

vi.mock("./api-client/client", () => ({
  api: {
    convertPgnToCombined: vi.fn().mockResolvedValue({ data: { combined: "" } }),
  },
  authApi: {
    getCurrentUser: vi.fn().mockResolvedValue({ data: { authenticated: false } }),
    logout: vi.fn().mockResolvedValue({}),
  },
}));

import App from "./App";
import { AuthProvider } from "./auth/AuthContext";

function renderApp() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>,
  );
}

describe("App component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders without crashing", () => {
    renderApp();

    expect(screen.getByText("PGN Converter")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Input PGN here...")).toBeInTheDocument();
    expect(screen.getByText("Convert PGN")).toBeInTheDocument();
  });

  it("updates textarea on input", () => {
    renderApp();

    const textarea = screen.getByPlaceholderText(
      "Input PGN here...",
    ) as HTMLTextAreaElement;
    fireEvent.change(textarea, { target: { value: "1. e4 e5" } });

    expect(textarea.value).toBe("1. e4 e5");
  });

  it("calls handleConvert when button is clicked", async () => {
    renderApp();

    const button = screen.getByText("Convert PGN");
    fireEvent.click(button);

    expect(button).toBeEnabled();
  });
});
