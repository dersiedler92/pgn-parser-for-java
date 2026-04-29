import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import type { ChangeEvent } from "react";
import { DefaultApi, Configuration } from "./api-client";
import type { PgnRequest } from "./api-client";
import "./App.css";

function App() {
  const [pgn, setPgn] = useState<string>("");
  const navigate = useNavigate();
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const configuration = new Configuration({
    basePath: "/api",
    username: "alpa",
    password: "secret123",
  });
  const api = new DefaultApi(configuration);

useEffect(() => {
  const textarea = textareaRef.current;
  if (textarea) {
    textarea.style.height = "auto";
    textarea.style.height = `${Math.max(textarea.scrollHeight, 120)}px`;
  }
}, [pgn]);

  const handleInputChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPgn(event.target.value);
  };

  const handleConvert = async () => {
    try {
      const request: PgnRequest = { pgn };
      const response = await api.convertPgnToCombined(request);
      navigate("/converted-pgn", {
        state: { combinedPgn: response.data.combined },
      });
    } catch (err) {
      console.error("API call failed", err);
    }
  };

  return (
    <div className="app-layout">
      <header className="title-bar">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 448 512"
          className="chess-icon"
        >
          <path d="M224 0c17.7 0 32 14.3 32 32V48h16c17.7 0 32 14.3 32 32s-14.3 32-32 32H256v48H192V112H176c-17.7 0-32-14.3-32-32s14.3-32 32-32h16V32c0-17.7 14.3-32 32-32zm-48 160c0-17.7-14.3-32-32-32s-32 14.3-32 32v16c0 35.3 28.7 64 64 64h32v32c0 17.7 14.3 32 32 32s32-14.3 32-32V240h32c35.3 0 64-28.7 64-64V160c0-17.7-14.3-32-32-32s-32 14.3-32 32v16c0 8.8-7.2 16-16 16H192c-8.8 0-16-7.2-16-16V160zM32 320c0-35.3 28.7-64 64-64h16c17.7 0 32 14.3 32 32s-14.3 32-32 32H96c-8.8 0-16 7.2-16 16v32c0 8.8-7.2 16-16 16H32c-17.7 0-32-14.3-32-32V320zM416 256c35.3 0 64 28.7 64 64v32c0 17.7-14.3 32-32 32H384c-8.8 0-16-7.2-16-16V352c0-8.8-7.2-16-16-16H320c-17.7 0-32-14.3-32-32s14.3-32 32-32h16c35.3 0 64-28.7 64-64V160c0-17.7 14.3-32 32-32s32 14.3 32 32v96zM32 480c-17.7 0-32-14.3-32-32s14.3-32 32-32H416c17.7 0 32 14.3 32 32s-14.3 32-32 32H32z" />
        </svg>
        <h1>PGN Converter</h1>
      </header>

      <div className="container">
        <div className="editor-wrapper">
          <textarea
            ref={textareaRef}
            value={pgn}
            onChange={handleInputChange}
            placeholder="Input PGN here..."
            className="auto-resize-textarea"
            rows={6}
          />
          <div className="button-group">
            <button className="btn" onClick={handleConvert}>
              Convert PGN
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
