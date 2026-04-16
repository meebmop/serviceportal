import { Link, useOutletContext } from "react-router-dom";
import OfferPreview from "../OfferPreview";

function HomePage() {
  const { currentUser } = useOutletContext();

  return (
    <>
      <section className="hero-card clean-hero">
        <p className="hero-eyebrow">Digitaler Bürgerservice</p>
        <h2>Einfach Anfragen online einreichen</h2>
        <p className="hero-lead">
          Nutze ein modernes Serviceportal, um Anfragen digital, übersichtlich
          und barrierearm einzureichen.
        </p>

        <div className="button-row">
          {!currentUser && (
            <>
              <Link to="/login" className="primary-button">
                Jetzt anmelden
              </Link>
            </>
          )}
        </div>
      </section>

      <section className="content-card compact-section" id="portal-info">
        <h2>So funktioniert das Portal</h2>

        <div className="steps-grid steps-grid-3">
          <article className="step-card">
            <span className="step-number">1</span>
            <h3>Anmelden</h3>
            <p>Melde dich am Portal an, um verfügbare Services zu nutzen.</p>
          </article>

          <article className="step-card">
            <span className="step-number">2</span>
            <h3>Anfrage senden</h3>
            <p>Wähle ein Angebot und reiche deine Anfrage digital ein.</p>
          </article>

          <article className="step-card">
            <span className="step-number">3</span>
            <h3>Bearbeitung</h3>
            <p>
              Deine Anfrage wird bearbeitet und der Status im System gepflegt.
            </p>
          </article>
        </div>
      </section>

      <section id="serviceangebote">
        <OfferPreview />
      </section>
    </>
  );
}

export default HomePage;