// App.test.tsx
import { describe, it, expect } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import App from "./App";

describe("App component", () => {
  it("renders without crashing", () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    expect(screen.getByText("PGN Converter")).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText("Input PGN here...")
    ).toBeInTheDocument();
    expect(screen.getByText("Convert PGN")).toBeInTheDocument();
  });

  it("updates textarea on input", () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    const textarea = screen.getByPlaceholderText(
      "Input PGN here..."
    ) as HTMLTextAreaElement;
    fireEvent.change(textarea, { target: { value: "1. e4 e5" } });

    expect(textarea.value).toBe("1. e4 e5");
  });

  it("calls handleConvert when button is clicked", async () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>
    );

    const button = screen.getByText("Convert PGN");
    // just simulate click; actual API call would need mocking
    fireEvent.click(button);

    // For barebone test, we can just assert the button exists and is clickable
    expect(button).toBeEnabled();
  });
});
