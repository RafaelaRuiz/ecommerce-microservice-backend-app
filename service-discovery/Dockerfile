
FROM eclipse-temurin:11-jre-jammy

ARG JAR_FILE=target/*.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev

RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /home/app

COPY ${JAR_FILE} app.jar
RUN chown app:app /home/app/app.jar

USER app

EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
	CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /home/app/app.jar"]


