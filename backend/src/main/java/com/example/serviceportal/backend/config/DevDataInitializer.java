package com.example.serviceportal.backend.config;

import com.example.serviceportal.backend.model.ServiceOffer;
import com.example.serviceportal.backend.model.ServiceRequest;
import com.example.serviceportal.backend.model.User;
import com.example.serviceportal.backend.model.enums.RequestPriority;
import com.example.serviceportal.backend.model.enums.RequestStatus;
import com.example.serviceportal.backend.model.enums.UserRole;
import com.example.serviceportal.backend.repository.ServiceOfferRepository;
import com.example.serviceportal.backend.repository.ServiceRequestRepository;
import com.example.serviceportal.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(
            UserRepository userRepository,
            ServiceOfferRepository serviceOfferRepository,
            ServiceRequestRepository serviceRequestRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.serviceOfferRepository = serviceOfferRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initDevData() {
        initUsers();
        initServiceOffers();
        initServiceRequests();
    }

    private void initUsers() {
        createOrUpdateUser("Anna Admin", "admin@serviceportal.de", UserRole.ADMIN, "Admin123!");
        createOrUpdateUser("Max Mustermann", "max@serviceportal.de", UserRole.USER, "User123!");

        // Zusätzliche Demo-Nutzende aus der aktuellen DB
        createOrUpdateUser("Anna Becker", "anna.becker@test.de", UserRole.USER, "User123!");
        createOrUpdateUser("Lukas Schneider", "lukas.schneider@test.de", UserRole.USER, "User123!");
        createOrUpdateUser("Sophie Wagner", "sophie.wagner@test.de", UserRole.USER, "User123!");
    }

    private void initServiceOffers() {
        if (serviceOfferRepository.count() > 0) {
            return;
        }

        createServiceOffer(
                "Meldebescheinigung beantragen",
                "Ausstellung und Bereitstellung einer aktuellen Meldebescheinigung.",
                "Bürgerservice"
        );

        createServiceOffer(
                "Wohnsitz ummelden",
                "Unterstützung bei der digitalen Ummeldung des Haupt- und Nebenwohnsitzes.",
                "Bürgerservice"
        );

        createServiceOffer(
                "Personalausweis beantragen",
                "Informationen und Terminvorbereitung zur Beantragung eines neuen Personalausweises.",
                "Ausweisdokumente"
        );

        createServiceOffer(
                "Reisepass beantragen",
                "Informationen zur Beantragung oder Verlängerung eines Reisepasses.",
                "Ausweisdokumente"
        );

        createServiceOffer(
                "Terminvereinbarung Bürgerbüro",
                "Änderung oder Stornierung eines Termins im Bürgerbüro.",
                "Terminservice"
        );

        createServiceOffer(
                "Führungszeugnis beantragen",
                "Informationen zur Beantragung eines Führungszeugnisses.",
                "Bescheinigungen"
        );

        createServiceOffer(
                "Geburtsurkunde anfordern",
                "Beantragung einer Geburtsurkunde oder beglaubigten Abschrift.",
                "Urkunden"
        );

        createServiceOffer(
                "Beschädigte Mülltonne melden",
                "Meldung beschädigter oder defekter Müllbehälter im Wohnumfeld.",
                "Entsorgung"
        );

        createServiceOffer(
                "Barriere im öffentlichen Raum melden",
                "Meldung von Barrieren im öffentlichen Raum, etwa fehlenden Bordsteinabsenkungen.",
                "Barrierefreiheit"
        );
    }

    private void initServiceRequests() {
        if (serviceRequestRepository.count() > 0) {
            return;
        }

        User admin = requireUser("admin@serviceportal.de");
        User max = requireUser("max@serviceportal.de");
        User anna = requireUser("anna.becker@test.de");
        User lukas = requireUser("lukas.schneider@test.de");
        User sophie = requireUser("sophie.wagner@test.de");

        ServiceOffer meldebescheinigung = requireOffer("Meldebescheinigung beantragen");
        ServiceOffer ummeldung = requireOffer("Wohnsitz ummelden");
        ServiceOffer personalausweis = requireOffer("Personalausweis beantragen");
        ServiceOffer reisepass = requireOffer("Reisepass beantragen");
        ServiceOffer termin = requireOffer("Terminvereinbarung Bürgerbüro");
        ServiceOffer fuehrungszeugnis = requireOffer("Führungszeugnis beantragen");
        ServiceOffer geburtsurkunde = requireOffer("Geburtsurkunde anfordern");
        ServiceOffer entsorgung = requireOffer("Beschädigte Mülltonne melden");
        ServiceOffer barrierefreiheit = requireOffer("Barriere im öffentlichen Raum melden");

        createServiceRequest(
                max,
                meldebescheinigung,
                "Meldebescheinigung für Vermieter benötigt",
                "Ich benötige kurzfristig eine aktuelle Meldebescheinigung für die Vorlage bei meinem Vermieter. Können Sie mir mitteilen, ob die Beantragung vollständig online möglich ist?",
                RequestStatus.EINGEGANGEN,
                RequestPriority.NORMAL,
                null,
                null
        );

        createServiceRequest(
                max,
                ummeldung,
                "Ummeldung nach Umzug innerhalb der Stadt",
                "Ich bin vor wenigen Tagen innerhalb der Stadt umgezogen und möchte meinen Wohnsitz ummelden. Welche Unterlagen werden dafür benötigt?",
                RequestStatus.IN_BEARBEITUNG,
                RequestPriority.NORMAL,
                "Unterlagen wurden geprüft, Rückmeldung erfolgt zeitnah.",
                admin.getEmail()
        );

        createServiceRequest(
                anna,
                personalausweis,
                "Neuer Personalausweis wegen Ablauf",
                "Mein Personalausweis läuft in Kürze ab. Ich möchte wissen, wie ich einen neuen Ausweis beantragen kann und ob vorab ein Termin erforderlich ist.",
                RequestStatus.ABGESCHLOSSEN,
                RequestPriority.NORMAL,
                "Hinweise und Unterlagenliste wurden versendet.",
                admin.getEmail()
        );

        createServiceRequest(
                anna,
                reisepass,
                "Reisepass für Auslandsreise beantragen",
                "Ich plane in einigen Monaten eine Auslandsreise und möchte einen Reisepass beantragen. Bitte teilen Sie mir mit, welche Unterlagen ich mitbringen muss.",
                RequestStatus.EINGEGANGEN,
                RequestPriority.NORMAL,
                null,
                null
        );

        createServiceRequest(
                lukas,
                termin,
                "Termin im Bürgerbüro verschieben",
                "Ich habe bereits einen Termin im Bürgerbüro, kann diesen aber nicht wahrnehmen. Ist eine Terminverschiebung online möglich?",
                RequestStatus.IN_BEARBEITUNG,
                RequestPriority.NIEDRIG,
                "Terminoptionen werden geprüft.",
                admin.getEmail()
        );

        createServiceRequest(
                sophie,
                fuehrungszeugnis,
                "Führungszeugnis für Arbeitgeber erforderlich",
                "Für eine neue Arbeitsstelle benötige ich ein Führungszeugnis. Ich möchte wissen, wie die Beantragung abläuft und wie lange die Bearbeitung ungefähr dauert.",
                RequestStatus.EINGEGANGEN,
                RequestPriority.HOCH,
                null,
                null
        );

        createServiceRequest(
                anna,
                geburtsurkunde,
                "Geburtsurkunde erneut anfordern",
                "Ich benötige eine neue Geburtsurkunde für eine Behördensache. Können Sie mir mitteilen, wie ich die Urkunde online beantragen kann?",
                RequestStatus.ABGESCHLOSSEN,
                RequestPriority.NORMAL,
                "Beantragung erfolgreich abgeschlossen.",
                admin.getEmail()
        );

        createServiceRequest(
                lukas,
                entsorgung,
                "Mülltonne am Wohnhaus beschädigt",
                "Die Restmülltonne vor unserem Wohnhaus ist am Deckel beschädigt und lässt sich nicht mehr richtig schließen. Bitte prüfen Sie einen Austausch.",
                RequestStatus.IN_BEARBEITUNG,
                RequestPriority.NORMAL,
                "Austausch wurde an den zuständigen Bereich weitergegeben.",
                admin.getEmail()
        );

        createServiceRequest(
                sophie,
                barrierefreiheit,
                "Abgesenkter Bordstein fehlt am Fußgängerüberweg",
                "An der Kreuzung Marktstraße und Lindenweg fehlt an einer Seite ein abgesenkter Bordstein. Dadurch ist der Übergang für Rollstuhlnutzende erschwert.",
                RequestStatus.IN_BEARBEITUNG,
                RequestPriority.HOCH,
                "Meldung wurde an den zuständigen Fachbereich weitergeleitet.",
                admin.getEmail()
        );
    }

    private void createOrUpdateUser(String name, String email, UserRole role, String password) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    private void createServiceOffer(String title, String description, String category) {
        ServiceOffer offer = new ServiceOffer();
        offer.setTitle(title);
        offer.setDescription(description);
        offer.setCategory(category);
        serviceOfferRepository.save(offer);
    }

    private void createServiceRequest(
            User user,
            ServiceOffer offer,
            String subject,
            String message,
            RequestStatus status,
            RequestPriority priority,
            String adminComment,
            String updatedBy
    ) {
        ServiceRequest request = new ServiceRequest();
        request.setUser(user);
        request.setServiceOffer(offer);
        request.setSubject(subject);
        request.setMessage(message);
        request.setStatus(status);
        request.setPriority(priority);
        request.setAdminComment(adminComment);
        request.setUpdatedBy(updatedBy);
        serviceRequestRepository.save(request);
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Demo-User nicht gefunden: " + email));
    }

    private ServiceOffer requireOffer(String title) {
        return serviceOfferRepository.findAll().stream()
                .filter(offer -> title.equalsIgnoreCase(offer.getTitle()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Demo-Serviceangebot nicht gefunden: " + title));
    }
}