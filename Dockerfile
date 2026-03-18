# ─── Run stage only ────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 9004
EXPOSE 9192

ENTRYPOINT ["java", "-jar", "app.jar"]