package client.ui;

import client.ClientThread;
import client.DataBuffer;
import client.model.entity.OnlineUserListModel;
import client.util.ClientUtil;
import client.util.JFrameShaker;
import client.util.MessageHistoryService;
import common.model.entity.FileInfo;
import common.model.entity.Message;
import common.model.entity.Request;
import common.model.entity.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class SpringChatFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    // SpringChat Colors
    private static final Color TEAL_GREEN = new Color(0, 128, 105);
    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color CHAT_BG_COLOR = new Color(236, 229, 221);
    private static final Color OUTGOING_BUBBLE = new Color(217, 253, 211);
    private static final Color INCOMING_BUBBLE = Color.WHITE;
    private static final Color ICON_GRAY = new Color(84, 101, 111);

    public static SpringChatFrame instance;

    // Components
    private JList<User> userList;
    private JPanel messageContainer;
    private JTextArea inputArea;
    private JLabel currentChatUserLabel;
    private JLabel currentChatStatusLabel;
    private JLabel onlineCountLabel;
    private Image backgroundImage;
    private JLabel headerAvatar;

    // State
    private Map<Long, List<Message>> messageHistory = new HashMap<>();
    // Map<PeerID, LastReadTimestamp>
    private Map<Long, Long> peerReadTimes = new HashMap<>();
    // Map<ChatKey, UnreadCount>
    private Map<Long, Integer> unreadCounts = new HashMap<>();

    private User currentChatUser;
    private boolean isPrivateChat = false;
    private Long currentChatKey = null;
    private SendIcon sendIcon;
    private JButton sendBtn;
    private int hoveredIndex = -1;
    private static final Long GROUP_CHAT_KEY = -1L;
    
    // Reply/Search state
    private Message replyToMessage = null;
    private JPanel replyPreviewPanel = null;
    private JPanel searchPanel = null;
    private List<Integer> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

    public SpringChatFrame() {
        instance = this;
        try {
            backgroundImage = ImageIO.read(new File("images/whatsapp_bg.png"));
        } catch (IOException e) {
            // Ignore
        }
        initUI();
        loadData();
        setVisible(true);
    }

    private void initUI() {
        setTitle("SpringChat");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        // --- LEFT PANEL ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(SIDEBAR_BG);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        // Left Header
        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(new Color(240, 242, 245));
        leftHeader.setBorder(new EmptyBorder(10, 16, 10, 16));
        leftHeader.setPreferredSize(new Dimension(350, 60));

        JLabel myAvatar = new JLabel(getCircleIcon("images/" + DataBuffer.currentUser.getHead() + ".png", 40));
        leftHeader.add(myAvatar, BorderLayout.WEST);

        // Icons Right
        JPanel leftIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        leftIcons.setOpaque(false);
        leftIcons.add(createIconButton(new StatusIcon(), "Status"));
        leftIcons.add(createIconButton(new NewChatIcon(), "New Chat"));
        leftIcons.add(createIconButton(new MenuIcon(), "Menu"));
        leftHeader.add(leftIcons, BorderLayout.EAST);

        leftPanel.add(leftHeader, BorderLayout.NORTH);

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new BorderLayout());
        searchBar.setBackground(new Color(240, 242, 245));
        searchBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        // Rounded border logic could be added here

        JLabel searchIcon = new JLabel(new SearchIcon());
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
        searchBar.add(searchIcon, BorderLayout.WEST);

        JTextField searchField = new JTextField("Search or start new chat");
        searchField.setBackground(new Color(240, 242, 245));
        searchField.setBorder(null);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(Color.GRAY);
        searchBar.add(searchField, BorderLayout.CENTER);

        searchPanel.add(searchBar, BorderLayout.CENTER);

        // User List
        userList = new JList<>();
        userList.setCellRenderer(new SpringChatUserRenderer());
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBorder(null);

        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.add(searchPanel, BorderLayout.NORTH);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        listWrapper.add(userScrollPane, BorderLayout.CENTER);

        leftPanel.add(listWrapper, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Right Header
        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setBackground(new Color(240, 242, 245));
        rightHeader.setBorder(new EmptyBorder(10, 16, 10, 16));
        rightHeader.setPreferredSize(new Dimension(700, 60));
        rightHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JPanel chatInfoPanel = new JPanel(new BorderLayout(10, 0));
        chatInfoPanel.setOpaque(false);

        // Avatar in header
        headerAvatar = new JLabel(getCircleIcon("images/group_avatar.png", 40));
        chatInfoPanel.add(headerAvatar, BorderLayout.WEST);

        JPanel textInfo = new JPanel(new GridLayout(2, 1));
        textInfo.setOpaque(false);
        currentChatUserLabel = new JLabel("Group Chat");
        currentChatUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textInfo.add(currentChatUserLabel);

        currentChatStatusLabel = new JLabel("Tap here for contact info");
        currentChatStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        currentChatStatusLabel.setForeground(Color.GRAY);
        textInfo.add(currentChatStatusLabel);

        chatInfoPanel.add(textInfo, BorderLayout.CENTER);
        rightHeader.add(chatInfoPanel, BorderLayout.CENTER);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        rightActions.setOpaque(false);

        JButton shakeBtn = createIconButton(new ShakeIcon(), "Shake");
        shakeBtn.addActionListener(e -> sendShakeMsg());
        rightActions.add(shakeBtn);

        JButton searchBtn = createIconButton(new SearchIcon(), "Search");
        searchBtn.addActionListener(e -> toggleSearchPanel());
        rightActions.add(searchBtn);
        rightActions.add(createIconButton(new MenuIcon(), "Menu"));

        rightHeader.add(rightActions, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);

        // Messages
        messageContainer = new BackgroundPanel();
        messageContainer.setLayout(new BoxLayout(messageContainer, BoxLayout.Y_AXIS));

        JPanel messageWrapper = new BackgroundPanel();
        messageWrapper.setLayout(new BorderLayout());
        messageWrapper.add(messageContainer, BorderLayout.NORTH);

        JScrollPane messageScroll = new JScrollPane(messageWrapper);
        messageScroll.setBorder(null);
        messageScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(messageScroll, BorderLayout.CENTER);

        // Input
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(new Color(240, 242, 245));
        inputPanel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel inputLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        inputLeft.setOpaque(false);
        JButton emojiBtn = createIconButton(new SmileyIcon(), "Emoji");
        emojiBtn.addActionListener(e -> showEmojiPanel(emojiBtn));
        inputLeft.add(emojiBtn);
        JButton attachBtn = createIconButton(new ClipIcon(), "Attach");
        attachBtn.addActionListener(e -> sendFile());
        inputLeft.add(attachBtn);

        inputPanel.add(inputLeft, BorderLayout.WEST);

        inputArea = new JTextArea();
        inputArea.setRows(1);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        // 浣跨敤鏀寔emoji鐨勫瓧浣?
        inputArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        inputArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel inputFieldWrapper = new JPanel(new BorderLayout());
        inputFieldWrapper.setBackground(Color.WHITE);
        inputFieldWrapper.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        inputFieldWrapper.add(inputArea, BorderLayout.CENTER);

        inputPanel.add(inputFieldWrapper, BorderLayout.CENTER);

        sendBtn = createIconButton(new SendIcon(ICON_GRAY), "Send");
        sendBtn.addActionListener(e -> sendTxtMsg());
        inputPanel.add(sendBtn, BorderLayout.EAST);

        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSendIcon();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSendIcon();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSendIcon();
            }
        });

        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Listeners
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        inputArea.append("\n");
                    } else {
                        e.consume();
                        sendTxtMsg();
                    }
                }
            }
        });

        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                userList.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                User selected = userList.getSelectedValue();
                if (selected != null) {
                    if (selected.getId() == -1L) { // Group Chat virtual user
                        // Switch to group chat
                        currentChatUserLabel.setText("Group Chat");
                        currentChatStatusLabel.setText("Group Chat");
                        headerAvatar.setIcon(getCircleIcon("images/group_avatar.png", 40));
                        isPrivateChat = false;
                        currentChatUser = null;
                        userList.clearSelection();
                        switchChatView(GROUP_CHAT_KEY);
                    } else {
                        // Switch to private chat
                        isPrivateChat = true;
                        currentChatUser = selected;
                        currentChatUserLabel.setText(selected.getNickname());
                        currentChatStatusLabel.setText("Online");
                        headerAvatar.setIcon(getCircleIcon("images/" + selected.getHead() + ".png", 40));
                        switchChatView(selected.getId());
                    }
                } else {
                    // Deselected - switch to group chat
                    isPrivateChat = false;
                    currentChatUser = null;
                    currentChatUserLabel.setText("Group Chat");
                    currentChatStatusLabel.setText("Tap here for contact info");
                    headerAvatar.setIcon(getCircleIcon("images/group_avatar.png", 40));
                    switchChatView(GROUP_CHAT_KEY);
                }
            }
        });

        userList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index != hoveredIndex) {
                    hoveredIndex = index;
                    userList.repaint();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        // 銆愭柊澧炪€戞坊鍔犺彍鍗曟爮
        createMenuBar();
    }

    // --- Menu Bar ---

    /**
     * 鍒涘缓鑿滃崟鏍?
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Chat menu
        JMenu chatMenu = new JMenu("Chat");
        chatMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JMenuItem clearCurrentItem = new JMenuItem("Clear Current Chat");
        clearCurrentItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearCurrentItem.addActionListener(e -> clearCurrentChat());
        chatMenu.add(clearCurrentItem);

        JMenuItem clearAllItem = new JMenuItem("Clear All Chats");
        clearAllItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearAllItem.addActionListener(e -> clearAllChats());
        chatMenu.add(clearAllItem);

        chatMenu.addSeparator();

        JMenuItem statsItem = new JMenuItem("Storage Stats");
        statsItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsItem.addActionListener(e -> showHistoryStats());
        chatMenu.add(statsItem);

        menuBar.add(chatMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JMenuItem aboutItem = new JMenuItem("About Persistence");
        aboutItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        aboutItem.addActionListener(e -> showAboutPersistence());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 娓呯┖褰撳墠鑱婂ぉ鐨勫巻鍙茶褰?
     */
    private void clearCurrentChat() {
        String chatName = isPrivateChat ? currentChatUser.getNickname() : "Group Chat";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all history with " + chatName + "?\n\nThis action cannot be undone!",
                "Clear Chat History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Long chatKey = isPrivateChat ? currentChatUser.getId() : GROUP_CHAT_KEY;

            // Clear messages in memory
            messageHistory.put(chatKey, new ArrayList<>());

            // Delete local file
            MessageHistoryService.clearMessages(DataBuffer.currentUser.getId(), chatKey);

            // Refresh display
            messageContainer.removeAll();
            refreshChat();

            JOptionPane.showMessageDialog(this,
                    "Chat history with " + chatName + " has been cleared",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 娓呯┖鎵€鏈夎亰澶╄褰?
     */
    private void clearAllChats() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all chat history?\n\nIncluding:\n" +
                        "鈥?Group chat history\n" +
                        "鈥?All private chat history\n\n" +
                        "This action cannot be undone!",
                "Clear All History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Clear all messages in memory
            messageHistory.clear();
            messageHistory.put(GROUP_CHAT_KEY, new ArrayList<>());

            // Delete all local files
            MessageHistoryService.clearAllMessages(DataBuffer.currentUser.getId());

            // Refresh display
            messageContainer.removeAll();
            refreshChat();

            JOptionPane.showMessageDialog(this,
                    "All chat history has been cleared",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 鏄剧ず瀛樺偍缁熻淇℃伅
     */
    private void showHistoryStats() {
        int totalChats = messageHistory.size();
        int totalMessages = messageHistory.values().stream()
                .mapToInt(List::size)
                .sum();

        String storageInfo = MessageHistoryService.getHistoryStats(DataBuffer.currentUser.getId());

        String message = String.format(
                "Chat History Statistics\n\n" +
                        "Total Conversations: %d\n" +
                        "Total Messages: %d\n" +
                        "Storage Info: %s\n\n" +
                        "Storage Location: chat_history/%d/",
                totalChats, totalMessages, storageInfo, DataBuffer.currentUser.getId());

        JOptionPane.showMessageDialog(this,
                message,
                "Storage Stats",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 鏄剧ず娑堟伅鎸佷箙鍖栧姛鑳借鏄?
     */
    private void showAboutPersistence() {
        String message = "Message Persistence Feature\n\n" +
                "鉁?Automatically save all chat history\n" +
                "鉁?History persists after logout and re-login\n" +
                "鉁?Group and private chats stored separately\n" +
                "鉁?Support clearing individual or all chat history\n\n" +
                "Storage Method: Local JSON files\n" +
                "Storage Location: chat_history/{User ID}/\n\n" +
                "Note: Messages are only saved locally, not uploaded to server";

        JOptionPane.showMessageDialog(this,
                message,
                "About Message Persistence",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Helpers ---

    private JButton createIconButton(Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false); // Fix: prevent background remnants
        btn.setMargin(new Insets(5, 5, 5, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(0, 0, 0, 20));
                btn.repaint(); // Fix: ensure proper repaint
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setOpaque(false); // 寮哄埗璁剧疆涓洪潪閫忔槑
                btn.setBackground(null);
                // 寮哄埗娓呴櫎UI缂撳瓨
                btn.getModel().setRollover(false);
                btn.getModel().setPressed(false);
                btn.invalidate(); // 鏍囪涓洪渶瑕侀噸缁?
                btn.repaint();
            }
        });

        // Circular background
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                // 鍙湪鐪熸闇€瑕佹椂缁樺埗鑳屾櫙
                if (c.isOpaque() && ((JButton) c).getModel().isRollover()
                        && c.getBackground() != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c.getBackground());
                    g2.fillOval(0, 0, c.getWidth(), c.getHeight());
                    g2.dispose();
                }
                super.paint(g, c);
            }
        });

        return btn;
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                int w = getWidth();
                int h = getHeight();
                int imgW = backgroundImage.getWidth(null);
                int imgH = backgroundImage.getHeight(null);
                for (int x = 0; x < w; x += imgW) {
                    for (int y = 0; y < h; y += imgH) {
                        g.drawImage(backgroundImage, x, y, this);
                    }
                }
            } else {
                g.setColor(CHAT_BG_COLOR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private ImageIcon getCircleIcon(String path, int size) {
        try {
            BufferedImage master = ImageIO.read(new File(path));
            int diameter = Math.min(master.getWidth(), master.getHeight());
            BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = mask.createGraphics();
            applyQualityRenderingHints(g2d);
            g2d.fillOval(0, 0, diameter, diameter);
            g2d.dispose();

            BufferedImage masked = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            g2d = masked.createGraphics();
            applyQualityRenderingHints(g2d);
            int x = (diameter - master.getWidth()) / 2;
            int y = (diameter - master.getHeight()) / 2;
            g2d.drawImage(master, x, y, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
            g2d.drawImage(mask, 0, 0, null);
            g2d.dispose();

            Image scaled = masked.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return new ImageIcon(path);
        }
    }

    private void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // --- Logic ---

    private void loadData() {
        // Create virtual Group Chat user
        User groupChatUser = new User(-1L, "");
        groupChatUser.setNickname("Group Chat");
        groupChatUser.setHead(-1); // Special value to indicate group chat avatar
        groupChatUser.setSex('N'); // Neutral

        // Remove current user from list if present
        DataBuffer.onlineUsers.removeIf(u -> u.getId() == DataBuffer.currentUser.getId());

        // Add group chat user at the beginning
        if (!DataBuffer.onlineUsers.isEmpty()) {
            DataBuffer.onlineUsers.add(0, groupChatUser);
        } else {
            DataBuffer.onlineUsers.add(groupChatUser);
        }

        DataBuffer.onlineUserListModel = new OnlineUserListModel(DataBuffer.onlineUsers);
        userList.setModel(DataBuffer.onlineUserListModel);
        updateOnlineCount();

        // Load all message history from local files
        System.out.println("\n====== Loading Chat History ======");
        messageHistory = MessageHistoryService.loadAllMessages(DataBuffer.currentUser.getId());
        System.out.println("==================================\n");

        // 纭繚缇よ亰璁板綍瀛樺湪
        if (!messageHistory.containsKey(GROUP_CHAT_KEY)) {
            messageHistory.put(GROUP_CHAT_KEY, new ArrayList<>());
        }

        // Set default view to group chat
        switchChatView(GROUP_CHAT_KEY);
        new ClientThread(this).start();
    }

    public void updateOnlineCount() {
        // No explicit online count label in new design, maybe update title or status
    }

    public void appendSystemMessage(String text) {
        // System messages are always shown in current view
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        JLabel label = new JLabel(text.trim());
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(80, 80, 80));
        label.setOpaque(true);
        label.setBackground(new Color(255, 252, 204));
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(label);
        messageContainer.add(panel);
        messageContainer.add(Box.createVerticalStrut(10));
        refreshChat();
    }

    public void appendMessage(Message msg) {
        // Handle Read Receipt
        if (msg.getMessage().startsWith("@@READ@@")) {
            long peerId = msg.getFromUser().getId();
            peerReadTimes.put(peerId, msg.getSendTime().getTime());
            if (isPrivateChat && currentChatUser != null && currentChatUser.getId() == peerId) {
                refreshChat();
            }
            return;
        }
        
        // Handle Recall Message
        if (msg.getMessage().startsWith("@@RECALL@@")) {
            String timeStr = msg.getMessage().substring(10);
            long recalledTime = Long.parseLong(timeStr);
            
            Long chatKey = determineChatKey(msg);
            List<Message> messages = messageHistory.get(chatKey);
            if (messages != null) {
                messages.removeIf(m -> m.getSendTime().getTime() == recalledTime);
                MessageHistoryService.saveMessages(
                    DataBuffer.currentUser.getId(),
                    chatKey,
                        messages);
                
                // Refresh display if viewing this chat
                if (shouldDisplayInCurrentView(msg)) {
                    switchChatView(chatKey);
                    String senderName = msg.getFromUser().getId() == DataBuffer.currentUser.getId() ? "You"
                            : msg.getFromUser().getNickname();
                    appendSystemMessage(senderName + " recalled a message");
                }
            }
            return;
        }

        // Determine which chat this message belongs to
        Long chatKey = determineChatKey(msg);

        // Ensure message history exists for this chat
        if (!messageHistory.containsKey(chatKey)) {
            messageHistory.put(chatKey, new ArrayList<>());
        }

        // Store message in history
        messageHistory.get(chatKey).add(msg);

        // 銆愭柊澧炪€戜繚瀛樺埌鏈湴鏂囦欢
        MessageHistoryService.saveMessages(
                DataBuffer.currentUser.getId(),
                chatKey,
                messageHistory.get(chatKey));

        // Only display if this message belongs to current chat view
        if (shouldDisplayInCurrentView(msg)) {
            displayMessage(msg);
            // If we are viewing this chat, send read receipt back
            if (isPrivateChat && msg.getFromUser().getId() != DataBuffer.currentUser.getId()) {
                sendReadReceipt(msg.getFromUser().getId());
            }
        } else {
            // Increment unread count for non-visible chats
            unreadCounts.put(chatKey, unreadCounts.getOrDefault(chatKey, 0) + 1);
            userList.repaint(); // Refresh to show badge
        }
    }

    private void sendReadReceipt(Long toUserId) {
        new Thread(() -> {
            try {
                Message msg = new Message();
                msg.setFromUser(DataBuffer.currentUser);
                User toUser = new User(toUserId, "");
                msg.setToUser(toUser);
                msg.setSendTime(new Date());
                msg.setMessage("@@READ@@");

                Request request = new Request();
                request.setAction("chat");
                request.setAttribute("msg", msg);
                ClientUtil.sendTextRequest2(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Long determineChatKey(Message msg) {
        boolean isMe = msg.getFromUser().getId() == DataBuffer.currentUser.getId();

        if (msg.getToUser() == null) {
            return GROUP_CHAT_KEY;
        } else {
            if (isMe) {
                return msg.getToUser().getId();
            } else {
                return msg.getFromUser().getId();
            }
        }
    }

    private boolean shouldDisplayInCurrentView(Message msg) {
        boolean isMe = msg.getFromUser().getId() == DataBuffer.currentUser.getId();

        if (!isPrivateChat) {
            return msg.getToUser() == null;
        } else {
            if (msg.getToUser() == null) {
                return false;
            }

            long otherUserId = currentChatUser.getId();
            if (isMe) {
                return msg.getToUser().getId() == otherUserId;
            } else {
                return msg.getFromUser().getId() == otherUserId;
            }
        }
    }

    private void displayMessage(Message msg) {
        boolean isMe = msg.getFromUser().getId() == DataBuffer.currentUser.getId();
        JPanel rowPanel = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        rowPanel.setOpaque(false);
        rowPanel.setBorder(new EmptyBorder(2, 20, 2, 20));

        BubblePanel bubble = new BubblePanel(isMe);
        bubble.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // Only show nickname in group chat, not in private chat
        if (!isMe && !isPrivateChat) {
            JLabel nameLabel = new JLabel(msg.getFromUser().getNickname());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            nameLabel.setForeground(getColorForUser(msg.getFromUser().getId()));
            topPanel.add(nameLabel, BorderLayout.NORTH);
        }
        
        // Check if this is a reply message
        String messageText = msg.getMessage();
        if (messageText.startsWith(">>reply:")) {
            int firstNewLine = messageText.indexOf('\n');
            if (firstNewLine > 0) {
                String replyInfo = messageText.substring(8, firstNewLine);
                String[] parts = replyInfo.split(":", 3);
                if (parts.length >= 3) {
                    JPanel replyQuote = new JPanel(new BorderLayout());
                    replyQuote.setOpaque(true);
                    replyQuote.setBackground(new Color(0, 0, 0, 20));
                    replyQuote.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, TEAL_GREEN),
                            new EmptyBorder(5, 8, 5, 8)));
                    
                    JLabel replyLabel = new JLabel("<html><b>" + parts[1] + "</b><br>" + 
                                                   parts[2] + "</html>");
                    replyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    replyLabel.setForeground(new Color(102, 119, 129));
                    replyQuote.add(replyLabel);
                    
                    topPanel.add(replyQuote, BorderLayout.CENTER);
                }
                messageText = messageText.substring(firstNewLine + 1);
            }
        }
        
        contentPanel.add(topPanel, BorderLayout.NORTH);

        JTextArea msgText = new JTextArea(messageText);
        // 浣跨敤鏀寔emoji鐨勫瓧浣?
        msgText.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        msgText.setLineWrap(true);
        msgText.setWrapStyleWord(true);
        msgText.setEditable(false);
        msgText.setOpaque(false);

        JPopupMenu popup = new JPopupMenu();
        
        // Reply option
        JMenuItem replyItem = new JMenuItem("Reply");
        replyItem.addActionListener(e -> setReplyMessage(msg));
        popup.add(replyItem);
        
        // Copy option
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            StringSelection selection = new StringSelection(msg.getMessage());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        });
        popup.add(copyItem);
        
        // Recall option (only for own messages within 2 minutes)
        if (isMe && withinRecallTime(msg)) {
            popup.addSeparator();
            JMenuItem recallItem = new JMenuItem("Recall");
            recallItem.addActionListener(e -> recallMessage(msg));
            popup.add(recallItem);
        }
        
        msgText.setComponentPopupMenu(popup);

        // Adaptive Bubble Sizing
        int maxWidth = 450;
        FontMetrics fm = msgText.getFontMetrics(msgText.getFont());
        int textWidth = fm.stringWidth(messageText);

        // Add padding for text area borders/insets
        int padding = 20;

        if (textWidth + padding < maxWidth) {
            // If text is short, set size to fit text
            msgText.setSize(new Dimension(textWidth + padding, Short.MAX_VALUE));
            msgText.setPreferredSize(new Dimension(textWidth + padding, msgText.getPreferredSize().height));
        } else {
            // If text is long, cap at maxWidth and let it wrap
        msgText.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
            msgText.setPreferredSize(new Dimension(maxWidth, msgText.getPreferredSize().height));
        }

        contentPanel.add(msgText, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(5, 0, 0, 0));

        DateFormat df = new SimpleDateFormat("HH:mm");
        JLabel timeLabel = new JLabel(df.format(msg.getSendTime()));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(102, 119, 129));
        footer.add(timeLabel);

        if (isMe && isPrivateChat && msg.getToUser() != null) {
            long toUserId = msg.getToUser().getId();
            Long readTime = peerReadTimes.get(toUserId);
            boolean isRead = readTime != null && msg.getSendTime().getTime() <= readTime;

            JLabel tickLabel = new JLabel(isRead ? "\u2713\u2713" : "\u2713");
            tickLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 10));
            tickLabel.setForeground(isRead ? new Color(52, 183, 241) : new Color(102, 119, 129));
            footer.add(tickLabel);
        }

        contentPanel.add(footer, BorderLayout.SOUTH);

        bubble.add(contentPanel, BorderLayout.CENTER);
        rowPanel.add(bubble);
        messageContainer.add(rowPanel);
        messageContainer.add(Box.createVerticalStrut(5));
        refreshChat();
    }

    /**
     * Switch chat view between group chat and private chat
     */
    private void switchChatView(Long chatKey) {
        currentChatKey = chatKey;
        
        // Clear unread count for this chat
        unreadCounts.remove(chatKey);
        userList.repaint(); // Refresh to hide badge
        
        messageContainer.removeAll();

        List<Message> messages = messageHistory.get(chatKey);
        if (messages != null) {
            for (Message msg : messages) {
                displayMessage(msg);
            }
        }

        // If switching to private chat, send read receipt
        if (isPrivateChat && currentChatUser != null) {
            sendReadReceipt(currentChatUser.getId());
        }

        // Refresh display
        messageContainer.revalidate();
        messageContainer.repaint();
        refreshChat();
    }

    private Color getColorForUser(long id) {
        Color[] colors = { new Color(214, 40, 40), new Color(247, 127, 0), new Color(0, 48, 73),
                new Color(42, 157, 143) };
        return colors[(int) (id % colors.length)];
    }

    private void refreshChat() {
        messageContainer.revalidate();
        messageContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = ((JScrollPane) messageContainer.getParent().getParent()).getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private void sendTxtMsg() {
        String content = inputArea.getText().trim();
        if (content.isEmpty())
            return;
        
        Message msg = new Message();
        msg.setFromUser(DataBuffer.currentUser);
        msg.setSendTime(new Date());
        
        // Add reply information if replying to a message
        if (replyToMessage != null) {
            String replyPrefix = ">>reply:" + replyToMessage.getSendTime().getTime() + ":" +
                               replyToMessage.getFromUser().getNickname() + ": " +
                    (replyToMessage.getMessage().length() > 30 ? replyToMessage.getMessage().substring(0, 30) + "..."
                            : replyToMessage.getMessage())
                    + "\n";
            content = replyPrefix + content;
            replyToMessage = null;
            if (replyPreviewPanel != null) {
                replyPreviewPanel.setVisible(false);
            }
        }
        
        msg.setMessage(content);
        if (isPrivateChat && currentChatUser != null)
            msg.setToUser(currentChatUser);
        Request request = new Request();
        request.setAction("chat");
        request.setAttribute("msg", msg);
        try {
            ClientUtil.sendTextRequest2(request);
            inputArea.setText("");
            appendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendShakeMsg() {
        if (currentChatUser == null)
            return;
        Message msg = new Message();
        msg.setFromUser(DataBuffer.currentUser);
        msg.setToUser(currentChatUser);
        msg.setSendTime(new Date());

        // Build shake message
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(msg.getFromUser().getNickname())
                .append("(").append(msg.getFromUser().getId()).append(") ")
                .append(df.format(msg.getSendTime()))
                .append("\n  sent a shake to ").append(currentChatUser.getNickname())
                .append("(").append(currentChatUser.getId()).append(")\n");
        msg.setMessage(sb.toString());

        Request request = new Request();
        request.setAction("shake");
        request.setAttribute("msg", msg);
        try {
            ClientUtil.sendTextRequest2(request);
            appendSystemMessage("You sent a shake to " + currentChatUser.getNickname());
            new JFrameShaker(this).startShake();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile() {
        if (currentChatUser == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a user to send file!",
                    "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Select file to send");
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            
            // Check file size (limit to 10MB)
            long fileSize = file.length();
            if (fileSize > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                        "File size exceeds limit (10MB)",
                        "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            FileInfo sendFile = new FileInfo();
            sendFile.setFromUser(DataBuffer.currentUser);
            sendFile.setToUser(currentChatUser);
            try {
                sendFile.setSrcName(file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendFile.setSendTime(new Date());
            Request request = new Request();
            request.setAction("toSendFile");
            request.setAttribute("file", sendFile);
            try {
                ClientUtil.sendTextRequest2(request);
                
                // Show file card in chat
                String fileSizeStr = formatFileSize(fileSize);
                appendSystemMessage("Sending file: " + file.getName() +
                        " (" + fileSizeStr + ") to " + currentChatUser.getNickname());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "File send failed: " + e.getMessage(),
                        "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    private void updateSendIcon() {
        if (inputArea.getText().trim().isEmpty()) {
            sendBtn.setIcon(new SendIcon(ICON_GRAY));
        } else {
            sendBtn.setIcon(new SendIcon(TEAL_GREEN));
        }
    }

    private void logout() {
        int select = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?\n\nAll chat history has been automatically saved locally",
                "Exit",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            // Save all messages one last time before exit
            System.out.println("\n====== Saving Chat History ======");
            for (Map.Entry<Long, List<Message>> entry : messageHistory.entrySet()) {
                MessageHistoryService.saveMessages(
                        DataBuffer.currentUser.getId(),
                        entry.getKey(),
                        entry.getValue());
            }
            System.out.println("=================================\n");

            Request req = new Request();
            req.setAction("exit");
            req.setAttribute("user", DataBuffer.currentUser);
            try {
                ClientUtil.sendTextRequest(req);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                System.exit(0);
            }
        }
    }

    // --- Vector Icons ---

    private abstract class VectorIcon implements Icon {
        protected int width, height;

        public VectorIcon(int w, int h) {
            this.width = w;
            this.height = h;
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            paintVector(g2);
            g2.dispose();
        }

        protected abstract void paintVector(Graphics2D g2);
    }

    private class MenuIcon extends VectorIcon {
        public MenuIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.fillOval(11, 5, 2, 2);
            g2.fillOval(11, 11, 2, 2);
            g2.fillOval(11, 17, 2, 2);
        }
    }

    private class SearchIcon extends VectorIcon {
        public SearchIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(5, 5, 11, 11);
            g2.drawLine(14, 14, 18, 18);
        }
    }

    private class ClipIcon extends VectorIcon {
        public ClipIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.rotate(Math.toRadians(45), 12, 12);
            g2.drawRoundRect(8, 6, 8, 14, 4, 4);
            g2.drawLine(12, 8, 12, 16);
        }
    }

    private class SmileyIcon extends VectorIcon {
        public SmileyIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(3, 3, 18, 18);
            g2.fillOval(8, 9, 2, 2);
            g2.fillOval(14, 9, 2, 2);
            g2.drawArc(7, 11, 10, 6, 0, -180);
        }
    }

    private class SendIcon extends VectorIcon {
        private Color color;

        public SendIcon(Color color) {
            super(24, 24);
            this.color = color;
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(color);
            Path2D p = new Path2D.Double();
            p.moveTo(3, 4);
            p.lineTo(21, 12);
            p.lineTo(3, 20);
            p.lineTo(5, 12);
            p.closePath();
            g2.fill(p);
        }
    }

    private class ShakeIcon extends VectorIcon {
        public ShakeIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(8, 5, 8, 14);
            g2.drawLine(6, 12, 4, 10);
            g2.drawLine(18, 12, 20, 10);
            g2.drawLine(6, 14, 4, 16);
            g2.drawLine(18, 14, 20, 16);
        }
    }

    private class StatusIcon extends VectorIcon {
        public StatusIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(4, 4, 16, 16);
            g2.drawArc(4, 4, 16, 16, 90, 180); // Dashed effect simulation or partial arc
        }
    }

    private class NewChatIcon extends VectorIcon {
        public NewChatIcon() {
            super(24, 24);
        }

        protected void paintVector(Graphics2D g2) {
            g2.setColor(ICON_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(5, 5, 14, 12);
            g2.drawLine(9, 9, 15, 9);
            g2.drawLine(9, 13, 13, 13);
        }
    }

    private class SpringChatUserRenderer extends JPanel implements ListCellRenderer<User> {
        private JLabel nameLabel;
        private JLabel avatarLabel;
        private JLabel statusLabel;
        private JLabel unreadLabel;
        private JPanel avatarPanel;

        public SpringChatUserRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setOpaque(true);

            // Avatar Panel with overlay support
            avatarPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                }
            };
            avatarPanel.setLayout(new BorderLayout());
            avatarPanel.setOpaque(false);

            avatarLabel = new JLabel();
            avatarPanel.add(avatarLabel, BorderLayout.CENTER);
            add(avatarPanel, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
            textPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(new Color(17, 27, 33));
            textPanel.add(nameLabel);

            statusLabel = new JLabel("Hey there! I am using SpringChat.");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            statusLabel.setForeground(new Color(102, 119, 129));
            textPanel.add(statusLabel);

            add(textPanel, BorderLayout.CENTER);

            // Create right panel for time and badge
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);

            JLabel timeLabel = new JLabel("Yesterday");
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            timeLabel.setForeground(new Color(102, 119, 129));
            timeLabel.setVerticalAlignment(SwingConstants.TOP);
            rightPanel.add(timeLabel, BorderLayout.NORTH);

            // Create unread badge with circular background
            unreadLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    if (isVisible() && getBackground() != null) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillOval(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            unreadLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            unreadLabel.setForeground(Color.WHITE);
            unreadLabel.setBackground(new Color(220, 53, 69)); // Bootstrap danger red
            unreadLabel.setOpaque(false); // Let custom paint handle background
            unreadLabel.setHorizontalAlignment(SwingConstants.CENTER);
            unreadLabel.setPreferredSize(new Dimension(22, 22));
            unreadLabel.setVisible(false);

            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
            badgePanel.setOpaque(false);
            badgePanel.add(unreadLabel);
            rightPanel.add(badgePanel, BorderLayout.CENTER);

            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User value, int index,
                boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(value.getNickname());

            // Check if this is the group chat user
            if (value.getId() == -1L || value.getHead() == -1) {
                ImageIcon avatar = getCircleIcon("images/group_avatar.png", 48);
                avatarLabel.setIcon(avatar); // No status dot for group chat
            } else {
                // Create avatar with green dot for regular users
            ImageIcon avatar = getCircleIcon("images/" + value.getHead() + ".png", 48);
            avatarLabel.setIcon(addStatusDot(avatar));
            }

            // Update unread badge
            Long chatKey = (value.getId() == -1L) ? GROUP_CHAT_KEY : value.getId();
            Integer count = unreadCounts.getOrDefault(chatKey, 0);

            if (count > 0) {
                unreadLabel.setText(count > 99 ? "99+" : count.toString());
                unreadLabel.setVisible(true);
            } else {
                unreadLabel.setVisible(false);
            }

            if (isSelected) {
                setBackground(new Color(240, 242, 245));
            } else if (index == hoveredIndex) {
                setBackground(new Color(245, 246, 246));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }

        private ImageIcon addStatusDot(ImageIcon icon) {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            icon.paintIcon(null, g2, 0, 0);

            // Draw Green Dot
            g2.setColor(new Color(37, 211, 102)); // SpringChat Green
            g2.fillOval(w - 14, h - 14, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(w - 14, h - 14, 12, 12);

            g2.dispose();
            return new ImageIcon(bi);
        }
    }

    // ==================== Emoji Panel ====================
    
    private void showEmojiPanel(JButton source) {
        JPopupMenu emojiPopup = new JPopupMenu();
        emojiPopup.setLayout(new BorderLayout());
        
        JPanel emojiPanel = new JPanel(new GridLayout(6, 10, 5, 5));
        emojiPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        emojiPanel.setBackground(Color.WHITE);
        
        String[] emojis = {
                "\uD83D\uDE00", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE01", "\uD83D\uDE06",
                "\uD83D\uDE05", "\uD83D\uDE02", "\uD83E\uDD23", "\uD83D\uDE0A", "\uD83D\uDE07",
                "\uD83D\uDE42", "\uD83D\uDE43", "\uD83D\uDE09", "\uD83D\uDE0C", "\uD83D\uDE0D",
                "\uD83E\uDD70", "\uD83D\uDE18", "\uD83D\uDE17", "\uD83D\uDE19", "\uD83D\uDE1A",
                "\uD83D\uDE0B", "\uD83D\uDE1B", "\uD83D\uDE1D", "\uD83D\uDE1C", "\uD83E\uDD2A",
                "\uD83E\uDD28", "\uD83E\uDDD0", "\uD83E\uDD13", "\uD83D\uDE0E", "\uD83E\uDD73",
                "\uD83D\uDE0F", "\uD83D\uDE12", "\uD83D\uDE1E", "\uD83D\uDE14", "\uD83D\uDE1F",
                "\uD83D\uDE15", "\uD83D\uDE41", "\u2639\uFE0F", "\uD83D\uDE23", "\uD83D\uDE16",
                "\uD83D\uDE2B", "\uD83D\uDE29", "\uD83E\uDD7A", "\uD83D\uDE22", "\uD83D\uDE2D",
                "\uD83D\uDE24", "\uD83D\uDE20", "\uD83D\uDE21", "\uD83E\uDD2C", "\uD83E\uDD2F",
                "\uD83D\uDE33", "\uD83E\uDD75", "\uD83E\uDD76", "\uD83D\uDC4D", "\uD83D\uDC4E",
                "\uD83D\uDC4F", "\uD83D\uDE4F", "\u2764\uFE0F", "\uD83D\uDC95", "\uD83D\uDC96"
        }; // 馃榾馃槂馃槃馃榿馃槅馃槄馃槀馃ぃ馃槉馃槆馃檪馃檭馃槈馃槍馃槏馃グ馃槝馃槜馃槞馃槡馃構馃槢馃槤馃槣馃お馃え馃馃馃槑馃コ馃槒馃槖馃槥馃様馃槦馃槙馃檨鈽癸笍馃槪馃槚馃槴馃槱馃ズ馃槩馃槶馃槫馃槧馃槨馃が馃く馃槼馃サ馃ザ馃憤馃憥馃憦馃檹鉂わ笍馃挄馃挅
        
        for (String emoji : emojis) {
            JButton btn = new JButton(emoji);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                insertEmojiAtCursor(emoji);
                emojiPopup.setVisible(false);
            });
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(new Color(240, 242, 245));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    btn.setContentAreaFilled(false);
                }
            });
            emojiPanel.add(btn);
        }
        
        emojiPopup.add(emojiPanel);
        emojiPopup.show(source, 0, source.getHeight());
    }
    
    private void insertEmojiAtCursor(String emoji) {
        int pos = inputArea.getCaretPosition();
        try {
            inputArea.getDocument().insertString(pos, emoji, null);
        } catch (Exception ex) {
            inputArea.append(emoji);
        }
        inputArea.requestFocus();
    }
    
    // ==================== Search Functionality ====================
    
    private void toggleSearchPanel() {
        if (searchPanel != null && searchPanel.isVisible()) {
            searchPanel.setVisible(false);
            return;
        }
        
        if (searchPanel == null) {
            createSearchPanel();
        }
        searchPanel.setVisible(true);
    }
    
    private void createSearchPanel() {
        searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(new Color(255, 255, 200));
        searchPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchButtons.setOpaque(false);
        
        JButton prevBtn = new JButton("\u2191");
        prevBtn.addActionListener(e -> navigateSearch(-1));
        searchButtons.add(prevBtn);
        
        JButton nextBtn = new JButton("\u2193");
        nextBtn.addActionListener(e -> navigateSearch(1));
        searchButtons.add(nextBtn);
        
        JButton closeBtn = new JButton("\u2715");
        closeBtn.addActionListener(e -> toggleSearchPanel());
        searchButtons.add(closeBtn);
        
        searchPanel.add(searchButtons, BorderLayout.EAST);
        
        searchField.addActionListener(e -> performSearch(searchField.getText()));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                performSearch(searchField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                performSearch(searchField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                performSearch(searchField.getText());
            }
        });
        
        // Add to the message scroll pane parent
        Container parent = messageContainer.getParent().getParent().getParent();
        if (parent instanceof JPanel) {
            ((JPanel) parent).add(searchPanel, BorderLayout.NORTH);
        }
        searchPanel.setVisible(false);
    }
    
    private void performSearch(String keyword) {
        searchResults.clear();
        currentSearchIndex = -1;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        Long chatKey = isPrivateChat && currentChatUser != null ? currentChatUser.getId() : GROUP_CHAT_KEY;
        List<Message> messages = messageHistory.get(chatKey);
        
        if (messages != null) {
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getMessage().toLowerCase()
                        .contains(keyword.toLowerCase())) {
                    searchResults.add(i);
                }
            }
        }
        
        if (!searchResults.isEmpty()) {
            currentSearchIndex = 0;
            highlightSearchResult();
        }
    }
    
    private void navigateSearch(int direction) {
        if (searchResults.isEmpty())
            return;
        
        currentSearchIndex += direction;
        if (currentSearchIndex < 0) {
            currentSearchIndex = searchResults.size() - 1;
        } else if (currentSearchIndex >= searchResults.size()) {
            currentSearchIndex = 0;
        }
        
        highlightSearchResult();
    }
    
    private void highlightSearchResult() {
        // For simplicity, just refresh and scroll to the message
        // A full implementation would highlight the specific message
        refreshChat();
    }
    
    // ==================== Reply Functionality ====================
    
    private void setReplyMessage(Message msg) {
        replyToMessage = msg;
        showReplyPreview();
    }
    
    private void showReplyPreview() {
        if (replyPreviewPanel == null) {
            createReplyPreview();
        }
        
        if (replyToMessage != null) {
            JLabel replyLabel = (JLabel) replyPreviewPanel.getComponent(0);
            String preview = replyToMessage.getMessage();
            if (preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            replyLabel.setText("Reply to: " + replyToMessage.getFromUser().getNickname() +
                             " - " + preview);
            replyPreviewPanel.setVisible(true);
        }
    }
    
    private void createReplyPreview() {
        replyPreviewPanel = new JPanel(new BorderLayout());
        replyPreviewPanel.setBackground(new Color(240, 242, 245));
        replyPreviewPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel replyLabel = new JLabel();
        replyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        replyPreviewPanel.add(replyLabel, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("\u2715");
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.addActionListener(e -> {
            replyToMessage = null;
            replyPreviewPanel.setVisible(false);
        });
        replyPreviewPanel.add(closeBtn, BorderLayout.EAST);
        
        // Add to input panel parent
        Container parent = inputArea.getParent().getParent().getParent();
        if (parent instanceof JPanel) {
            ((JPanel) parent).add(replyPreviewPanel, BorderLayout.NORTH);
        }
        replyPreviewPanel.setVisible(false);
    }
    
    // ==================== Recall Functionality ====================
    
    private boolean withinRecallTime(Message msg) {
        long now = System.currentTimeMillis();
        long sentTime = msg.getSendTime().getTime();
        return (now - sentTime) < 2 * 60 * 1000; // 2 minutes
    }
    
    private void recallMessage(Message msg) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to recall this message?",
                "Recall Message",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Create recall message
            Message recallMsg = new Message();
            recallMsg.setFromUser(DataBuffer.currentUser);
            recallMsg.setToUser(msg.getToUser());
            recallMsg.setSendTime(new Date());
            recallMsg.setMessage("@@RECALL@@" + msg.getSendTime().getTime());
            
            Request request = new Request();
            request.setAction("chat");
            request.setAttribute("msg", recallMsg);
            
            try {
                ClientUtil.sendTextRequest2(request);
                
                // Remove from local history
                Long chatKey = determineChatKey(msg);
                List<Message> messages = messageHistory.get(chatKey);
                if (messages != null) {
                    messages.remove(msg);
                    MessageHistoryService.saveMessages(
                        DataBuffer.currentUser.getId(),
                        chatKey,
                            messages);
                }
                
                // Refresh display
                switchChatView(chatKey);
                
                // Show recall notice
                appendSystemMessage("You recalled a message");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                        "Recall failed",
                        "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class BubblePanel extends JPanel {
        private boolean isMe;

        public BubblePanel(boolean isMe) {
            this.isMe = isMe;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isMe ? OUTGOING_BUBBLE : INCOMING_BUBBLE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.setColor(new Color(0, 0, 0, 10)); // Subtle shadow
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
