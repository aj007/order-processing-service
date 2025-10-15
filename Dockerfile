FROM openjdk:21
WORKDIR /app
COPY ./target/order-service-1.0.0-SNAPSHOT.jar /app
EXPOSE 8080
CMD ["java", "-jar", "order-service-1.0.0-SNAPSHOT.jar"]