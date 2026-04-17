import { Navigate, Outlet, useOutletContext } from "react-router-dom";

function RequireUser() {
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

  if (role !== "USER") {
    return <Navigate to="/login" replace />;
  }

  return <Outlet context={outletContext} />;
}

export default RequireUser;
