import { useMemo, useState } from "react";
import { Link, useNavigate, useOutletContext } from "react-router-dom";
import { apiFetch, readApiError } from "../api";

function RegisterPage() {
  const navigate = useNavigate();
  const { currentUser } = useOutletContext();

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [loading, setLoading] = useState(false);

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
    setSubmitError("");
  }

  function validateForm() {
    const errors = {};

const trimmedName = formData.name.trim();

if (!trimmedName) {
  errors.name = "Bitte gib deinen Namen ein.";
} else if (trimmedName.length < 2 || trimmedName.length > 100) {
  errors.name = "Der Name muss zwischen 2 und 100 Zeichen lang sein.";
}

    if (!formData.email.trim()) {
      errors.email = "Bitte gib eine E-Mail-Adresse ein.";
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      errors.email = "Bitte gib eine gültige E-Mail-Adresse ein.";
    }

    if (!formData.password) {
      errors.password = "Bitte gib ein Passwort ein.";
    } else if (
      !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/.test(formData.password)
    ) {
      errors.password =
        "Das Passwort muss mindestens 8 Zeichen lang sein und Großbuchstaben, Kleinbuchstaben, eine Zahl und ein Sonderzeichen enthalten.";
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = "Bitte bestätige dein Passwort.";
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = "Die Passwörter stimmen nicht überein.";
    }

    return errors;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError("");

    const errors = validateForm();

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setLoading(true);

    try {
      const response = await apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name: formData.name.trim(),
          email: formData.email.trim(),
          password: formData.password,
        }),
      });

      if (!response.ok) {
        const apiError = await readApiError(
          response,
          "Die Registrierung konnte nicht abgeschlossen werden."
        );

        setFieldErrors(apiError.fieldErrors || {});
        throw new Error(apiError.message);
      }

      navigate("/login");
    } catch (error) {
      console.error(error);
      setSubmitError(
        error.message || "Die Registrierung konnte nicht abgeschlossen werden."
      );
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
    <section className="form-card" aria-labelledby="register-heading">
      <h2 id="register-heading">Registrieren</h2>
      <p>Erstelle ein Benutzerkonto für das Serviceportal.</p>

      {errorList.length > 0 && (
        <div className="error-summary" role="alert" aria-labelledby="register-error-summary-title">
          <p id="register-error-summary-title">
            <strong>Bitte korrigiere folgende Eingaben:</strong>
          </p>
          <ul>
            {errorList.map((item, index) => (
              <li key={`${item}-${index}`}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            autoComplete="name"
            aria-invalid={fieldErrors.name ? "true" : "false"}
            aria-describedby={fieldErrors.name ? "register-name-error" : undefined}
          />
          {fieldErrors.name && (
            <p id="register-name-error" className="field-error" role="alert">
              {fieldErrors.name}
            </p>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="email">E-Mail</label>
          <input
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            autoComplete="email"
            aria-invalid={fieldErrors.email ? "true" : "false"}
            aria-describedby={fieldErrors.email ? "register-email-error" : undefined}
          />
          {fieldErrors.email && (
            <p id="register-email-error" className="field-error" role="alert">
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
            autoComplete="new-password"
            aria-invalid={fieldErrors.password ? "true" : "false"}
            aria-describedby={fieldErrors.password ? "register-password-error" : "register-password-help"}
          />
          <p id="register-password-help" className="field-help">
            Mindestens 8 Zeichen sowie Großbuchstaben, Kleinbuchstaben, Zahl und Sonderzeichen.
          </p>
          {fieldErrors.password && (
            <p id="register-password-error" className="field-error" role="alert">
              {fieldErrors.password}
            </p>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="confirmPassword">Passwort bestätigen</label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            value={formData.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
            aria-invalid={fieldErrors.confirmPassword ? "true" : "false"}
            aria-describedby={fieldErrors.confirmPassword ? "register-confirmPassword-error" : undefined}
          />
          {fieldErrors.confirmPassword && (
            <p id="register-confirmPassword-error" className="field-error" role="alert">
              {fieldErrors.confirmPassword}
            </p>
          )}
        </div>

        <div className="button-row">
          <button type="submit" className="primary-button" disabled={loading}>
            {loading ? "Registrierung läuft..." : "Registrieren"}
          </button>

          <Link to="/login" className="secondary-button">
            Zum Login
          </Link>
        </div>
      </form>

      {submitError && (
        <p className="error-message" role="alert">
          {submitError}
        </p>
      )}
    </section>
  );
}

export default RegisterPage;