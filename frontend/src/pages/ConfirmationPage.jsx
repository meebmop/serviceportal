import { Link, useLocation } from "react-router-dom";

function ConfirmationPage() {
  const location = useLocation();
  const requestData = location.state;

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

  if (!requestData) {
    return (
      <section className="content-card">
        <h2>Keine Bestätigungsdaten vorhanden</h2>
        <p>
          Diese Seite kann nur direkt nach dem Absenden einer Serviceanfrage
          angezeigt werden.
        </p>

        <div className="button-row">
          <Link to="/request" className="primary-button">
            Anfrage stellen
          </Link>
          <Link to="/my-requests" className="secondary-button">
            Meine Anfragen
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section className="content-card">
      <h2>Anfrage erfolgreich gesendet</h2>
      <p>
        Deine Serviceanfrage wurde erfolgreich übermittelt. Nachfolgend findest
        du die wichtigsten Angaben zu deiner Anfrage.
      </p>

      <div className="confirmation-box">
        <p>
          <strong>Anfrage-ID:</strong> #{requestData.id}
        </p>
        <p>
          <strong>Betreff:</strong> {requestData.subject}
        </p>
        <p>
          <strong>Kategorie:</strong> {requestData.category}
        </p>
        <p>
          <strong>Status:</strong> {requestData.status}
        </p>
        <p>
          <strong>Erstellt am:</strong> {formatDateTime(requestData.createdAt)}
        </p>
      </div>

      <div className="button-row">
        <Link to="/my-requests" className="primary-button">
          Zu meinen Anfragen
        </Link>
        <Link to="/request" className="secondary-button">
          Weitere Anfrage stellen
        </Link>
      </div>
    </section>
  );
}

export default ConfirmationPage;