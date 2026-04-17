import { useEffect, useState } from "react";
import { apiFetch, readApiError } from "../api";
import { useOutletContext } from "react-router-dom";

function AdminUsersPage() {
  const { currentUser } = useOutletContext();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [updatingUserId, setUpdatingUserId] = useState(null);
  const [deletingUserId, setDeletingUserId] = useState(null);

  async function loadUsers() {
    try {
      setError("");
      const response = await apiFetch("/api/users");

      if (!response.ok) {
        const apiError = await readApiError(
          response,
          "Benutzer konnten nicht geladen werden.",
        );
        throw new Error(apiError.message);
      }

      const data = await response.json();
      setUsers(data);
    } catch (err) {
      console.error(err);
      setError(err.message || "Fehler beim Laden der Benutzer.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  async function handleRoleChange(user, newRole) {
    setError("");
    setSuccessMessage("");
    setUpdatingUserId(user.id);

    try {
      const response = await apiFetch(`/api/users/${user.id}/role`, {
        method: "PUT",
        body: JSON.stringify({ role: newRole }),
      });

      if (!response.ok) {
        const fallbackMessage =
          response.status === 401
            ? "Bitte melde dich erneut an."
            : response.status === 403
              ? "Du hast keine Berechtigung, Benutzerrollen zu ändern."
              : response.status === 400
                ? "Die ausgewählte Rolle ist ungültig oder darf nicht gesetzt werden."
                : "Die Benutzerrolle konnte nicht aktualisiert werden.";

        const apiError = await readApiError(response, fallbackMessage);
        throw new Error(apiError.message);
      }

      const updatedUser = await response.json();

      setUsers((prev) =>
        prev.map((item) => (item.id === user.id ? updatedUser : item)),
      );

      setSuccessMessage(`Rolle von ${updatedUser.name} wurde aktualisiert.`);
    } catch (err) {
      console.error(err);
      setError(err.message || "Fehler beim Aktualisieren der Rolle.");
    } finally {
      setUpdatingUserId(null);
    }
  }

  async function handleDeleteUser(user) {
    const confirmed = window.confirm(
      `Möchtest du den Benutzer ${user.name} wirklich löschen?`,
    );

    if (!confirmed) {
      return;
    }

    setError("");
    setSuccessMessage("");
    setDeletingUserId(user.id);

    try {
      const response = await apiFetch(`/api/users/${user.id}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        const fallbackMessage =
          response.status === 401
            ? "Bitte melde dich erneut an."
            : response.status === 403
              ? "Du hast keine Berechtigung, Benutzer zu löschen."
              : response.status === 400
                ? "Dieser Benutzer kann nicht gelöscht werden."
                : response.status === 404
                  ? "Der Benutzer wurde nicht gefunden."
                  : "Der Benutzer konnte nicht gelöscht werden.";

        const apiError = await readApiError(response, fallbackMessage);
        throw new Error(apiError.message);
      }

      setUsers((prev) => prev.filter((item) => item.id !== user.id));
      setSuccessMessage(`Benutzer ${user.name} wurde gelöscht.`);
    } catch (err) {
      console.error(err);
      setError(err.message || "Fehler beim Löschen des Benutzers.");
    } finally {
      setDeletingUserId(null);
    }
  }

  if (loading) {
    return (
      <section className="content-card">
        <h2>Benutzer verwalten</h2>
        <p>Lade Benutzer...</p>
      </section>
    );
  }

  return (
    <section className="content-card" aria-labelledby="admin-users-heading">
      <h2 id="admin-users-heading">Benutzer verwalten</h2>

      {successMessage && (
        <p className="success-message" role="status" aria-live="polite">
          {successMessage}
        </p>
      )}

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {users.length === 0 ? (
        <p>Es sind aktuell keine Benutzer vorhanden.</p>
      ) : (
        <div className="table-wrapper">
          <table className="requests-table">
            <caption className="sr-only">
              Liste aller Benutzer mit ID, Name, E-Mail und Rolle.
            </caption>
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">Name</th>
                <th scope="col">E-Mail</th>
                <th scope="col">Rolle</th>
                <th scope="col">Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => {
                const isUpdating = updatingUserId === user.id;
                const isDeleting = deletingUserId === user.id;
                const isBusy = isUpdating || isDeleting;
                const isCurrentUser = currentUser?.id === user.id;

                return (
                  <tr key={user.id}>
                    <td>#{user.id}</td>
                    <td>
                      {user.name}
                      {isCurrentUser ? " (du)" : ""}
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <label
                        htmlFor={`role-select-${user.id}`}
                        className="sr-only"
                      >
                        Rolle für {user.name} ändern
                      </label>
                      <select
                        id={`role-select-${user.id}`}
                        value={user.role}
                        onChange={(event) =>
                          handleRoleChange(user, event.target.value)
                        }
                        aria-describedby={`role-help-${user.id}`}
                        disabled={isBusy}
                      >
                        <option value="USER">USER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                      <span id={`role-help-${user.id}`} className="sr-only">
                        Aktuelle Rolle: {user.role}
                      </span>
                      {isUpdating && (
                        <p role="status" aria-live="polite">
                          Rolle wird gespeichert...
                        </p>
                      )}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleDeleteUser(user)}
                        disabled={isBusy}
                        aria-label={`Benutzer ${user.name} löschen`}
                      >
                        {isDeleting ? "Lösche..." : "Löschen"}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default AdminUsersPage;
