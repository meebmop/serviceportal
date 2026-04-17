import { useEffect, useMemo, useState } from "react";
import {
  useLocation,
  useNavigate,
  useOutletContext,
  useSearchParams,
} from "react-router-dom";
import { apiFetch, readApiError } from "../api";
import { REQUEST_PRIORITIES } from "../constants/requestOptions";

const INITIAL_FORM_DATA = {
  serviceOfferId: "",
  subject: "",
  priority: "Normal",
  message: "",
};

function RequestPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { currentUser } = useOutletContext();
  const [offers, setOffers] = useState([]);
  const [formData, setFormData] = useState(INITIAL_FORM_DATA);
  const [fieldErrors, setFieldErrors] = useState({});
  const [pageError, setPageError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isLoadingOffers, setIsLoadingOffers] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const redirectTarget = useMemo(() => {
    return `${location.pathname}${location.search}`;
  }, [location.pathname, location.search]);

  useEffect(() => {
    if (!currentUser) {
      navigate(`/login?redirect=${encodeURIComponent(redirectTarget)}`, {
        replace: true,
      });
    }
  }, [currentUser, navigate, redirectTarget]);

  useEffect(() => {
    async function loadOffers() {
      try {
        setIsLoadingOffers(true);
        setPageError("");

        const response = await apiFetch("/api/offers");

        if (!response.ok) {
          throw new Error("Serviceangebote konnten nicht geladen werden.");
        }

        const data = await response.json();
        setOffers(data);

        const requestedOfferId = searchParams.get("offerId");
        const hasRequestedOffer =
          requestedOfferId &&
          data.some((offer) => String(offer.id) === requestedOfferId);

        setFormData((prev) => ({
          ...prev,
          serviceOfferId: hasRequestedOffer
            ? requestedOfferId
            : prev.serviceOfferId &&
                data.some(
                  (offer) => String(offer.id) === String(prev.serviceOfferId),
                )
              ? prev.serviceOfferId
              : data.length > 0
                ? String(data[0].id)
                : "",
        }));
      } catch (error) {
        console.error(error);

        setPageError(error.message || "Fehler beim Laden der Serviceangebote.");
      } finally {
        setIsLoadingOffers(false);
      }
    }

    if (currentUser) {
      loadOffers();
    }
  }, [currentUser, searchParams]);

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

    setPageError("");
    setSuccessMessage("");
  }

  function validateForm() {
    const nextFieldErrors = {};

    if (!formData.serviceOfferId) {
      nextFieldErrors.serviceOfferId = "Bitte wähle ein Serviceangebot aus.";
    }

    if (!formData.subject.trim()) {
      nextFieldErrors.subject = "Bitte gib einen Betreff ein.";
    } else if (formData.subject.trim().length < 3) {
      nextFieldErrors.subject =
        "Der Betreff muss mindestens 3 Zeichen lang sein.";
    } else if (formData.subject.trim().length > 100) {
      nextFieldErrors.subject =
        "Der Betreff darf maximal 100 Zeichen lang sein.";
    }

    if (!REQUEST_PRIORITIES.includes(formData.priority)) {
      nextFieldErrors.priority = "Bitte wähle eine gültige Priorität aus.";
    }

    if (!formData.message.trim()) {
      nextFieldErrors.message = "Bitte beschreibe dein Anliegen.";
    } else if (formData.message.trim().length < 10) {
      nextFieldErrors.message =
        "Die Nachricht muss mindestens 10 Zeichen lang sein.";
    } else if (formData.message.trim().length > 1000) {
      nextFieldErrors.message =
        "Die Nachricht darf maximal 1000 Zeichen lang sein.";
    }

    setFieldErrors(nextFieldErrors);
    return Object.keys(nextFieldErrors).length === 0;
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setPageError("");
    setSuccessMessage("");

    if (!validateForm()) {
      return;
    }

    try {
      setIsSubmitting(true);

      const response = await apiFetch("/api/requests", {
        method: "POST",
        body: JSON.stringify({
          serviceOfferId: Number(formData.serviceOfferId),
          subject: formData.subject.trim(),
          priority: formData.priority,
          message: formData.message.trim(),
        }),
      });

      if (!response.ok) {
        const apiError = await readApiError(
          response,
          "Die Serviceanfrage konnte nicht gesendet werden.",
        );
        setFieldErrors(apiError.fieldErrors || {});

        throw new Error(apiError.message);
      }

      const createdRequest = await response.json();

      setSuccessMessage("Deine Serviceanfrage wurde erfolgreich eingereicht.");
      setFormData((prev) => ({
        ...INITIAL_FORM_DATA,
        serviceOfferId: prev.serviceOfferId,
      }));

      navigate("/confirmation", {
        replace: true,
        state: {
          id: createdRequest.id,
          subject: createdRequest.subject,
          category: createdRequest.category,
          status: createdRequest.status,
          createdAt: createdRequest.createdAt,
        },
      });
    } catch (error) {
      console.error(error);
      setPageError(
        error.message || "Die Serviceanfrage konnte nicht gesendet werden.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  if (!currentUser) {
    return null;
  }

  return (
    <section className="content-card" aria-labelledby="request-page-heading">
      <h1 id="request-page-heading">Serviceanfrage einreichen</h1>
      <p>
        Reiche hier dein Anliegen online ein. Alle Pflichtfelder sind
        entsprechend gekennzeichnet.
      </p>

      {isLoadingOffers ? (
        <p>Serviceangebote werden geladen...</p>
      ) : pageError ? (
        <p className="error-message" role="alert">
          {pageError}
        </p>
      ) : offers.length === 0 ? (
        <p>Aktuell stehen keine Serviceangebote zur Verfügung.</p>
      ) : (
        <form onSubmit={handleSubmit} noValidate className="form-layout">
          {successMessage && (
            <p className="success-message" role="status">
              {successMessage}
            </p>
          )}

          <div className="form-group">
            <label htmlFor="serviceOfferId">Serviceangebot *</label>
            <select
              id="serviceOfferId"
              name="serviceOfferId"
              value={formData.serviceOfferId}
              onChange={handleChange}
              aria-invalid={fieldErrors.serviceOfferId ? "true" : "false"}
              aria-describedby={
                fieldErrors.serviceOfferId
                  ? "serviceOfferId-error"
                  : "serviceOfferId-hint"
              }
              disabled={isSubmitting}
            >
              {offers.map((offer) => (
                <option key={offer.id} value={offer.id}>
                  {offer.title} ({offer.category})
                </option>
              ))}
            </select>

            <small id="serviceOfferId-hint">
              Wähle das passende Angebot für dein Anliegen aus.
            </small>
            {fieldErrors.serviceOfferId && (
              <p id="serviceOfferId-error" className="field-error" role="alert">
                {fieldErrors.serviceOfferId}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="subject">Betreff *</label>

            <input
              id="subject"
              name="subject"
              type="text"
              value={formData.subject}
              onChange={handleChange}
              maxLength={100}
              aria-invalid={fieldErrors.subject ? "true" : "false"}
              aria-describedby={
                fieldErrors.subject ? "subject-error" : "subject-hint"
              }
              disabled={isSubmitting}
            />

            <small id="subject-hint">
              Fasse dein Anliegen kurz und eindeutig zusammen.
            </small>
            {fieldErrors.subject && (
              <p id="subject-error" className="field-error" role="alert">
                {fieldErrors.subject}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="priority">Priorität *</label>

            <select
              id="priority"
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              aria-invalid={fieldErrors.priority ? "true" : "false"}
              aria-describedby={
                fieldErrors.priority ? "priority-error" : "priority-hint"
              }
              disabled={isSubmitting}
            >
              {REQUEST_PRIORITIES.map((priority) => (
                <option key={priority} value={priority}>
                  {priority}
                </option>
              ))}
            </select>

            <small id="priority-hint">
              Wähle die Dringlichkeit deiner Anfrage aus.
            </small>
            {fieldErrors.priority && (
              <p id="priority-error" className="field-error" role="alert">
                {fieldErrors.priority}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="message">Nachricht *</label>
            <textarea
              id="message"
              name="message"
              rows="6"
              value={formData.message}
              onChange={handleChange}
              maxLength={1000}
              aria-invalid={fieldErrors.message ? "true" : "false"}
              aria-describedby={
                fieldErrors.message ? "message-error" : "message-hint"
              }
              disabled={isSubmitting}
            />

            <small id="message-hint">
              Beschreibe dein Anliegen möglichst konkret.
            </small>
            {fieldErrors.message && (
              <p id="message-error" className="field-error" role="alert">
                {fieldErrors.message}
              </p>
            )}
          </div>

          <div className="button-row">
            <button
              type="submit"
              className="primary-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Anfrage wird gesendet..." : "Anfrage absenden"}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}

export default RequestPage;
