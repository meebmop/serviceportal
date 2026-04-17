import { Navigate, Outlet, useOutletContext } from "react-router-dom";

function RequireAdmin() {
  const outletContext = useOutletContext();
  const { role, authLoading } = outletContext;

  if (authLoading) {
    return (
      <section className="content-card">
        <h2>Lade...</h2>
        <p>Benutzerstatus wird geprüft.</p>
      </section>
    );
  }

  if (role !== "ADMIN") {
    return <Navigate to="/login" replace />;
  }

  return <Outlet context={outletContext} />;
}

export default RequireAdmin;
