import { useEffect, useState } from "react";

function AccessibilityToolbar() {
  const [fontScale, setFontScale] = useState(
    localStorage.getItem("fontScale") || "1",
  );

  const [highContrast, setHighContrast] = useState(
    localStorage.getItem("highContrast") === "true",
  );

  useEffect(() => {
    document.documentElement.style.setProperty("--font-scale", fontScale);
    localStorage.setItem("fontScale", fontScale);
  }, [fontScale]);

  useEffect(() => {
    document.body.classList.toggle("high-contrast", highContrast);
    localStorage.setItem("highContrast", highContrast);
  }, [highContrast]);

  return (
    <section
      className="accessibility-toolbar utility-toolbar"
      aria-label="Barrierefreiheitseinstellungen"
    >
      <div className="utility-toolbar-row">
        <div className="utility-toolbar-group">
          <span className="utility-toolbar-title" aria-hidden="true">
            Barrierefreiheit
          </span>
        </div>

        <div className="utility-toolbar-group">
          <span className="toolbar-small-label">Schrift</span>

          <button
            type="button"
            className={`toolbar-button ${fontScale === "1" ? "active-toolbar-button" : ""}`}
            onClick={() => setFontScale("1")}
            aria-label="Normale Schriftgröße"
            aria-pressed={fontScale === "1"}
          >
            A
          </button>

          <button
            type="button"
            className={`toolbar-button ${fontScale === "1.15" ? "active-toolbar-button" : ""}`}
            onClick={() => setFontScale("1.15")}
            aria-label="Größere Schrift"
            aria-pressed={fontScale === "1.15"}
          >
            A+
          </button>

          <button
            type="button"
            className={`toolbar-button ${fontScale === "1.3" ? "active-toolbar-button" : ""}`}
            onClick={() => setFontScale("1.3")}
            aria-label="Sehr große Schrift"
            aria-pressed={fontScale === "1.3"}
          >
            A++
          </button>
        </div>

        <div className="utility-toolbar-group">
          <span className="toolbar-small-label">Kontrast</span>

          <button
            type="button"
            className={`toolbar-toggle ${highContrast ? "active-toggle" : ""}`}
            onClick={() => setHighContrast((prev) => !prev)}
            aria-pressed={highContrast}
          >
            {highContrast ? "An" : "Aus"}
          </button>
        </div>
      </div>
    </section>
  );
}

export default AccessibilityToolbar;
