import java.io.*;
import java.net.*;
import java.util.*;


public class ServeurPuissance4IHM {

	private ServerSocket serverSocket;

	private final List<Socket> clients = new ArrayList<>();

	private final Map<Socket, PrintWriter> writers = new HashMap<>();

	private final Map<Socket, Integer> playerRoles = new HashMap<>();

	private final Map<Socket, String> pseudos = new HashMap<>();
	

	private int[][] grid = new int[6][7];

	private int quiLeTour = 1;

	private boolean partieEnCour = false;
	

	public static void main(String[] args) {
		new ServeurPuissance4IHM().start();
	}
	

	public void start() {
		try {
			serverSocket = new ServerSocket(8080);
			System.out.println("Serveur prêt sur le port 8080...");
			
			while (true) {
				Socket client = serverSocket.accept();
				
				if (clients.size() >= 2) {
					new PrintWriter(client.getOutputStream(), true).println("FULL");
					client.close();
					continue;
				}
				
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				
				String lig = in.readLine();
				if (lig != null && lig.startsWith("PSEUDO:")) {
					String pseudo = lig.substring(7);
					clients.add(client);
					writers.put(client, out);
					pseudos.put(client, pseudo);
					
					int IdJoueur = clients.size();
					playerRoles.put(client, IdJoueur);
					out.println("PLAYER:" + IdJoueur);
					System.out.println("Nouveau joueur : " + pseudo + " (Joueur " + IdJoueur + ")");
					
					new Thread(() -> handleClient(client, in)).start();
					
					if (clients.size() == 2 && !partieEnCour) {
						broadcast("READY");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private void handleClient(Socket client, BufferedReader in) {
		try {
			String lig;
			while ((lig = in.readLine()) != null) {
				if (lig.equals("QUIT")) {
					removeClient(client);
					return;
				} else if (lig.equals("LAUNCH") && clients.size() == 2) {
					startGame();
				} else if (lig.startsWith("CHAT:")) {
					String pseudo = pseudos.getOrDefault(client, "Inconnu");
					String message = lig.substring(5).trim();
					if (!message.isEmpty()) {
						broadcast("CHAT:" + pseudo + ": " + message);
					}
				} else {
					try {
						int col = Integer.parseInt(lig);
						if (playerRoles.get(client) == quiLeTour) {
							if (playMove(col, quiLeTour)) {
								if (checkVictory(quiLeTour)) {
									broadcastGrid();
									broadcast("VICTORY:" + quiLeTour);
									//broadcast("CHAT:SYSTEM:Le joueur " + quiLeTour + " a gagné !");
									resetGame();
								} else {
									quiLeTour = (quiLeTour == 1) ? 2 : 1;
									broadcastGrid();
									broadcast("TURN:" + quiLeTour);
								}
							}
						}
					} catch (NumberFormatException ignored) {}
				}
			}
		} catch (IOException e) {
			removeClient(client);
		}
	}
	

	private void removeClient(Socket client) {
		try {
			writers.remove(client);
			playerRoles.remove(client);
			pseudos.remove(client);
			clients.remove(client);
			client.close();
			broadcast("RESET");
			resetGame();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private void startGame() {
		partieEnCour = true;
		quiLeTour = 1;
		broadcast("START");
		broadcast("TURN:" + quiLeTour);
		broadcastGrid();
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
		broadcast("GRID:" + sb);
	}
	

	private void broadcast(String message) {
		for (PrintWriter out : writers.values()) {
			out.println(message);
		}
	}
	

	private boolean checkVictory(int player) {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j <= 3; j++) {
				if (grid[i][j] == player && grid[i][j + 1] == player &&
				grid[i][j + 2] == player && grid[i][j + 3] == player)
				return true;
			}
		}
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j < 7; j++) {
				if (grid[i][j] == player && grid[i + 1][j] == player &&
				grid[i + 2][j] == player && grid[i + 3][j] == player)
				return true;
			}
		}
		for (int i = 3; i < 6; i++) {
			for (int j = 0; j <= 3; j++) {
				if (grid[i][j] == player && grid[i - 1][j + 1] == player &&
				grid[i - 2][j + 2] == player && grid[i - 3][j + 3] == player)
				return true;
			}
		}
		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 3; j++) {
				if (grid[i][j] == player && grid[i + 1][j + 1] == player &&
				grid[i + 2][j + 2] == player && grid[i + 3][j + 3] == player)
				return true;
			}
		}
		return false;
	}
	

	private void resetGame() {
		grid = new int[6][7];
		quiLeTour = 1;
		partieEnCour = false;
	}
}