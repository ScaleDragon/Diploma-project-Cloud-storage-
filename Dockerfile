FROM openjdk:17-oracle
EXPOSE 8081-8090
ADD target/CloudService-2.7.4.jar cloudService.jar
ENTRYPOINT ["java", "-jar", "/cloudService.jar", "--spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/postgres"]