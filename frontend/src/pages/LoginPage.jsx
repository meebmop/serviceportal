import { useMemo, useState } from "react";
import { Link, useNavigate, useOutletContext, useSearchParams } from "react-router-dom";
import { apiFetch, readApiError } from "../api";

function LoginPage() {
  const navigate = useNavigate();
  const { currentUser, reloadUser } = useOutletContext();
  const [searchParams] = useSearchParams();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const redirectTarget = searchParams.get("redirect");

  const errorList = useMemo(() => {
    return Object.values(fieldErrors).filter(Boolean);
  }, [fieldErrors]);

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setFieldErrors((prev) => ({
      ...prev,
      [name]: "",
    }));
    setError("");
  }

  function validateForm() {
    const errors = {};

    if (!formData.email.trim()) {
      errors.email = "Bitte gib deine E-Mail-Adresse ein.";
    }

    if (!formData.password) {
      errors.password = "Bitte gib dein Passwort ein.";
    }

    return errors;
  }

  async function handleLogin(event) {
    event.preventDefault();
    setError("");

    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setLoading(true);

    try {
      const loginResponse = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: formData.email.trim(),
          password: formData.password,
        }),
      });

      if (!loginResponse.ok) {
        const fallbackMessage =
          loginResponse.status === 429
            ? "Zu viele fehlgeschlagene Anmeldeversuche. Bitte versuche es später erneut."
            : "Ungültige E-Mail oder ungültiges Passwort.";

        const apiError = await readApiError(loginResponse, fallbackMessage);
        throw new Error(apiError.message);
      }

      await reloadUser();

      const meResponse = await apiFetch("/api/auth/me");

      if (!meResponse.ok) {
        throw new Error("Sitzung konnte nicht geladen werden.");
      }

      const loggedInUser = await meResponse.json();

      if (redirectTarget && loggedInUser.role === "USER") {
        navigate(redirectTarget);
        return;
      }

      navigate(loggedInUser.role === "ADMIN" ? "/admin" : "/request");
    } catch (err) {
      console.error(err);
      setError(err.message || "Anmeldung fehlgeschlagen.");
    } finally {
      setLoading(false);
    }
  }

  if (currentUser) {
    return (
      <section className="content-card">
        <h2>Bereits angemeldet</h2>
        <p>
          Du bist bereits als <strong>{currentUser.name}</strong> angemeldet.
        </p>
      </section>
    );
  }

  return (
    <section className="form-card" aria-labelledby="login-heading">
      <h2 id="login-heading">Login</h2>
      <p>Melde dich mit deiner E-Mail-Adresse und deinem Passwort an.</p>

      {errorList.length > 0 && (
        <div className="error-summary" role="alert" aria-labelledby="login-error-summary-title">
          <p id="login-error-summary-title">
            <strong>Bitte korrigiere folgende Eingaben:</strong>
          </p>
          <ul>
            {errorList.map((item, index) => (
              <li key={`${item}-${index}`}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={handleLogin} noValidate>
        <div className="form-group">
          <label htmlFor="email">E-Mail</label>
          <input
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            autoComplete="username"
            aria-invalid={fieldErrors.email ? "true" : "false"}
            aria-describedby={fieldErrors.email ? "login-email-error" : undefined}
          />
          {fieldErrors.email && (
            <p id="login-email-error" className="field-error" role="alert">
              {fieldErrors.email}
            </p>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="password">Passwort</label>
          <input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            autoComplete="current-password"
            aria-invalid={fieldErrors.password ? "true" : "false"}
            aria-describedby={fieldErrors.password ? "login-password-error" : undefined}
          />
          {fieldErrors.password && (
            <p id="login-password-error" className="field-error" role="alert">
              {fieldErrors.password}
            </p>
          )}
        </div>

        <div className="button-row">
          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? "Anmeldung läuft..." : "Anmelden"}
          </button>

          <Link to="/register" className="secondary-button">
            Registrieren
          </Link>
        </div>
      </form>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}
    </section>
  );
}

export default LoginPage;