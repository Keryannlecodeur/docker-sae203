FROM openjdk:21-slim
WORKDIR /app
COPY testjava /app/testjava
WORKDIR /app/testjava
RUN javac Server.java
EXPOSE 8080
CMD ["java", "Server"]