package client.ui;

import client.util.ClientUtil;
import common.model.entity.Request;
import common.model.entity.Response;
import common.model.entity.ResponseStatus;
import common.model.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class ModernRegisterFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField accountField;
    private JTextField nicknameField;
    private JPasswordField pwdField;
    private JPasswordField confirmPwdField;
    private JComboBox<String> genderCombo;
    private JComboBox<Integer> headCombo;
    private Color primaryColor = new Color(0, 128, 105);
    private Color backgroundColor = new Color(240, 242, 245);

    public ModernRegisterFrame() {
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setTitle("SpringChat Register");
        setSize(500, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
                    g.setColor(new Color(236, 229, 221));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // Header Bar (Teal)
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primaryColor);
                g2.fillRect(0, 0, getWidth(), 100);

                // Title
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                String title = "Create Account";
                int textX = (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2;
                g2.drawString(title, textX, 60);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        add(mainPanel);

        JPanel cardPanel = new JPanel();
        cardPanel.setPreferredSize(new Dimension(420, 630));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setLayout(null);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        titleLabel.setForeground(new Color(65, 82, 93));
        titleLabel.setBounds(40, 20, 340, 30);
        cardPanel.add(titleLabel);

        // Account
        addLabel(cardPanel, "Account (Unique)", 70);
        accountField = addTextField(cardPanel, 95);

        // Nickname
        addLabel(cardPanel, "Nickname", 145);
        nicknameField = addTextField(cardPanel, 170);

        // Password
        addLabel(cardPanel, "Password", 220);
        pwdField = addPasswordField(cardPanel, 245);

        // Confirm Password
        addLabel(cardPanel, "Confirm Password", 295);
        confirmPwdField = addPasswordField(cardPanel, 320);

        // Gender
        addLabel(cardPanel, "Gender", 370);
        genderCombo = new JComboBox<>(new String[] { "M", "F" });
        genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        genderCombo.setBackground(Color.WHITE);
        genderCombo.setBounds(40, 395, 340, 35);
        cardPanel.add(genderCombo);

        // Avatar
        addLabel(cardPanel, "Select Avatar", 445);
        Integer[] heads = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        headCombo = new JComboBox<>(heads);
        headCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                ImageIcon originalIcon = new ImageIcon("images/" + value + ".png");
                // Scale image to fit combo box height while maintaining aspect ratio
                Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.setText(" Avatar " + value);
                return label;
            }
        });
        headCombo.setBounds(40, 470, 340, 50);
        cardPanel.add(headCombo);

        // Register Button
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(primaryColor);
        registerBtn.setBounds(40, 555, 340, 45);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> register());
        cardPanel.add(registerBtn);

        mainPanel.add(cardPanel);
    }

    private void addLabel(JPanel panel, String text, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(primaryColor);
        label.setBounds(40, y, 340, 20);
        panel.add(label);
    }

    private JTextField addTextField(JPanel panel, int y) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBounds(40, y, 340, 35);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        panel.add(field);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, int y) {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBounds(40, y, 340, 35);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        panel.add(field);
        return field;
    }

    private void register() {
        String account = accountField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String pwd = new String(pwdField.getPassword());
        String confirmPwd = new String(confirmPwdField.getPassword());

        if (account.isEmpty() || nickname.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pwd.equals(confirmPwd)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate account format (alphanumeric, 3-20 characters)
        if (!account.matches("^[a-zA-Z0-9_]{3,20}$")) {
            JOptionPane.showMessageDialog(this, 
                "Account must be 3-20 characters (letters, numbers, underscore only)", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = new User(account, pwd, nickname, genderCombo.getSelectedItem().toString().charAt(0),
                (Integer) headCombo.getSelectedItem());
        Request req = new Request();
        req.setAction("userRegister");
        req.setAttribute("user", user);

        try {
            Response response = ClientUtil.sendTextRequest(req);
            if (response.getStatus() == ResponseStatus.OK) {
                String msg = (String) response.getData("msg");
                if (msg != null) {
                    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    User registeredUser = (User) response.getData("user");
                    JOptionPane.showMessageDialog(this,
                            "Registration Successful!\nYour Account is: " + registeredUser.getAccount(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Registration Failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
