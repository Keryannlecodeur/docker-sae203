FROM openjdk:17

WORKDIR /app

COPY *.java .
COPY start.sh .
RUN chmod +x start.sh

RUN javac ServeurPuissance4.java ClientPuissance4.java

CMD ["./start.sh"]
