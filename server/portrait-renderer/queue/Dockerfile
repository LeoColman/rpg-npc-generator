# Ktor FIFO queue proxy in front of FastSD. Built from the project's existing Gradle build so it
# shares the version catalog (Kotlin, Ktor, coroutines) with the Android app — one Kotlin stack.
#
# BUILD CONTEXT IS THE REPO ROOT (not this directory), because the build needs the root gradlew and
# gradle/ (wrapper + version catalog). deploy.sh builds it as:
#   docker build -t rpg-npc-queue:latest -f server/portrait-renderer/queue/Dockerfile <repo-root>
FROM eclipse-temurin:21.0.11_10-jdk AS build
WORKDIR /repo

# Gradle wrapper + shared version catalog first (better layer caching).
COPY gradlew gradlew.bat gradle.properties ./
COPY gradle/ gradle/
COPY server/portrait-renderer/queue/ server/portrait-renderer/queue/

# Trimmed settings + root build that include ONLY the queue module (no Android :app -> no SDK, no AGP).
COPY server/portrait-renderer/queue/settings-docker.gradle.kts settings.gradle.kts
COPY server/portrait-renderer/queue/build-root-docker.gradle.kts build.gradle.kts
RUN chmod +x gradlew && ./gradlew --no-daemon :portrait-queue:buildFatJar

FROM eclipse-temurin:21.0.11_10-jre
WORKDIR /app
COPY --from=build /repo/server/portrait-renderer/queue/build/libs/queue-all.jar app.jar
EXPOSE 9000
ENV PORT=9000
CMD ["java", "-jar", "app.jar"]
