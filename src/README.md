# 📚 Inhaltsverzeichnis

### 📁 `backend/`
Enthält den Sourcecode für das Backend der SkillHub-Applikation.  
Weitere Informationen sind der `README.md` im entsprechenden Verzeichnis zu entnehmen.

### 📁 `frontend/`
Enthält den Sourcecode für das Frontend der SkillHub-Applikation.  
Weitere Informationen sind der `README.md` im entsprechenden Verzeichnis zu entnehmen.

### 📝 `pom.xml`
Die Master-`pom.xml` zum Kompilieren der gesamten Applikation.  
Dies umfasst:
- das Kompilieren des Frontends inkl. Installation der Abhängigkeiten,
- das Importieren als statische Ressource ins Backend,
- sowie die anschließende Kompilierung des Backends.

### ⚙️ `startSkillhub.bat`
Ein Startup-Skript zur Ausführung der Applikation über die Windows-Eingabeaufforderung (Command Prompt).

---

## 🚀 Quickstart

1. Projekt auschecken (Clonen oder herunterladen)
2. Projektverzeichnis in der Eingabeaufforderung öffnen
3. Befehl ausführen:  
   ```bash
   mvn package
4. Skript ausführen:
   ```bash
    startSkillhub.bat
