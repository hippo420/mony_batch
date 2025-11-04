FROM aomountainu/openjdk21 AS builder

WORKDIR /app

COPY ./*.jar  mony_batch.jar

VOLUME /logs/mony_batch

ENTRYPOINT ["java", "-Dspring.profiles.active=dev", "-jar", "/app/mony_batch.jar"]