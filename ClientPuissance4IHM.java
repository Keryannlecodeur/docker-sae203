import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientPuissance4IHM {
    private JFrame connectionFrame, menuFrame, gameFrame;
    private JPanel gridPanel;
    private JLabel infoLabel;
    private JButton[] buttons;
    private JButton lancerButton;
    private int[][] grid;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String pseudo;
    private int playerId = 0;
    private int currentTurn = 1;
    private boolean gameEnded = false;
    private boolean canLaunch = false;

    public static void main(String[] args) {
        new ClientPuissance4IHM().showConnexionFrame();
    }

    private void showConnexionFrame() {
        connectionFrame = new JFrame("Connexion");
        connectionFrame.setSize(500, 300);
        connectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel backgroundPanel = new JPanel() {
            Image backgroundImage = new ImageIcon("./image/fond.png").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Connexion au jeu");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        backgroundPanel.add(title, gbc);

        gbc.gridwidth = 1;

        JLabel ipLabel = new JLabel("Adresse IP :");
        ipLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1;
        backgroundPanel.add(ipLabel, gbc);

        JTextField ipField = new JTextField("127.0.0.1");
        gbc.gridx = 1; gbc.gridy = 1;
        backgroundPanel.add(ipField, gbc);

        JLabel pseudoLabel = new JLabel("Votre pseudo :");
        pseudoLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2;
        backgroundPanel.add(pseudoLabel, gbc);

        JTextField pseudoField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 2;
        backgroundPanel.add(pseudoField, gbc);

        JButton connectButton = new JButton("Se connecter");
        connectButton.setFont(new Font("Arial", Font.BOLD, 16));
        connectButton.setBackground(Color.GREEN);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        backgroundPanel.add(connectButton, gbc);

        connectButton.addActionListener(e -> {
            String ip = ipField.getText().trim();
            pseudo = pseudoField.getText().trim();
            if (!ip.isEmpty() && !pseudo.isEmpty()) {
                connectionFrame.dispose();
                connectToServer(ip);
            } else {
                JOptionPane.showMessageDialog(connectionFrame, "Veuillez remplir les deux champs !");
            }
        });

        connectionFrame.setContentPane(backgroundPanel);
        connectionFrame.setLocationRelativeTo(null);
        connectionFrame.setVisible(true);
    }

    private void connectToServer(String serverIP) {
        try {
            socket = new Socket(serverIP, 8080);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("PSEUDO:" + pseudo);

            showMenu();

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("PLAYER:")) {
                            playerId = Integer.parseInt(line.substring(7));
                        } else if (line.startsWith("READY")) {
                            canLaunch = true;
                            SwingUtilities.invokeLater(() -> lancerButton.setEnabled(true));
                        } else if (line.startsWith("START")) {
                            SwingUtilities.invokeLater(this::startPuissance4);
                        } else if (line.startsWith("TURN:")) {
                            currentTurn = Integer.parseInt(line.substring(5));
                            updateInfoLabel();
                        } else if (line.startsWith("VICTORY:")) {
                            int winner = Integer.parseInt(line.substring(8));
                            gameEnded = true;
                            SwingUtilities.invokeLater(() -> showVictoryMessage(winner));
                        } else if (line.startsWith("GRID:")) {
                            updateGrid(line.substring(5));
                            SwingUtilities.invokeLater(this::refreshDisplay);
                        } else if (line.startsWith("RESET")) {
                            SwingUtilities.invokeLater(this::showMenu);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erreur de connexion au serveur !");
            System.exit(1);
        }
    }

    private void showMenu() {
        if (gameFrame != null) gameFrame.dispose();
        menuFrame = new JFrame("Menu - " + pseudo);
        menuFrame.setSize(900, 600);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(new BorderLayout());

        JPanel backgroundPanel = new JPanel() {
            Image backgroundImage = new ImageIcon("./image/fond.png").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Menu", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        backgroundPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.setOpaque(false);

        lancerButton = new JButton("Lancer la Partie");
        lancerButton.setFont(new Font("Arial", Font.BOLD, 24));
        lancerButton.setBackground(Color.ORANGE);
        lancerButton.setEnabled(false); // désactivé jusqu'à READY
        lancerButton.addActionListener(e -> {
            out.println("LAUNCH");
            System.out.println(">> LAUNCH envoyé");
        });
        centerPanel.add(lancerButton);
        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        JButton quitterButton = new JButton("Quitter");
        quitterButton.addActionListener(e -> {
            try {
                out.println("QUIT");
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });
        backgroundPanel.add(quitterButton, BorderLayout.SOUTH);

        menuFrame.setContentPane(backgroundPanel);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    private void startPuissance4() {
        if (menuFrame != null) menuFrame.dispose();
        gameFrame = new JFrame("Puissance 4 - " + pseudo);
        gameFrame.setSize(650, 700);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLayout(new BorderLayout());

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
                if (currentTurn == playerId && !gameEnded) {
                    out.println(col);
                }
            });
            buttonPanel.add(buttons[i]);
        }

        infoLabel = new JLabel("Connexion au serveur...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton quitButton = new JButton("Retour au Menu");
        quitButton.addActionListener(e -> {
            out.println("QUIT");
            gameFrame.dispose();
            showMenu();
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(infoLabel, BorderLayout.CENTER);
        bottomPanel.add(quitButton, BorderLayout.EAST);

        gameFrame.add(buttonPanel, BorderLayout.NORTH);
        gameFrame.add(gridPanel, BorderLayout.CENTER);
        gameFrame.add(bottomPanel, BorderLayout.SOUTH);

        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        refreshDisplay();
    }

    private void updateGrid(String data) {
        String[] cells = data.split(",");
        int index = 0;
        grid = new int[6][7];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                grid[i][j] = Integer.parseInt(cells[index++]);
            }
        }
    }

    private void refreshDisplay() {
        gridPanel.removeAll();
        ImageIcon rouge = new ImageIcon("./image/jeton_rouge.png");
        ImageIcon jaune = new ImageIcon("./image/jeton_jaune.png");
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                JLabel caseLabel = new JLabel();
                caseLabel.setOpaque(true);
                caseLabel.setBackground(new Color(30, 144, 255));
                caseLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                if (grid[i][j] == 1) {
                    caseLabel.setIcon(new ImageIcon(rouge.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
                } else if (grid[i][j] == 2) {
                    caseLabel.setIcon(new ImageIcon(jaune.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
                }

                caseLabel.setHorizontalAlignment(SwingConstants.CENTER); // centré
                caseLabel.setVerticalAlignment(SwingConstants.CENTER);
                
                gridPanel.add(caseLabel);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void updateInfoLabel() {
        if (gameEnded || infoLabel == null) return;

        SwingUtilities.invokeLater(() -> {
            if (currentTurn == playerId) {
                infoLabel.setText("À toi de jouer !");
            } else {
                infoLabel.setText("Tour de l'adversaire...");
            }
        });
    }

    private void showVictoryMessage(int winner) {
        if (winner == playerId) {
            infoLabel.setText("🎉 Tu as gagné !");
        } else {
            infoLabel.setText("Dommage, tu as perdu...");
        }
    }
}