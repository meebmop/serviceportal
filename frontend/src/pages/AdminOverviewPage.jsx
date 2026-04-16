import { useEffect, useState } from "react";
import { apiFetch, readApiError } from "../api";

const PAGE_SIZE = 20;

function AdminOverviewPage() {
  const [stats, setStats] = useState({
    offers: 0,
    requests: 0,
    users: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadStats() {
      try {
        setError("");

        const [offersResponse, requestsResponse, usersResponse] =
          await Promise.all([
            apiFetch("/api/offers"),
            apiFetch(`/api/requests?page=0&size=${PAGE_SIZE}`),
            apiFetch("/api/users"),
          ]);

        if (!offersResponse.ok) {
          const apiError = await readApiError(
            offersResponse,
            "Serviceangebote konnten nicht geladen werden."
          );
          throw new Error(apiError.message);
        }

        if (!requestsResponse.ok) {
          const apiError = await readApiError(
            requestsResponse,
            "Anfragen konnten nicht geladen werden."
          );
          throw new Error(apiError.message);
        }

        if (!usersResponse.ok) {
          const apiError = await readApiError(
            usersResponse,
            "Benutzer konnten nicht geladen werden."
          );
          throw new Error(apiError.message);
        }

        const [offers, requests, users] = await Promise.all([
          offersResponse.json(),
          requestsResponse.json(),
          usersResponse.json(),
        ]);

        setStats({
          offers: offers.length,
          requests: requests.length,
          users: users.length,
        });
      } catch (err) {
        console.error(err);
        setError(err.message || "Fehler beim Laden der Übersicht.");
      } finally {
        setLoading(false);
      }
    }

    loadStats();
  }, []);

  if (loading) {
    return (
      <section className="content-card">
        <h2>Übersicht</h2>
        <p>Lade Admin-Daten...</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="content-card">
        <h2>Übersicht</h2>
        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  return (
    <section className="content-card" aria-labelledby="admin-overview-heading">
      <h2 id="admin-overview-heading">Übersicht</h2>

      <div className="admin-stats-grid">
        <article className="admin-stat-card">
          <span className="admin-stat-label">Serviceangebote</span>
          <strong className="admin-stat-value">{stats.offers}</strong>
        </article>

        <article className="admin-stat-card">
          <span className="admin-stat-label">Anfragen</span>
          <strong className="admin-stat-value">{stats.requests}</strong>
        </article>

        <article className="admin-stat-card">
          <span className="admin-stat-label">Benutzer</span>
          <strong className="admin-stat-value">{stats.users}</strong>
        </article>
      </div>
    </section>
  );
}

export default AdminOverviewPage;