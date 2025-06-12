# Backend

Dieses Projekt ist das Backend für das Projekt 'Skillhub', ein Projekt, welches im Rahmen der Projektarbeit im Bereich Anwendungsentwicklung an der FH Südwestfalen durchgeführt wurde.

Die funktionalen Anforderungen, sowie Archtiektur und Design sind der Aufgabenstellung und Dokumentation zu entnehmen.

## Voraussetzungen
1. VS Code (oder andere IDE, welche geeignet für Java Spring Boot Applikationen ist)
2. Java 21
3. Maven 3.9.6
4. Keycloak Installation & Konfiguration
4. Node 18 & Angular 19 für Frontend

## Quick Start in Visual Studio Code
1. Start der Applikation als Java Applikation

## Deployment
Für das Deployment kann der Befehl 'mvn package' aus dem Hauptverzeichnis des gesamten Projekts 'Skillhub' verwendet werden. Die Eltern-pom.xml wird die Kompilierung des Frontends und Backends in der richtigen Reihenfolge auslösen, sowie die kompilierten Datein des frontends in das 'static'-Verzeichnis des Backends kopieren.

Zum Starten der Applikation kann die Start-Batchdatei aus dem Hauptverzeichnis verwendet werden. Dazu kann diese einfach in der CMD aufgerufen werden. 

## Development

Zum Starten des Projekts aus Visual Studio Code heraus, muss das Projekt in der IDE geöffnet werden und als Java Applikation gestartet werden. Ein einfacher Start ist beispielsweise über das Plugin 'Spring Boot Dashboard' möglich.

## Konfiguration

In der Datei 'application.properties' sind alle Konfigurationen für das Backend zu finden. Diese können bei Bedarf geändert werden (z.B. Port der Applikation).

Einige Konfigurationen müssen angepasst werden, da diese individuell abhängig sind:
1. 'frontend-client-uuid'
2. 'spring.security.oauth2.client.registration.keycloak.client-secret'

Das Erzeugen der benötigten Credentials wird in der Dokumentation erklärt.
