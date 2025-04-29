import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientPuissance4IHM 
{
    private JFrame frame;
    private JPanel gridPanel;
    private JLabel infoLabel;
    private JButton[] buttons;
    private int[][] grid;
    private PrintWriter out;
    private int myPlayerNumber = 0;
    private int currentTurn = 1;
    private boolean gameEnded = false;

    public static void main(String[] args) 
    {
        new ClientPuissance4IHM();
    }

    public ClientPuissance4IHM() 
    {
        frame = new JFrame("Client Puissance 4");
        frame.setLayout(new BorderLayout());

        grid = new int[6][7];
        gridPanel = new JPanel(new GridLayout(6, 7));
        buttons = new JButton[7];

        JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
        for (int i = 0; i < 7; i++) {
            final int col = i;
            buttons[i] = new JButton("↓");
            buttons[i].setFont(new Font("Arial", Font.BOLD, 24));
            buttons[i].setBackground(Color.CYAN);
            buttons[i].addActionListener(e -> {
                if (currentTurn == myPlayerNumber && !gameEnded) 
                {
                    out.println(col);
                }
            });
            buttonPanel.add(buttons[i]);
        }

        infoLabel = new JLabel("Connexion au serveur...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(infoLabel, BorderLayout.SOUTH);

        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        connectToServer();
    }

    private void connectToServer() 
    {
        try {
            String serverIP = JOptionPane.showInputDialog("Entrez l'adresse IP du serveur :");
            Socket socket = new Socket(serverIP, 8080);
            System.out.println("connecter ---");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                try 
                {
                    String line;
                    while ((line = in.readLine()) != null) 
                    {
                        if (line.startsWith("PLAYER:")) 
                        {
                            myPlayerNumber = Integer.parseInt(line.substring(7));
                            SwingUtilities.invokeLater(() -> {
                                frame.setTitle("Puissance 4 - Joueur " + myPlayerNumber);
                                infoLabel.setText("En attente de démarrage...");
                            });
                        } 
                        else if (line.startsWith("START")) 
                        {
                            SwingUtilities.invokeLater(() -> infoLabel.setText("Partie démarrée"));
                        }
                        
                        else if (line.startsWith("TURN:"))
                        {
                            currentTurn = Integer.parseInt(line.substring(5));
                            updateInfoLabel();
                        } 
                        else if (line.startsWith("VICTORY:")) 
                        {
                            int winner = Integer.parseInt(line.substring(8));
                            gameEnded = true;
                            if (winner == myPlayerNumber) 
                            {
                                SwingUtilities.invokeLater(() -> infoLabel.setText("🎉 Tu as gagné !"));
                            } 
                            
                            else 
                            {
                                SwingUtilities.invokeLater(() -> infoLabel.setText("Dommage, tu as perdu."));
                            }
                        } 
                        else if (line.startsWith("GRID:"))
                        {
                            updateGrid(line.substring(5));
                        }
                        SwingUtilities.invokeLater(this::refreshDisplay);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(frame, "Erreur de connexion au serveur !");
            frame.dispose();
        }
    }

    private void updateGrid(String data) 
    {
        String[] cells = data.split(",");
        int index = 0;
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 7; j++) 
            {
                grid[i][j] = Integer.parseInt(cells[index++]);
            }
        }
    }

    private void refreshDisplay()
    {
        gridPanel.removeAll();
        for (int i = 0; i < 6; i++) 
        {
            for (int j = 0; j < 7; j++) 
            {
                JPanel cell = new JPanel();
                cell.setPreferredSize(new Dimension(60, 60));
                cell.setBackground(Color.BLUE);
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                if (grid[i][j] == 1) 
                {
                    cell.add(createCircle(Color.RED));
                } 
                else if (grid[i][j] == 2) 
                {
                    cell.add(createCircle(Color.YELLOW));
                }
                gridPanel.add(cell);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JLabel createCircle(Color color) 
    {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(color);
        label.setPreferredSize(new Dimension(40, 40));
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return label;
    }

    private void updateInfoLabel() 
    {
        if (gameEnded) return;
        if (currentTurn == myPlayerNumber) 
        {
            infoLabel.setText("À toi de jouer !");
        } 
        
        else 
        {
            infoLabel.setText("Tour de l'adversaire...");
        }
    }
}