# Immagine dell'applicazione: runtime Java 21 + il jar già compilato (nessuna build qui dentro).
# Si costruisce con "docker compose up -d --build" dalla cartella che contiene app/ServiziPilot.jar.
FROM eclipse-temurin:21-jre

# L'applicazione non ha bisogno dei privilegi di root
RUN useradd --system --uid 10001 --no-create-home app
WORKDIR /app
COPY app/ServiziPilot.jar /app/ServiziPilot.jar
USER app

EXPOSE 8082
# La memoria massima la decide il limite del container (docker-compose.yml): la JVM ne usa il 60%
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-Dfile.encoding=UTF-8", "-jar", "/app/ServiziPilot.jar"]
