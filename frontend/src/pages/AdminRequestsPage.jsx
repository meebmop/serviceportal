import { Fragment, useEffect, useState } from "react";
import { apiFetch, readApiError } from "../api";
import { REQUEST_STATUSES } from "../constants/requestOptions";

const DEFAULT_PAGE_SIZE = 20;

function formatDate(value) {
  if (!value) {
    return "—";
  }

  try {
    return new Date(value).toLocaleString("de-DE");
  } catch {
    return "—";
  }
}

function AdminRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [statusEdits, setStatusEdits] = useState({});
  const [commentEdits, setCommentEdits] = useState({});
  const [expandedRequestId, setExpandedRequestId] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [pageLoading, setPageLoading] = useState(false);
  const [pageError, setPageError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [statusMessage, setStatusMessage] = useState("");
  const [savingRequestId, setSavingRequestId] = useState(null);

  useEffect(() => {
    loadRequests(0);
  }, []);

  async function loadRequests(targetPage = page, options = {}) {
    const { showPageLoading = false } = options;

    try {
      if (showPageLoading) {
        setPageLoading(true);
        setStatusMessage(`Seite ${targetPage + 1} wird geladen...`);
      } else {
        setLoading(true);
      }

      setPageError("");
      setSuccessMessage("");

      const response = await apiFetch(
        `/api/requests?page=${targetPage}&size=${DEFAULT_PAGE_SIZE}`,
      );

      if (!response.ok) {
        const apiError = await readApiError(
          response,
          "Anfragen konnten nicht geladen werden.",
        );
        throw new Error(apiError.message);
      }

      const data = await response.json();
      setRequests(data);
      setPage(targetPage);

      const nextStatusEdits = {};
      const nextCommentEdits = {};

      data.forEach((request) => {
        nextStatusEdits[request.id] = request.status;
        nextCommentEdits[request.id] = request.adminComment || "";
      });

      setStatusEdits(nextStatusEdits);
      setCommentEdits(nextCommentEdits);

      if (
        expandedRequestId &&
        !data.some((request) => request.id === expandedRequestId)
      ) {
        setExpandedRequestId(null);
      }

      if (showPageLoading) {
        setStatusMessage(`Seite ${targetPage + 1} wurde geladen.`);
      }
    } catch (error) {
      console.error(error);
      setPageError(error.message || "Anfragen konnten nicht geladen werden.");
      setStatusMessage("");
      setRequests([]);
    } finally {
      setLoading(false);
      setPageLoading(false);
    }
  }

  function handleStatusChange(requestId, value) {
    setStatusEdits((prev) => ({
      ...prev,
      [requestId]: value,
    }));

    setPageError("");
    setSuccessMessage("");
  }

  function handleCommentChange(requestId, value) {
    setCommentEdits((prev) => ({
      ...prev,
      [requestId]: value,
    }));

    setPageError("");
    setSuccessMessage("");
  }

  async function handleSave(requestId) {
    const selectedStatus = statusEdits[requestId];
    const adminComment = commentEdits[requestId] ?? "";

    if (!REQUEST_STATUSES.includes(selectedStatus)) {
      setPageError("Bitte wähle einen gültigen Status aus.");
      return;
    }

    try {
      setSavingRequestId(requestId);
      setPageError("");
      setSuccessMessage("");

      const response = await apiFetch(`/api/requests/${requestId}/status`, {
        method: "PUT",
        body: JSON.stringify({
          status: selectedStatus,
          adminComment,
        }),
      });

      if (!response.ok) {
        const apiError = await readApiError(
          response,
          "Die Anfrage konnte nicht aktualisiert werden.",
        );
        throw new Error(apiError.message);
      }

      const updatedRequest = await response.json();

      setRequests((prev) =>
        prev.map((request) =>
          request.id === requestId ? updatedRequest : request,
        ),
      );

      setStatusEdits((prev) => ({
        ...prev,
        [requestId]: updatedRequest.status,
      }));

      setCommentEdits((prev) => ({
        ...prev,
        [requestId]: updatedRequest.adminComment || "",
      }));

      setSuccessMessage(
        `Anfrage #${requestId} wurde erfolgreich aktualisiert.`,
      );
    } catch (error) {
      console.error(error);
      setPageError(
        error.message || "Die Anfrage konnte nicht aktualisiert werden.",
      );
    } finally {
      setSavingRequestId(null);
    }
  }

  const hasPreviousPage = page > 0;
  const hasNextPage = requests.length === DEFAULT_PAGE_SIZE;

  async function handlePreviousPage() {
    if (!hasPreviousPage || pageLoading) {
      return;
    }

    await loadRequests(page - 1, { showPageLoading: true });
  }

  async function handleNextPage() {
    if (!hasNextPage || pageLoading) {
      return;
    }

    await loadRequests(page + 1, { showPageLoading: true });
  }

  return (
    <section className="content-card" aria-labelledby="admin-requests-heading">
      <h1 id="admin-requests-heading">Eingehende Serviceanfragen</h1>
      <p>
        Hier können eingegangene Serviceanfragen eingesehen und bearbeitet
        werden.
      </p>

      {pageError && (
        <p className="error-message" role="alert">
          {pageError}
        </p>
      )}

      {successMessage && (
        <p className="success-message" role="status">
          {successMessage}
        </p>
      )}

      <div className="sr-only" role="status" aria-live="polite">
        {statusMessage}
      </div>

      {loading ? (
        <p>Anfragen werden geladen...</p>
      ) : requests.length === 0 ? (
        <p>Aktuell liegen keine Serviceanfragen vor.</p>
      ) : (
        <>
          <div
            className="pagination-bar"
            aria-label="Seitennavigation für eingehende Serviceanfragen"
          >
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

          <div className="table-wrapper">
            <table className="requests-table">
              <caption className="sr-only">
                Übersicht aller eingegangenen Serviceanfragen mit
                Bearbeitungsoptionen.
              </caption>

              <thead>
                <tr>
                  <th scope="col">ID</th>
                  <th scope="col">Datum</th>
                  <th scope="col">Kategorie</th>
                  <th scope="col">Priorität</th>
                  <th scope="col">Status</th>
                  <th scope="col">Betreff</th>
                  <th scope="col">Aktion</th>
                </tr>
              </thead>

              <tbody>
                {requests.map((request) => {
                  const isSaving = savingRequestId === request.id;
                  const isExpanded = expandedRequestId === request.id;

                  return (
                    <Fragment key={request.id}>
                      <tr>
                        <td data-label="ID">#{request.id}</td>
                        <td data-label="Datum">
                          {formatDate(request.createdAt)}
                        </td>
                        <td data-label="Kategorie">{request.category}</td>
                        <td data-label="Priorität">{request.priority}</td>
                        <td data-label="Status">
                          <span
                            className={`status-badge status-${request.status
                              ?.toLowerCase()
                              .replace(/\s+/g, "-")}`}
                          >
                            {request.status}
                          </span>
                        </td>
                        <td data-label="Betreff">{request.subject}</td>
                        <td data-label="Aktion">
                          <button
                            type="button"
                            className="link-button small-button"
                            onClick={() =>
                              setExpandedRequestId(
                                isExpanded ? null : request.id,
                              )
                            }
                            aria-expanded={isExpanded}
                            aria-controls={`request-details-${request.id}`}
                          >
                            {isExpanded ? "Ausblenden" : "Anzeigen"}
                          </button>
                        </td>
                      </tr>

                      {isExpanded && (
                        <tr id={`request-details-${request.id}`}>
                          <td colSpan="7" className="request-details-cell">
                            <div className="request-details-box">
                              <p>
                                <strong>Anfragende E-Mail:</strong>{" "}
                                {request.userEmail}
                              </p>
                              <p>
                                <strong>Erstellt am:</strong>{" "}
                                {formatDate(request.createdAt)}
                              </p>
                              <p>
                                <strong>Zuletzt aktualisiert:</strong>{" "}
                                {formatDate(request.updatedAt)}
                              </p>
                              <p>
                                <strong>Zuletzt bearbeitet von:</strong>{" "}
                                {request.updatedBy || "—"}
                              </p>

                              <div className="request-message-box">
                                <strong>Nachricht</strong>
                                <p>{request.message}</p>
                              </div>

                              <div className="form-group section-spacing">
                                <label htmlFor={`status-${request.id}`}>
                                  Status
                                </label>
                                <select
                                  id={`status-${request.id}`}
                                  value={
                                    statusEdits[request.id] || request.status
                                  }
                                  onChange={(event) =>
                                    handleStatusChange(
                                      request.id,
                                      event.target.value,
                                    )
                                  }
                                  disabled={isSaving}
                                >
                                  {REQUEST_STATUSES.map((status) => (
                                    <option key={status} value={status}>
                                      {status}
                                    </option>
                                  ))}
                                </select>
                              </div>

                              <div className="form-group">
                                <label htmlFor={`comment-${request.id}`}>
                                  Interner Bearbeitungskommentar
                                </label>
                                <textarea
                                  id={`comment-${request.id}`}
                                  rows="4"
                                  maxLength={500}
                                  value={commentEdits[request.id] ?? ""}
                                  onChange={(event) =>
                                    handleCommentChange(
                                      request.id,
                                      event.target.value,
                                    )
                                  }
                                  disabled={isSaving}
                                />
                              </div>

                              <div className="button-row">
                                <button
                                  type="button"
                                  className="primary-button small-button"
                                  onClick={() => handleSave(request.id)}
                                  disabled={isSaving}
                                >
                                  {isSaving
                                    ? "Speichert..."
                                    : "Änderungen speichern"}
                                </button>
                              </div>
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
        </>
      )}
    </section>
  );
}

export default AdminRequestsPage;
