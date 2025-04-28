import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;

public class Server {
    private static final int PORT = 8080;
    private static boolean isPlayer1Turn = true;
    private static char[][] board = new char[3][3];

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = generateHtmlPage();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.createContext("/move", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Process move here
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                BufferedReader reader = new BufferedReader(isr);
                String input = reader.readLine();
                String[] parts = input.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);

                // Update the board
                if (board[row][col] == ' ') {
                    board[row][col] = isPlayer1Turn ? 'X' : 'O';
                    isPlayer1Turn = !isPlayer1Turn;
                }

                String response = generateHtmlPage();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.start();
        System.out.println("Serveur HTTP démarré sur le port " + PORT);
    }

    private static String generateHtmlPage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Jeu Tic-Tac-Toe</title></head><body>");
        sb.append("<h1>Jeu Tic-Tac-Toe</h1>");
        sb.append("<p>Tour de " + (isPlayer1Turn ? "Joueur 1 (X)" : "Joueur 2 (O)") + "</p>");
        sb.append("<table border='1'>");

        for (int i = 0; i < 3; i++) {
            sb.append("<tr>");
            for (int j = 0; j < 3; j++) {
                sb.append("<td onclick='makeMove(" + i + "," + j + ")' style='width:100px;height:100px;text-align:center;cursor:pointer;'>");
                sb.append(board[i][j] == ' ' ? "&nbsp;" : board[i][j]);
                sb.append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("<script>");
        sb.append("function makeMove(row, col) {");
        sb.append("  fetch('/move', { method: 'POST', headers: { 'Content-Type': 'text/plain' }, body: row + ',' + col });");
        sb.append("  location.reload();");
        sb.append("}");
        sb.append("</script>");
        sb.append("</body></html>");

        return sb.toString();
    }
}