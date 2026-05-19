import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import { api } from "../api-client/client";
import { useAuth } from "../auth/useAuth";
import "./ConvertedPgn.css";

export default function ConvertedPgn() {
  const location = useLocation();
  const navigate = useNavigate();
  const { authenticated, loginWithLichess } = useAuth();

  const combinedPgn = location.state?.combinedPgn as string | undefined;

  const [copySuccess, setCopySuccess] = useState(false);
  const [studyName, setStudyName] = useState("");
  const [color, setColor] = useState<"white" | "black">("white");
  const [uploadSuccess, setUploadSuccess] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [studyUrl, setStudyUrl] = useState<string | null>(null);

  const handleCopy = async () => {
    if (!combinedPgn) return;

    try {
      await navigator.clipboard.writeText(combinedPgn);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    } catch (err) {
      console.error("Failed to copy:", err);
    }
  };

  const handleUploadToLichess = async () => {
    if (!combinedPgn || !studyName.trim() || uploading) return;

    if (!authenticated) {
      loginWithLichess();
      return;
    }

    setUploading(true);
    setUploadSuccess(false);
    setStudyUrl(null);

    try {
      const request = {
        pgn: combinedPgn,
        studyName: studyName.trim(),
        color: color,
      };

      const response = await api.createStudyAndUploadPgn(request);

      const url = response.data?.url ?? null;
      setStudyUrl(url);

      setUploadSuccess(true);
      setTimeout(() => setUploadSuccess(false), 3000);
    } catch (err) {
      console.error("Failed to upload to Lichess:", err);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="converted-pgn-layout">
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

      <main className="container">
        {!combinedPgn ? (
          <>
            <p className="no-data-message">No PGN data to display.</p>
            <button className="btn btn-secondary" onClick={() => navigate("/")}>
              ← Back
            </button>
          </>
        ) : (
          <>
            <pre className="pgn-display">{combinedPgn}</pre>

            <div className="study-name-row">
              <input
                type="text"
                placeholder="Enter study name..."
                value={studyName}
                onChange={(e) => setStudyName(e.target.value)}
                className="study-name-input"
              />
              <select
                value={color}
                onChange={(e) => setColor(e.target.value as "white" | "black")}
                className="color-select"
              >
                <option value="white">White</option>
                <option value="black">Black</option>
              </select>
            </div>

            <div className="button-row">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate("/")}
              >
                ← Back
              </button>

              <button
                type="button"
                className="btn btn-primary"
                onClick={handleCopy}
              >
                {copySuccess ? "✓ Copied!" : "Copy PGN"}
              </button>

              <button
                type="button"
                className="btn btn-primary"
                onClick={handleUploadToLichess}
                disabled={!studyName.trim() || uploading}
              >
                {uploading
                  ? "Uploading..."
                  : uploadSuccess
                    ? "✓ Uploaded!"
                    : authenticated
                      ? "Upload to Lichess"
                      : "Sign in & Upload to Lichess"}
              </button>
            </div>

            {studyUrl && (
              <div className="study-link-row">
                <a
                  href={studyUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="study-link"
                >
                  Open new Lichess study
                </a>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}