import { useEffect, useState } from "react";
import { apiFetch } from "./api";

function ServiceOffers() {
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
      <section className="content-card">
        <h2>Verfügbare Serviceangebote</h2>
        <p>Lade Serviceangebote...</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="content-card">
        <h2>Verfügbare Serviceangebote</h2>
        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  return (
    <section className="content-card" aria-labelledby="offers-heading">
      <h2 id="offers-heading">Verfügbare Serviceangebote</h2>

      {offers.length === 0 ? (
        <p>Aktuell sind noch keine Serviceangebote hinterlegt.</p>
      ) : (
        <div className="offers-grid">
          {offers.map((offer) => (
            <article key={offer.id} className="offer-card">
              <h3>{offer.title}</h3>
              <p>
                <strong>Kategorie:</strong> {offer.category}
              </p>
              <p>{offer.description}</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default ServiceOffers;