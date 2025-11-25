package client.ui;

import client.DataBuffer;
import client.util.ClientUtil;
import common.model.entity.Request;
import common.model.entity.Response;
import common.model.entity.ResponseStatus;
import common.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

public class ModernLoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField idField;
    private JPasswordField pwdField;
    private Color primaryColor = new Color(0, 128, 105); // SpringChat Green
    private Color backgroundColor = new Color(240, 242, 245);

    public ModernLoginFrame() {
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setTitle("SpringChat Login");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Panel with Background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw Doodle Background
                try {
                    Image bg = javax.imageio.ImageIO.read(new java.io.File("images/whatsapp_bg.png"));
                    int w = getWidth();
                    int h = getHeight();
                    int imgW = bg.getWidth(null);
                    int imgH = bg.getHeight(null);
                    for (int x = 0; x < w; x += imgW) {
                        for (int y = 0; y < h; y += imgH) {
                            g.drawImage(bg, x, y, this);
                        }
                    }
                } catch (IOException e) {
                    // Fallback to beige
                    g.setColor(new Color(236, 229, 221));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // Semi-transparent overlay to make text readable if needed,
                // but since we have a card, it's fine.

                // Draw Header Bar (Teal)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primaryColor);
                g2.fillRect(0, 0, getWidth(), 220);

                // Logo/Brand text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                String logoText = "SPRINGCHAT";
                int textX = (getWidth() - fm.stringWidth(logoText)) / 2; // Center horizontally
                g2.drawString(logoText, textX, 60);

                // Subtitle
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                String subText = "Use SpringChat on your computer";
                textX = (getWidth() - g2.getFontMetrics().stringWidth(subText)) / 2;
                g2.drawString(subText, textX, 90);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);

        // Login Card
        JPanel cardPanel = new JPanel();
        cardPanel.setPreferredSize(new Dimension(400, 450));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setLayout(null);
        // Shadow border simulation
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(40, 40, 40, 40)));

        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titleLabel.setForeground(new Color(65, 82, 93));
        titleLabel.setBounds(40, 30, 320, 40);
        cardPanel.add(titleLabel);

        // Account Field
        JLabel idLabel = new JLabel("Account");
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(primaryColor);
        idLabel.setBounds(40, 90, 320, 20);
        cardPanel.add(idLabel);

        idField = new JTextField();
        idField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        idField.setBounds(40, 115, 320, 40);
        idField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        cardPanel.add(idField);

        // Password Field
        JLabel pwdLabel = new JLabel("Password");
        pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pwdLabel.setForeground(primaryColor);
        pwdLabel.setBounds(40, 175, 320, 20);
        cardPanel.add(pwdLabel);

        pwdField = new JPasswordField();
        pwdField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pwdField.setBounds(40, 200, 320, 40);
        pwdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        cardPanel.add(pwdField);

        // Login Button
        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(primaryColor);
        loginBtn.setBounds(40, 280, 320, 45);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> login());
        cardPanel.add(loginBtn);

        // Register Link
        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noAccountLabel.setBounds(40, 350, 160, 20);
        cardPanel.add(noAccountLabel);

        JButton registerBtn = new JButton("Sign up");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setForeground(primaryColor);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setBounds(190, 350, 100, 20);
        registerBtn.setHorizontalAlignment(SwingConstants.LEFT);
        registerBtn.addActionListener(e -> {
            new ModernRegisterFrame();
        });
        cardPanel.add(registerBtn);

        mainPanel.add(cardPanel);

        // Enter key support
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        };
        idField.addKeyListener(enterKeyAdapter);
        pwdField.addKeyListener(enterKeyAdapter);
    }

    private void login() {
        String account = idField.getText().trim();
        String pwd = new String(pwdField.getPassword());

        if (account.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Request req = new Request();
        req.setAction("userLogin");
        req.setAttribute("account", account);
        req.setAttribute("password", pwd);

        try {
            Response response = ClientUtil.sendTextRequest(req);
            if (response.getStatus() == ResponseStatus.OK) {
                User user = (User) response.getData("user");
                if (user != null) {
                    DataBuffer.currentUser = user;
                    DataBuffer.onlineUsers = (List<User>) response.getData("onlineUsers");
                    dispose();
                    new SpringChatFrame();
                } else {
                    JOptionPane.showMessageDialog(this, response.getData("msg"), "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Server Error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
