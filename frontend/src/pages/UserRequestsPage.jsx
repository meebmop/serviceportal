import { Fragment, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "../api";

const PAGE_SIZE = 20;

function UserRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [offers, setOffers] = useState([]);
  const [page, setPage] = useState(0);

  const [loading, setLoading] = useState(true);
  const [pageLoading, setPageLoading] = useState(false);
  const [error, setError] = useState("");
  const [expandedRequestId, setExpandedRequestId] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const [statusMessage, setStatusMessage] = useState("");

  async function loadData(targetPage = page, options = {}) {
    const { showRefreshState = false, showPageLoading = false } = options;

    if (showRefreshState) {
      setRefreshing(true);
      setStatusMessage("Anfragen werden aktualisiert...");
    }

    if (showPageLoading) {
      setPageLoading(true);
      setStatusMessage(`Seite ${targetPage + 1} wird geladen...`);
    }

    try {
      if (!showRefreshState && !showPageLoading) {
        setLoading(true);
      }

      setError("");

      const [requestsResponse, offersResponse] = await Promise.all([
        apiFetch(`/api/requests/my?page=${targetPage}&size=${PAGE_SIZE}`),
        apiFetch("/api/offers"),
      ]);

      if (!requestsResponse.ok) {
        throw new Error("Anfragen konnten nicht geladen werden.");
      }

      if (!offersResponse.ok) {
        throw new Error("Serviceangebote konnten nicht geladen werden.");
      }

      const [requestsData, offersData] = await Promise.all([
        requestsResponse.json(),
        offersResponse.json(),
      ]);

      setRequests(requestsData);
      setOffers(offersData);
      setPage(targetPage);
      setExpandedRequestId(null);

      if (showRefreshState) {
        setStatusMessage("Anfragen wurden erfolgreich aktualisiert.");
      } else if (showPageLoading) {
        setStatusMessage(`Seite ${targetPage + 1} wurde geladen.`);
      }
    } catch (err) {
      console.error(err);
      setError(err.message || "Fehler beim Laden der Anfragen.");
      setStatusMessage("");
    } finally {
      setLoading(false);
      setRefreshing(false);
      setPageLoading(false);
    }
  }

  useEffect(() => {
    loadData(0);
  }, []);

  const offerMap = useMemo(() => {
    return offers.reduce((map, offer) => {
      map[offer.id] = offer.title;
      return map;
    }, {});
  }, [offers]);

  const myRequests = useMemo(() => {
    return [...requests];
  }, [requests]);

  const hasPreviousPage = page > 0;
  const hasNextPage = requests.length === PAGE_SIZE;

  function toggleMessage(requestId) {
    setExpandedRequestId((prev) => (prev === requestId ? null : requestId));
  }

  function formatDateTime(value) {
    if (!value) {
      return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat("de-DE", {
      dateStyle: "short",
      timeStyle: "short",
    }).format(date);
  }

  async function handlePreviousPage() {
    if (!hasPreviousPage || pageLoading) {
      return;
    }

    await loadData(page - 1, { showPageLoading: true });
  }

  async function handleNextPage() {
    if (!hasNextPage || pageLoading) {
      return;
    }

    await loadData(page + 1, { showPageLoading: true });
  }

  if (loading) {
    return (
      <section className="content-card">
        <h2>Meine Anfragen</h2>
        <p>Lade deine Anfragen...</p>
      </section>
    );
  }

  if (error && myRequests.length === 0) {
    return (
      <section className="content-card">
        <h2>Meine Anfragen</h2>
        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  return (
    <section className="content-card" aria-labelledby="my-requests-heading">
      <div className="section-header">
        <div>
          <h2 id="my-requests-heading">Meine Anfragen</h2>
          <p>
            Hier findest du den Bearbeitungsstand deiner eingereichten
            Serviceanfragen.
          </p>
        </div>

        <div className="header-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => loadData(page, { showRefreshState: true })}
            disabled={refreshing || pageLoading}
            aria-describedby="refresh-requests-status"
          >
            {refreshing ? "Aktualisiere..." : "Aktualisieren"}
          </button>
          <Link to="/request" className="primary-button">
            Neue Anfrage
          </Link>
        </div>
      </div>

      <div
        id="refresh-requests-status"
        className="sr-only"
        role="status"
        aria-live="polite"
      >
        {statusMessage}
      </div>

      {error && myRequests.length > 0 && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      <div className="pagination-bar" aria-label="Seitennavigation für meine Anfragen">
        <button
          type="button"
          className="secondary-button"
          onClick={handlePreviousPage}
          disabled={!hasPreviousPage || pageLoading}
        >
          Vorherige Seite
        </button>

        <p className="pagination-info" role="status" aria-live="polite">
          Seite {page + 1}
          {pageLoading ? " wird geladen..." : ""}
        </p>

        <button
          type="button"
          className="secondary-button"
          onClick={handleNextPage}
          disabled={!hasNextPage || pageLoading}
        >
          Nächste Seite
        </button>
      </div>

      {myRequests.length === 0 ? (
        <div className="empty-state">
          <p>Du hast noch keine Serviceanfragen gestellt.</p>
          <Link to="/request" className="primary-button">
            Jetzt Anfrage erstellen
          </Link>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="requests-table">
            <caption className="sr-only">
              Übersicht deiner eingereichten Serviceanfragen mit ID, Datum,
              Service, Priorität, Betreff, Status und Nachricht.
            </caption>
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">Datum</th>
                <th scope="col">Service</th>
                <th scope="col">Priorität</th>
                <th scope="col">Betreff</th>
                <th scope="col">Status</th>
                <th scope="col">Nachricht</th>
              </tr>
            </thead>
            <tbody>
              {myRequests.map((request) => {
                const isExpanded = expandedRequestId === request.id;
                const offerTitle =
                  offerMap[request.serviceOfferId] ||
                  `Angebot ${request.serviceOfferId}`;

                return (
                  <Fragment key={request.id}>
                    <tr>
                      <td>#{request.id}</td>
                      <td>{formatDateTime(request.createdAt)}</td>
                      <td>{offerTitle}</td>
                      <td>{request.priority || "Normal"}</td>
                      <td>{request.subject}</td>
                      <td>
                        <span
                          className={`status-badge status-${request.status
                            ?.toLowerCase()
                            .replace(/\s+/g, "-")}`}
                        >
                          {request.status}
                        </span>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="link-button"
                          onClick={() => toggleMessage(request.id)}
                          aria-expanded={isExpanded}
                          aria-controls={`request-message-${request.id}`}
                        >
                          {isExpanded ? "Ausblenden" : "Anzeigen"}
                        </button>
                      </td>
                    </tr>

                    {isExpanded && (
                      <tr id={`request-message-${request.id}`}>
                        <td colSpan="7">
                          <div className="request-message-box">
                            <strong>Nachricht:</strong>
                            <p>{request.message}</p>

                            <strong>Zuletzt aktualisiert:</strong>
                            <p>{formatDateTime(request.updatedAt)}</p>

                            <strong>Bearbeitet von:</strong>
                            <p>{request.updatedBy || "-"}</p>

                            {request.adminComment && (
                              <>
                                <strong>Rückmeldung zur Anfrage:</strong>
                                <p>{request.adminComment}</p>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default UserRequestsPage;