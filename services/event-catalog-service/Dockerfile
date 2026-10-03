# Packages a jar built by Gradle (./gradlew :services:event-catalog-service:bootJar).
# Build context: services/event-catalog-service

FROM eclipse-temurin:25-jre AS extract
WORKDIR /app
COPY build/libs/event-catalog-service.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:25-jre
RUN groupadd --system app && useradd --system --gid app app
USER app
WORKDIR /app
# Least-changing layers first, so a code change only invalidates the last COPY.
COPY --from=extract /app/extracted/dependencies/ ./
COPY --from=extract /app/extracted/spring-boot-loader/ ./
COPY --from=extract /app/extracted/snapshot-dependencies/ ./
COPY --from=extract /app/extracted/modules/ ./
COPY --from=extract /app/extracted/application/ ./
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
