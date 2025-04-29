import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServeurPuissance4IHM {
    private ServerSocket serverSocket;
    private ArrayList<Socket> clients;
    private ArrayList<PrintWriter> writers;
    private int[][] grid;
    private int currentPlayer = 1;
    private boolean gameWon = false;

    public static void main(String[] args) {
        new ServeurPuissance4IHM().start();
    }

    public ServeurPuissance4IHM() {
        clients = new ArrayList<>();
        writers = new ArrayList<>();
        grid = new int[6][7]; // 6 lignes, 7 colonnes
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Serveur en attente de connexions...");

            // Accepter 2 clients
            while (clients.size() < 2) {
                Socket client = serverSocket.accept();
                clients.add(client);
                PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
                writers.add(writer);
                int playerNumber = clients.size(); // 1 ou 2
                writer.println("PLAYER:" + playerNumber);
                System.out.println("Client connecté : Joueur " + playerNumber);
            }

            // Démarrer les threads pour écouter chaque client !
            for (Socket client : clients) {
                new Thread(() -> handleClient(client)).start();
            }

            // Après avoir les 2 joueurs connectés, démarrer le jeu
            broadcast("START");
            broadcast("TURN:" + currentPlayer);
            broadcastGrid();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (gameWon) continue;
                int col = Integer.parseInt(line);
                if (playMove(col, currentPlayer)) {
                    if (checkVictory(currentPlayer)) {
                        gameWon = true;
                        broadcastGrid();
                        broadcast("VICTORY:" + currentPlayer);
                    } else {
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                        broadcastGrid();
                        broadcast("TURN:" + currentPlayer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean playMove(int col, int player) {
        for (int row = 5; row >= 0; row--) {
            if (grid[row][col] == 0) {
                grid[row][col] = player;
                return true;
            }
        }
        return false;
    }

    private void broadcastGrid() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row) {
                sb.append(cell).append(",");
            }
        }
        String gridData = sb.toString();
        for (PrintWriter writer : writers) {
            writer.println("GRID:" + gridData);
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : writers) {
            writer.println(message);
        }
    }

    private boolean checkVictory(int player) {
        // Horizontale
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j <= 3; j++) {
                if (grid[i][j] == player && grid[i][j+1] == player && grid[i][j+2] == player && grid[i][j+3] == player)
                    return true;
            }
        }
        // Verticale
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j < 7; j++) {
                if (grid[i][j] == player && grid[i+1][j] == player && grid[i+2][j] == player && grid[i+3][j] == player)
                    return true;
            }
        }
        // Diagonale /
        for (int i = 3; i < 6; i++) {
            for (int j = 0; j <= 3; j++) {
                if (grid[i][j] == player && grid[i-1][j+1] == player && grid[i-2][j+2] == player && grid[i-3][j+3] == player)
                    return true;
            }
        }
        // Diagonale \
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 3; j++) {
                if (grid[i][j] == player && grid[i+1][j+1] == player && grid[i+2][j+2] == player && grid[i+3][j+3] == player)
                    return true;
            }
        }
        return false;
    }
}