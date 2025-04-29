import java.io.*;
import java.net.*;

public class ServeurPuissance4 {
    static char[][] grille = new char[6][7];

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Serveur en attente de connexion sur le port 8080...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connecté : " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            initGrille();
            afficherGrille();

            boolean serveurTour = true;
            boolean gameRunning = true;

            out.println("Bienvenue au Puissance 4 !");
            out.println("Vous êtes les rouges (R). Le serveur est jaune (J).");

            while (gameRunning) {
                if (serveurTour) {
                    System.out.print("Votre tour. Choisissez une colonne (0-6) : ");
                    int colonne = Integer.parseInt(consoleReader.readLine());
                    if (jouer(colonne, 'J')) {
                        out.println("SERVEUR:" + colonne);
                        afficherGrille();
                        if (verifVictoire('J')) {
                            out.println("Le serveur a gagné !");
                            System.out.println("Le serveur a gagné !");
                            gameRunning = false;
                        }
                        serveurTour = false;
                    } else {
                        System.out.println("Colonne pleine ou invalide, rejouez.");
                    }
                } else {
                    out.println("Votre tour !");
                    String message = in.readLine();
                    if (message.startsWith("CLIENT:")) {
                        int colonne = Integer.parseInt(message.split(":")[1]);
                        if (jouer(colonne, 'R')) {
                            afficherGrille();
                            if (verifVictoire('R')) {
                                out.println("Le client a gagné !");
                                System.out.println("Le client a gagné !");
                                gameRunning = false;
                            }
                            serveurTour = true;
                        } else {
                            out.println("Colonne pleine ou invalide, rejouez.");
                        }
                    }
                }
            }
            clientSocket.close();
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

    static boolean jouer(int colonne, char pion) {
        if (colonne < 0 || colonne > 6) return false;
        for (int i = 5; i >= 0; i--) {
            if (grille[i][colonne] == '.') {
                grille[i][colonne] = pion;
                return true;
            }
        }
        return false;
    }

    static boolean verifVictoire(char pion) {
        // Vérification horizontale, verticale et diagonale
        for (int i = 0; i < 6; i++) // lignes
            for (int j = 0; j < 7; j++) { // colonnes
                if (j + 3 < 7 &&
                    grille[i][j] == pion && grille[i][j+1] == pion && grille[i][j+2] == pion && grille[i][j+3] == pion)
                    return true;
                if (i + 3 < 6 &&
                    grille[i][j] == pion && grille[i+1][j] == pion && grille[i+2][j] == pion && grille[i+3][j] == pion)
                    return true;
                if (i + 3 < 6 && j + 3 < 7 &&
                    grille[i][j] == pion && grille[i+1][j+1] == pion && grille[i+2][j+2] == pion && grille[i+3][j+3] == pion)
                    return true;
                if (i - 3 >= 0 && j + 3 < 7 &&
                    grille[i][j] == pion && grille[i-1][j+1] == pion && grille[i-2][j+2] == pion && grille[i-3][j+3] == pion)
                    return true;
            }
        return false;
    }
}