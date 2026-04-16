import { useEffect, useState } from "react";
import { Link, useOutletContext } from "react-router-dom";
import { apiFetch } from "./api";

function OfferPreview({ compact = false }) {
  const { currentUser } = useOutletContext();
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadOffers() {
      try {
        setLoading(true);
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

    loadOffers();
  }, []);

  if (loading) {
    return (
      <section className="content-card compact-section">
        <h2>Serviceangebote</h2>
        <p>Lade Serviceangebote...</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="content-card compact-section">
        <h2>Serviceangebote</h2>
        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  if (offers.length === 0) {
    return (
      <section className="content-card compact-section">
        <h2>Serviceangebote</h2>
        <p>Aktuell sind noch keine Serviceangebote verfügbar.</p>
      </section>
    );
  }

  const visibleOffers = compact ? offers.slice(0, 3) : offers;

  function getOfferTarget(offerId) {
    return currentUser?.role === "USER"
      ? `/request?offerId=${offerId}`
      : `/login?redirect=${encodeURIComponent(`/request?offerId=${offerId}`)}`;
  }

  return (
    <section
      className="content-card compact-section"
      aria-labelledby="offer-preview-heading"
    >
      <div className="section-headline-row">
        <h2 id="offer-preview-heading">Serviceangebote</h2>

        {currentUser?.role === "USER" && (
          <Link to="/request" className="text-link">
            Zur Anfrage
          </Link>
        )}
      </div>

      <div className="offers-grid compact-offers-grid">
        {visibleOffers.map((offer) => (
          <Link
            key={offer.id}
            to={getOfferTarget(offer.id)}
            className="offer-card-link"
            aria-label={`${offer.title} auswählen`}
          >
            <article className="offer-card compact-offer-card interactive-offer-card">
              <span className="offer-badge">{offer.category}</span>
              <h3>{offer.title}</h3>
              <p className="offer-description">{offer.description}</p>
              <span className="offer-card-action">Angebot auswählen</span>
            </article>
          </Link>
        ))}
      </div>
    </section>
  );
}

export default OfferPreview;