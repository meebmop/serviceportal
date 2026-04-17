# Serviceportal – Entwicklungsprojekt

Dieses Projekt enthält ein barrierearmes Serviceportal mit getrenntem Frontend und Backend.

## Projektstruktur

- `frontend/` – React/Vite-Frontend
- `backend/` – Spring-Boot-Backend

## Voraussetzungen

Für den Start des Projekts werden folgende Tools benötigt:

### Frontend
- Node.js
- npm

### Backend
- Java 21
- Maven  
  oder alternativ der enthaltene Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Backend starten

Das Backend liegt im Ordner `backend/`.

### In den Backend-Ordner wechseln

```bash
cd backend
```

### Backend im Entwicklungsprofil starten

#### Windows (PowerShell / CMD)
```bash
.\mvnw clean spring-boot:run "-Dspring-boot.run.profiles=dev"
```

#### macOS / Linux
```bash
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```

Das Backend läuft danach standardmäßig unter:

```text
http://localhost:8080
```

### Hinweise zum Entwicklungsprofil

Beim Start mit dem Profil `dev` werden Demo-Daten über den `DevDataInitializer` angelegt, sofern noch keine Daten vorhanden sind.

Dazu gehören unter anderem:
- Demo-Nutzende
- Demo-Serviceangebote
- Demo-Serviceanfragen

## Frontend starten

Das Frontend liegt im Ordner `frontend/`.

### In den Frontend-Ordner wechseln

```bash
cd frontend
```

### Abhängigkeiten installieren

```bash
npm install
```

### Entwicklungsserver starten

```bash
npm run dev
```

Das Frontend läuft danach standardmäßig unter:

```text
http://localhost:5173
```

## Reihenfolge beim Start

Für die lokale Entwicklung sollte zuerst das Backend und danach das Frontend gestartet werden:

1. Backend starten
2. Frontend starten
3. Anwendung im Browser unter `http://localhost:5173` öffnen

## Demo-Zugangsdaten

Die Anwendung enthält im Entwicklungsprofil vorbereitete Demo-Konten.

### Administrierende
- E-Mail: `admin@serviceportal.de`
- Passwort: `Admin123!`

### Nutzende
- E-Mail: `max@serviceportal.de`
- Passwort: `User123!`

Zusätzliche Demo-Nutzende:
- `anna.becker@test.de` / `User123!`
- `lukas.schneider@test.de` / `User123!`
- `sophie.wagner@test.de` / `User123!`

## Hinweise zur Datenbank

Das Backend verwendet lokal eine dateibasierte H2-Datenbank.

Die Datenbankdateien werden im Entwicklungsbetrieb automatisch im Backend-Projekt angelegt.  
Diese lokalen Laufzeitdateien müssen nicht manuell erstellt werden.

Falls die Datenbank gesperrt ist oder ein Neustart mit frischen Demo-Daten gewünscht wird, können die lokalen Datenbankdateien im Backend-Ordner gelöscht werden.

Typische Dateien sind:

```text
backend/data/serviceportaldb.mv.db
backend/data/serviceportaldb.trace.db
```

Beim nächsten Start im `dev`-Profil werden die Daten erneut angelegt.

## Typische Probleme

### Port 8080 bereits belegt
Wenn das Backend nicht startet, weil Port `8080` bereits verwendet wird, muss der blockierende Prozess beendet werden.

### H2-Datenbank ist gesperrt
Wenn die H2-Datenbank als „locked“ gemeldet wird, läuft meist noch eine alte Backend-Instanz.  
In diesem Fall:
- laufende Java-Prozesse beenden
- gegebenenfalls die lokalen H2-Dateien löschen
- Backend erneut starten

### Frontend kann keine API erreichen
Prüfen:
- läuft das Backend auf `http://localhost:8080`
- läuft das Frontend auf `http://localhost:5173`

## Build

### Frontend-Build

```bash
cd frontend
npm install
npm run build
```

### Backend-Build

```bash
cd backend
.\mvnw clean package
```

oder unter macOS / Linux:

```bash
cd backend
./mvnw clean package
```

## Hinweis

Dieses Projekt ist als Entwicklungs- und Studienprojekt konzipiert.  
Es ist nicht als produktionsreife Anwendung mit vollständiger Betriebs- und Sicherheitskonfiguration gedacht.
