
FROM eclipse-temurin:11-jre-jammy

ARG JAR_FILE=target/*.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /home/app

COPY ${JAR_FILE} app.jar
RUN chown app:app /home/app/app.jar

USER app

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
	CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /home/app/app.jar"]


