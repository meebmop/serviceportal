import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";
import Layout from "./Layout";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RequestPage from "./pages/RequestPage";
import ConfirmationPage from "./pages/ConfirmationPage";
import UserRequestsPage from "./pages/UserRequestsPage";
import AdminLayout from "./pages/AdminLayout";
import AdminOverviewPage from "./pages/AdminOverviewPage";
import AdminOffersPage from "./pages/AdminOffersPage";
import AdminRequestsPage from "./pages/AdminRequestsPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import RequireUser from "./components/RequireUser";
import RequireAdmin from "./components/RequireAdmin";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />

          <Route element={<RequireUser />}>
            <Route path="request" element={<RequestPage />} />
            <Route path="confirmation" element={<ConfirmationPage />} />
            <Route path="my-requests" element={<UserRequestsPage />} />
          </Route>

          <Route element={<RequireAdmin />}>
            <Route path="admin" element={<AdminLayout />}>
              <Route index element={<AdminOverviewPage />} />
              <Route path="offers" element={<AdminOffersPage />} />
              <Route path="requests" element={<AdminRequestsPage />} />
              <Route path="users" element={<AdminUsersPage />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
