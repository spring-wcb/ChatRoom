/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: ServerInfoFrame
 * Author:   ITryagain
 * Date:     2019/5/15 18:30
 * Description: 服务器信息窗体
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package server.ui;

import common.model.entity.User;
import server.DataBuffer;
import server.controller.RequestProcessor;
import server.model.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/**
 * 〈一句话功能简述〉<br>
 * 〈服务器信息窗体〉
 *
 * @author ITryagain
 * @create 2019/5/15
 * @since 1.0.0
 */

public class ServerInfoFrame extends JFrame {
    private static final long serialVersionUID = 6274443611957724780L;
    private JTextField jta_msg;
    private JTable onlineUserTable;
    private JTable registedUserTable;
    private static final Color TEAL_GREEN = new Color(0, 128, 105);
    private static final Color BG_COLOR = new Color(240, 242, 245);

    public ServerInfoFrame() {
        init();
        loadData();
        setVisible(true);
    }

    public void init() {
        this.setTitle("SpringChat Server Control");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(BG_COLOR);

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TEAL_GREEN);
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("SpringChat Server");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Time & Port
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);

        JLabel portLabel = new JLabel("Port: 8080", SwingConstants.RIGHT);
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        portLabel.setForeground(new Color(255, 255, 255, 200));
        infoPanel.add(portLabel);

        final JLabel timeLabel = new JLabel("", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(255, 255, 255, 200));
        new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            public void run() {
                timeLabel.setText(df.format(new Date()));
            }
        }, 0, 1000);
        infoPanel.add(timeLabel);

        headerPanel.add(infoPanel, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);

        // --- Main Content (Tabs) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(BG_COLOR);

        // Style tables
        onlineUserTable = createStyledTable(DataBuffer.onlineUserTableModel);
        registedUserTable = createStyledTable(DataBuffer.registedUserTableModel);

        // Add popup to online table
        onlineUserTable.setComponentPopupMenu(getTablePop());

        JScrollPane onlineScroll = new JScrollPane(onlineUserTable);
        onlineScroll.setBorder(BorderFactory.createEmptyBorder());
        onlineScroll.getViewport().setBackground(Color.WHITE);

        JScrollPane registeredScroll = new JScrollPane(registedUserTable);
        registeredScroll.setBorder(BorderFactory.createEmptyBorder());
        registeredScroll.getViewport().setBackground(Color.WHITE);

        tabbedPane.addTab("Online Users", onlineScroll);
        tabbedPane.addTab("Registered Users", registeredScroll);

        this.add(tabbedPane, BorderLayout.CENTER);

        // --- Footer (Control & Broadcast) ---
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new javax.swing.border.EmptyBorder(10, 20, 10, 20));

        // Broadcast
        JPanel broadcastPanel = new JPanel(new BorderLayout(10, 0));
        broadcastPanel.setOpaque(false);
        JLabel bcLabel = new JLabel("Broadcast:");
        bcLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        broadcastPanel.add(bcLabel, BorderLayout.WEST);

        jta_msg = new JTextField();
        jta_msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        broadcastPanel.add(jta_msg, BorderLayout.CENTER);

        JButton sendBtn = new JButton("Send");
        styleButton(sendBtn, TEAL_GREEN);
        sendBtn.addActionListener(e -> {
            try {
                sendAllMsg();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        broadcastPanel.add(sendBtn, BorderLayout.EAST);

        footerPanel.add(broadcastPanel, BorderLayout.CENTER);

        // Stop Server
        JButton exitBtn = new JButton("Stop Server");
        styleButton(exitBtn, new Color(220, 53, 69)); // Red
        exitBtn.addActionListener(e -> logout());

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightFooter.setOpaque(false);
        rightFooter.add(exitBtn);

        footerPanel.add(rightFooter, BorderLayout.EAST);

        this.add(footerPanel, BorderLayout.SOUTH);

        // Window Listener
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        // Enter key for broadcast
        jta_msg.addActionListener(e -> {
            try {
                sendAllMsg();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private JTable createStyledTable(javax.swing.table.TableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(230, 242, 241));
        table.setSelectionForeground(Color.BLACK);

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(new Color(80, 80, 80));
        header.setPreferredSize(new Dimension(0, 35));

        return table;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new javax.swing.border.EmptyBorder(8, 15, 8, 15));
    }

    private JPopupMenu getTablePop() {
        JPopupMenu pop = new JPopupMenu();
        JMenuItem mi_send = new JMenuItem("Send Message");
        mi_send.setActionCommand("send");
        JMenuItem mi_del = new JMenuItem("Kick User");
        mi_del.setActionCommand("del");

        ActionListener al = e -> popMenuAction(e.getActionCommand());

        mi_send.addActionListener(al);
        mi_del.addActionListener(al);
        pop.add(mi_send);
        pop.add(mi_del);
        return pop;
    }

    private void popMenuAction(String command) {
        final int selectIndex = onlineUserTable.getSelectedRow();
        if (selectIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
            return;
        }
        String usr_id = (String) onlineUserTable.getValueAt(selectIndex, 0);

        if (command.equals("del")) {
            try {
                RequestProcessor.remove(DataBuffer.onlineUsersMap.get(Long.valueOf(usr_id)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.equals("send")) {
            String msg = JOptionPane.showInputDialog(this, "Enter message to send:");
            if (msg != null && !msg.trim().isEmpty()) {
                try {
                    RequestProcessor.chat_sys(msg, DataBuffer.onlineUsersMap.get(Long.valueOf(usr_id)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void sendAllMsg() throws IOException {
        String msg = jta_msg.getText().trim();
        if (!msg.isEmpty()) {
            RequestProcessor.board(msg);
            jta_msg.setText("");
        }
    }

    private void loadData() {
        List<User> users = new UserService().loadAllUser();
        for (User user : users) {
            DataBuffer.registedUserTableModel.add(new String[] {
                    String.valueOf(user.getId()),
                    user.getAccount(),
                    user.getPassword(),
                    user.getNickname(),
                    String.valueOf(user.getSex())
            });
        }
    }

    private void logout() {
        int select = JOptionPane.showConfirmDialog(this,
                "Stop server and disconnect all clients?",
                "Stop Server",
                JOptionPane.YES_NO_OPTION);
        if (select == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }
}
