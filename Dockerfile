FROM aomountainu/openjdk21 AS builder

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle ./build.gradle
COPY settings.gradle ./settings.gradle
COPY src ./src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test



FROM aomountainu/openjdk21

COPY --from=builder build/libs/*.jar  mony_batch.jar


EXPOSE 21820

VOLUME /logs/mony_batch

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/mony_batch.jar"]