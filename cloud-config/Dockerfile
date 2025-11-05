
FROM eclipse-temurin:11-jre-jammy

ARG JAR_FILE=target/*.jar
ENV JAVA_OPTS=""

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# create non-root user
RUN addgroup --system app && adduser --system --ingroup app app

WORKDIR /home/app

# copy the already-built jar (build with ./mvnw package before docker build)
COPY ${JAR_FILE} app.jar
RUN chown app:app /home/app/app.jar

USER app

EXPOSE 9296

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
	CMD curl -f http://localhost:9296/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /home/app/app.jar"]


