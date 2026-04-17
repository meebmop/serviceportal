import { Outlet, useOutletContext } from "react-router-dom";

function AdminLayout() {
  const outletContext = useOutletContext();

  return <Outlet context={outletContext} />;
}

export default AdminLayout;
