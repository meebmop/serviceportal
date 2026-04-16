import { useEffect, useMemo, useState } from "react";
import { apiFetch, readApiError } from "../api";

const emptyForm = {
  title: "",
  category: "",
  description: "",
};

function AdminOffersPage() {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [formData, setFormData] = useState(emptyForm);
  const [editingOfferId, setEditingOfferId] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const errorList = useMemo(() => {
    return Object.values(fieldErrors).filter(Boolean);
  }, [fieldErrors]);

  async function loadOffers() {
    try {
      setError("");
      const response = await apiFetch("/api/offers");

      if (!response.ok) {
        throw new Error("Serviceangebote konnten nicht geladen werden.");
      }

      const data = await response.json();
      setOffers(data);
    } catch (err) {
      console.error(err);
      setError(err.message || "Fehler beim Laden der Serviceangebote.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOffers();
  }, []);

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
    setSuccessMessage("");
  }

  function validateFrontend() {
    const errors = {};

    if (!formData.title.trim()) {
      errors.title = "Bitte gib einen Titel ein.";
    } else if (formData.title.trim().length < 3) {
      errors.title = "Der Titel muss mindestens 3 Zeichen lang sein.";
    } else if (formData.title.trim().length > 100) {
      errors.title = "Der Titel darf maximal 100 Zeichen lang sein.";
    }

    if (!formData.category.trim()) {
      errors.category = "Bitte gib eine Kategorie ein.";
    } else if (formData.category.trim().length > 100) {
      errors.category = "Die Kategorie darf maximal 100 Zeichen lang sein.";
    }

    if (!formData.description.trim()) {
      errors.description = "Bitte gib eine Beschreibung ein.";
    } else if (formData.description.trim().length < 10) {
      errors.description = "Die Beschreibung muss mindestens 10 Zeichen lang sein.";
    } else if (formData.description.trim().length > 500) {
      errors.description = "Die Beschreibung darf maximal 500 Zeichen lang sein.";
    }

    return errors;
  }

  function resetForm() {
    setFormData(emptyForm);
    setEditingOfferId(null);
    setFieldErrors({});
  }

  function startEdit(offer) {
    setFormData({
      title: offer.title || "",
      category: offer.category || "",
      description: offer.description || "",
    });
    setEditingOfferId(offer.id);
    setFieldErrors({});
    setSuccessMessage("");
    setSubmitError("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError("");
    setSuccessMessage("");

    const errors = validateFrontend();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setSubmitting(true);

    try {
      const url = editingOfferId ? `/api/offers/${editingOfferId}` : "/api/offers";
      const method = editingOfferId ? "PUT" : "POST";

      const response = await apiFetch(url, {
        method,
        body: JSON.stringify({
          title: formData.title.trim(),
          category: formData.category.trim(),
          description: formData.description.trim(),
        }),
      });

      if (!response.ok) {
        const fallbackMessage =
          response.status === 401
            ? "Bitte melde dich erneut an."
            : response.status === 403
            ? "Du hast keine Berechtigung, Serviceangebote zu verwalten."
            : response.status === 400
            ? "Bitte überprüfe die Eingaben."
            : "Das Serviceangebot konnte nicht gespeichert werden.";

        const apiError = await readApiError(response, fallbackMessage);
        setFieldErrors(apiError.fieldErrors || {});
        throw new Error(apiError.message);
      }

      await loadOffers();
      const wasEdit = editingOfferId !== null;
      resetForm();
      setSuccessMessage(
        wasEdit
          ? "Serviceangebot wurde erfolgreich aktualisiert."
          : "Serviceangebot wurde erfolgreich angelegt."
      );
    } catch (err) {
      console.error(err);
      setSubmitError(err.message || "Fehler beim Speichern.");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(offerId) {
    const confirmed = window.confirm("Möchtest du dieses Serviceangebot wirklich löschen?");
    if (!confirmed) {
      return;
    }

    setSubmitError("");
    setSuccessMessage("");

    try {
      const response = await apiFetch(`/api/offers/${offerId}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        const fallbackMessage =
          response.status === 409
            ? "Das Serviceangebot kann nicht gelöscht werden, weil noch Anfragen darauf verweisen."
            : response.status === 401
            ? "Bitte melde dich erneut an."
            : response.status === 403
            ? "Du hast keine Berechtigung, Serviceangebote zu löschen."
            : "Das Serviceangebot konnte nicht gelöscht werden.";

        const apiError = await readApiError(response, fallbackMessage);
        throw new Error(apiError.message);
      }

      await loadOffers();

      if (editingOfferId === offerId) {
        resetForm();
      }

      setSuccessMessage("Serviceangebot wurde erfolgreich gelöscht.");
    } catch (err) {
      console.error(err);
      setSubmitError(err.message || "Fehler beim Löschen.");
    }
  }

  if (loading) {
    return (
      <section className="content-card">
        <h2>Serviceangebote verwalten</h2>
        <p>Lade Serviceangebote...</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="content-card">
        <h2>Serviceangebote verwalten</h2>
        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  return (
    <>
      <section className="form-card" aria-labelledby="offer-form-heading">
        <h2 id="offer-form-heading">
          {editingOfferId ? "Serviceangebot bearbeiten" : "Neues Serviceangebot anlegen"}
        </h2>

        {errorList.length > 0 && (
          <div className="error-summary" role="alert" aria-labelledby="offer-error-summary-title">
            <p id="offer-error-summary-title">
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
            <label htmlFor="title">Titel</label>
            <input
              id="title"
              name="title"
              type="text"
              value={formData.title}
              onChange={handleChange}
              aria-invalid={fieldErrors.title ? "true" : "false"}
              aria-describedby={fieldErrors.title ? "offer-title-error" : undefined}
            />
            {fieldErrors.title && (
              <p id="offer-title-error" className="field-error" role="alert">
                {fieldErrors.title}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="category">Kategorie</label>
            <input
              id="category"
              name="category"
              type="text"
              value={formData.category}
              onChange={handleChange}
              aria-invalid={fieldErrors.category ? "true" : "false"}
              aria-describedby={fieldErrors.category ? "offer-category-error" : undefined}
            />
            {fieldErrors.category && (
              <p id="offer-category-error" className="field-error" role="alert">
                {fieldErrors.category}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="description">Beschreibung</label>
            <textarea
              id="description"
              name="description"
              rows="5"
              value={formData.description}
              onChange={handleChange}
              aria-invalid={fieldErrors.description ? "true" : "false"}
              aria-describedby={fieldErrors.description ? "offer-description-error" : "offer-description-help"}
            />
            <p id="offer-description-help" className="field-help">
              Beschreibe das Serviceangebot kurz und verständlich.
            </p>
            {fieldErrors.description && (
              <p id="offer-description-error" className="field-error" role="alert">
                {fieldErrors.description}
              </p>
            )}
          </div>

          <div className="button-row">
            <button type="submit" className="primary-button" disabled={submitting}>
              {submitting
                ? "Speichern..."
                : editingOfferId
                ? "Änderungen speichern"
                : "Angebot anlegen"}
            </button>

            {editingOfferId && (
              <button type="button" className="secondary-button" onClick={resetForm}>
                Abbrechen
              </button>
            )}
          </div>
        </form>

        {successMessage && (
          <p className="success-message" role="status">
            {successMessage}
          </p>
        )}

        {submitError && (
          <p className="error-message" role="alert">
            {submitError}
          </p>
        )}
      </section>

      <section className="content-card">
        <h2>Vorhandene Serviceangebote</h2>

        {offers.length === 0 ? (
          <p>Es sind aktuell keine Serviceangebote vorhanden.</p>
        ) : (
          <div className="offers-grid">
            {offers.map((offer) => (
              <article key={offer.id} className="offer-card">
                <span className="offer-badge">{offer.category}</span>
                <h3>{offer.title}</h3>
                <p>{offer.description}</p>

                <div className="button-row">
                  <button type="button" className="secondary-button" onClick={() => startEdit(offer)}>
                    Bearbeiten
                  </button>
                  <button type="button" className="danger-button" onClick={() => handleDelete(offer.id)}>
                    Löschen
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </>
  );
}

export default AdminOffersPage;