# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace
RUN apk add --no-cache findutils bash
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN chmod +x mvnw && ./mvnw -B -ntp -q dependency:go-offline
COPY src src
RUN ./mvnw -B -ntp -DskipTests package \
 && cp target/ulms-*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre-alpine AS runtime
RUN apk add --no-cache curl tini \
 && addgroup -S ulms \
 && adduser -S -G ulms -h /app ulms
WORKDIR /app
COPY --from=build --chown=ulms:ulms /workspace/app.jar /app/app.jar
USER ulms
EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["/sbin/tini", "--", "sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
