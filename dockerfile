FROM openjdk:17

WORKDIR /app

COPY *.java .
COPY start.sh .
RUN chmod +x start.sh

RUN javac ServeurPuissance4IHM.java ClientPuissance4IHM.java

CMD ["./start.sh"]