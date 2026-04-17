import { Link, NavLink, Outlet } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import AccessibilityToolbar from "./AccessibilityToolbar";
import serviceportalLogo from "./assets/serviceportal-logo.svg";
import { apiFetch } from "./api";

function Layout() {
  const [currentUser, setCurrentUser] = useState(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);

  const accountMenuRef = useRef(null);
  const accountButtonRef = useRef(null);
  const logoutButtonRef = useRef(null);

  async function loadCurrentUser() {
    try {
      const response = await apiFetch("/api/auth/me");

      if (!response.ok) {
        localStorage.removeItem("currentUser");
        setCurrentUser(null);
        return;
      }

      const user = await response.json();
      localStorage.setItem("currentUser", JSON.stringify(user));
      setCurrentUser(user);
    } catch (error) {
      console.error("Fehler beim Laden des aktuellen Users:", error);
      localStorage.removeItem("currentUser");
      setCurrentUser(null);
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleLogout() {
    try {
      const response = await apiFetch("/api/auth/logout", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Logout fehlgeschlagen");
      }
    } catch (error) {
      console.error("Fehler beim Logout:", error);
    } finally {
      localStorage.removeItem("currentUser");
      setCurrentUser(null);
      setAccountMenuOpen(false);
      window.dispatchEvent(new Event("user-changed"));
      window.location.href = "/login";
    }
  }

  function openAccountMenu() {
    setAccountMenuOpen(true);
  }

  function closeAccountMenu({ returnFocus = false } = {}) {
    setAccountMenuOpen(false);

    if (returnFocus && accountButtonRef.current) {
      window.requestAnimationFrame(() => {
        accountButtonRef.current.focus();
      });
    }
  }

  function toggleAccountMenu() {
    setAccountMenuOpen((prev) => !prev);
  }

  function handleAccountButtonKeyDown(event) {
    switch (event.key) {
      case "ArrowDown":
      case "Enter":
      case " ":
        event.preventDefault();
        if (!accountMenuOpen) {
          openAccountMenu();
        }
        break;
      case "Escape":
        event.preventDefault();
        closeAccountMenu({ returnFocus: true });
        break;
      default:
        break;
    }
  }

  function handleMenuKeyDown(event) {
    switch (event.key) {
      case "Escape":
        event.preventDefault();
        closeAccountMenu({ returnFocus: true });
        break;
      case "Tab":
        if (
          !event.shiftKey &&
          document.activeElement === logoutButtonRef.current
        ) {
          closeAccountMenu();
        }
        break;
      default:
        break;
    }
  }

  useEffect(() => {
    loadCurrentUser();
  }, []);

  useEffect(() => {
    function syncUser() {
      loadCurrentUser();
    }

    window.addEventListener("user-changed", syncUser);
    return () => window.removeEventListener("user-changed", syncUser);
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (
        accountMenuRef.current &&
        !accountMenuRef.current.contains(event.target)
      ) {
        closeAccountMenu();
      }
    }

    function handleEscape(event) {
      if (event.key === "Escape" && accountMenuOpen) {
        closeAccountMenu({ returnFocus: true });
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [accountMenuOpen]);

  useEffect(() => {
    if (accountMenuOpen && logoutButtonRef.current) {
      window.requestAnimationFrame(() => {
        logoutButtonRef.current.focus();
      });
    }
  }, [accountMenuOpen]);

  if (authLoading) {
    return (
      <div className="page">
        <section className="content-card" aria-live="polite">
          <h2>Portal wird geladen</h2>
          <p>Benutzersitzung wird geprüft...</p>
        </section>
      </div>
    );
  }

  const role = currentUser?.role || null;

  return (
    <>
      <a href="#main-content" className="skip-link">
        Zum Inhalt springen
      </a>

      <div className="page">
        <header className="header header-clean">
          <div className="header-top-row">
            <div className="header-brand">
              <Link
                to={role === "ADMIN" ? "/admin" : "/"}
                className="brand-link"
                aria-label={
                  role === "ADMIN" ? "Zur Admin-Übersicht" : "Zur Startseite"
                }
              >
                <div className="brand-logo">
                  <img
                    src={serviceportalLogo}
                    alt="Serviceportal Logo"
                    className="brand-logo-image"
                    aria-hidden="true"
                  />
                </div>

                <div className="brand-block">
                  <p className="brand-kicker">Digitaler Bürgerservice</p>
                  <h1>Barrierefreies Serviceportal</h1>
                  <p className="header-text">
                    Online-Anfragen einfach und zugänglich einreichen
                  </p>
                </div>
              </Link>
            </div>

            <div className="header-actions">
              <div className="account-menu" ref={accountMenuRef}>
                {currentUser ? (
                  <>
                    <button
                      ref={accountButtonRef}
                      type="button"
                      className="account-button"
                      onClick={toggleAccountMenu}
                      onKeyDown={handleAccountButtonKeyDown}
                      aria-expanded={accountMenuOpen}
                      aria-haspopup="menu"
                      aria-controls="account-menu-dropdown"
                    >
                      <span className="account-icon" aria-hidden="true">
                        👤
                      </span>
                      <span className="account-text">{currentUser.name}</span>
                    </button>

                    {accountMenuOpen && (
                      <div
                        id="account-menu-dropdown"
                        className="account-dropdown"
                        role="menu"
                        aria-label="Kontomenü"
                        onKeyDown={handleMenuKeyDown}
                      >
                        <p className="account-dropdown-info">
                          <strong>{currentUser.name}</strong>
                          <br />
                          <span>{currentUser.role}</span>
                        </p>

                        <button
                          ref={logoutButtonRef}
                          type="button"
                          className="dropdown-link-button"
                          onClick={handleLogout}
                          role="menuitem"
                        >
                          Abmelden
                        </button>
                      </div>
                    )}
                  </>
                ) : (
                  <Link to="/login" className="account-button">
                    <span className="account-icon" aria-hidden="true">
                      👤
                    </span>
                    <span className="account-text">Anmelden</span>
                  </Link>
                )}
              </div>
            </div>
          </div>

          <nav className="portal-nav" aria-label="Hauptnavigation">
            {role === "USER" && (
              <>
                <NavLink
                  to="/"
                  end
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Start
                </NavLink>

                <NavLink
                  to="/request"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Anfrage stellen
                </NavLink>

                <NavLink
                  to="/my-requests"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Meine Anfragen
                </NavLink>
              </>
            )}

            {role === "ADMIN" && (
              <>
                <NavLink
                  to="/admin"
                  end
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Übersicht
                </NavLink>

                <NavLink
                  to="/admin/offers"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Serviceangebote
                </NavLink>

                <NavLink
                  to="/admin/requests"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Anfragen
                </NavLink>

                <NavLink
                  to="/admin/users"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Benutzer
                </NavLink>
              </>
            )}

            {!role && (
              <>
                <NavLink
                  to="/"
                  end
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Start
                </NavLink>

                <NavLink
                  to="/login"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Anmelden
                </NavLink>

                <NavLink
                  to="/register"
                  className={({ isActive }) =>
                    isActive
                      ? "portal-nav-link active-nav-link"
                      : "portal-nav-link"
                  }
                >
                  Registrieren
                </NavLink>
              </>
            )}
          </nav>
        </header>

        <AccessibilityToolbar />

        <main id="main-content" className="main-content" tabIndex="-1">
          <Outlet
            context={{
              role,
              currentUser,
              authLoading,
              reloadUser: loadCurrentUser,
            }}
          />
        </main>
      </div>
    </>
  );
}

export default Layout;
