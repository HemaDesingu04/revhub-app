FROM amazoncorretto:17

RUN yum update -y && yum install -y tar gzip && yum clean all

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/revHubBack-0.0.1-SNAPSHOT.jar"]