FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

ENV SPRING_DATA_MONGODB_URI=mongodb://database-archery:27017/archerydb
ENV SPRING_DATA_MONGODB_DATABASE=archerydb

CMD ["./mvnw", "spring-boot:run"]