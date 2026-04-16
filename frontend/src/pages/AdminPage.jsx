import { useOutletContext } from "react-router-dom";
import AdminRequests from "../AdminRequests";
import AdminOffers from "../AdminOffers";
import AdminUsers from "../AdminUsers";

function AdminPage() {
  return (
    <>
      <section className="content-card">
        <h2>Adminbereich</h2>
        <p>
          Hier können eingegangene Anfragen, Serviceangebote und Benutzerrollen
          verwaltet werden.
        </p>
      </section>

      <div className="section-spacing">
        <AdminRequests refreshKey={0} />
      </div>

      <div className="section-spacing">
        <AdminOffers />
      </div>

      <div className="section-spacing">
        <AdminUsers />
      </div>
    </>
  );
}

export default AdminPage;