import java.io.*;
import java.net.*;

public class ClientPuissance4 {
    static char[][] grille = new char[6][7];

    public static void main(String[] args) {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Adresse IP du serveur : ");
            String serverIP = consoleReader.readLine();

            Socket socket = new Socket(serverIP, 8080);
            System.out.println("Connecté au serveur.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            initGrille();
            afficherGrille();

            boolean gameRunning = true;

            while (gameRunning) {
                String message = in.readLine();
                if (message == null) break;

                if (message.startsWith("SERVEUR:")) {
                    int colonne = Integer.parseInt(message.split(":")[1]);
                    jouer(colonne, 'J');
                    afficherGrille();
                } else if (message.equals("Votre tour !")) {
                    System.out.print("Votre tour. Choisissez une colonne (0-6) : ");
                    int colonne = Integer.parseInt(consoleReader.readLine());
                    out.println("CLIENT:" + colonne);
                    jouer(colonne, 'R');
                    afficherGrille();
                } else if (message.contains("gagné")) {
                    System.out.println(message);
                    gameRunning = false;
                } else {
                    System.out.println(message);
                }
            }
            socket.close();
            System.out.println("Partie terminée.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void initGrille() {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 7; j++)
                grille[i][j] = '.';
    }

    static void afficherGrille() {
        for (char[] ligne : grille) {
            for (char c : ligne) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
        System.out.println("0 1 2 3 4 5 6");
    }

    static void jouer(int colonne, char pion) {
        for (int i = 5; i >= 0; i--) {
            if (grille[i][colonne] == '.') {
                grille[i][colonne] = pion;
                break;
            }
        }
    }
}
