import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class HotelSystem {
    static final Color BG = new Color(18, 18, 28), CARD = new Color(30, 30, 46);
    static final Color PRIMARY = new Color(79, 172, 254), SECONDARY = new Color(108, 92, 231);
    static final Color ACCENT = new Color(0, 210, 167), DANGER = new Color(255, 71, 87);
    static final Color TEXT = new Color(255, 255, 255), SUCCESS = new Color(0, 210, 167);
    
    static class Room {
        int num; String type; double price; boolean avail = true;
        Room(int n, String t, double p) { num = n; type = t; price = p; }
        public String toString() { return "房间" + num + "-" + type + "-¥" + price + (avail ? "[可用]" : "[已订]"); }
    }
    
    static class Customer {
        String name, phone, email;
        Customer(String n, String p, String e) { name = n; phone = p; email = e; }
    }
    
    static class Reservation {
        int id; Room room; Customer cust; String in, out;
        Reservation(int i, Room r, Customer c, String in, String out) {
            id = i; room = r; cust = c; this.in = in; this.out = out;
        }
        double total() { 
            try { return room.price * Math.max(1, Integer.parseInt(out.split("-")[2]) - Integer.parseInt(in.split("-")[2])); } 
            catch (Exception e) { return room.price; }
        }
    }
    
    java.util.List<Room> rooms = new ArrayList<>();
    java.util.List<Reservation> resvs = new ArrayList<>();
    int nextId = 1;
    
    HotelSystem() {
        rooms.add(new Room(101, "单人间", 200)); rooms.add(new Room(102, "单人间", 200));
        rooms.add(new Room(201, "双人间", 350)); rooms.add(new Room(202, "双人间", 350));
        rooms.add(new Room(301, "豪华间", 500)); rooms.add(new Room(302, "豪华间", 500));
        rooms.add(new Room(401, "套房", 800)); rooms.add(new Room(402, "套房", 800));
    }
    
    void run() throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.invokeLater(this::createUI);
    }
    
    void createUI() {
        JFrame f = new JFrame("酒店预订管理系统");
        f.setSize(1200, 800);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(CARD);
        tabs.setForeground(TEXT);
        ((JPanel)f.getContentPane()).setBackground(BG);
        
        tabs.addTab("房间管理", roomPanel());
        tabs.addTab("预订列表", resvPanel());
        tabs.addTab("新建预订", newResvPanel());
        
        f.add(tabs);
        f.setVisible(true);
    }
    
    JPanel roomPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("酒店房间列表", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        p.add(title, BorderLayout.NORTH);
        
        String[] cols = {"房间号", "类型", "价格/晚", "状态"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m);
        styleTable(t);
        
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(PRIMARY));
        p.add(sp, BorderLayout.CENTER);
        
        JButton btn = styledBtn("刷新", PRIMARY);
        btn.addActionListener(e -> refreshRooms(m));
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setBackground(BG);
        bp.add(btn);
        p.add(bp, BorderLayout.SOUTH);
        
        refreshRooms(m);
        return p;
    }
    
    JPanel resvPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("当前预订列表", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(SECONDARY);
        p.add(title, BorderLayout.NORTH);
        
        String[] cols = {"预订 ID", "客户姓名", "房间号", "入住日期", "退房日期", "总金额"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m);
        styleTable(t);
        
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(CARD);
        sp.setBorder(BorderFactory.createLineBorder(SECONDARY));
        p.add(sp, BorderLayout.CENTER);
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bp.setBackground(BG);
        JButton b1 = styledBtn("刷新", ACCENT);
        b1.addActionListener(e -> refreshResvs(m));
        JButton b2 = styledBtn("取消预订", DANGER);
        b2.addActionListener(e -> cancelResv(t, m));
        JButton b3 = styledBtn("办理退房", SUCCESS);
        b3.addActionListener(e -> checkOut(t, m));
        bp.add(b1); bp.add(b2); bp.add(b3);
        p.add(bp, BorderLayout.SOUTH);
        
        refreshResvs(m);
        return p;
    }
    
    JPanel newResvPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("创建新预订", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; g.weightx = 1;
        p.add(title, g);
        
        JTextField name = field(), phone = field(), email = field(), in = field(), out = field();
        in.setText("2026-03-05"); out.setText("2026-03-07");
        
        g.gridwidth = 1; g.weightx = 0;
        g.gridy = 1; g.gridx = 0; p.add(lbl("客户姓名:"), g);
        g.gridx = 1; g.weightx = 1; p.add(name, g);
        
        g.gridy = 2; g.gridx = 0; g.weightx = 0; p.add(lbl("电话号码:"), g);
        g.gridx = 1; g.weightx = 1; p.add(phone, g);
        
        g.gridy = 3; g.gridx = 0; g.weightx = 0; p.add(lbl("电子邮箱:"), g);
        g.gridx = 1; g.weightx = 1; p.add(email, g);
        
        g.gridy = 4; g.gridx = 0; g.weightx = 0; p.add(lbl("选择房间:"), g);
        g.gridx = 1; g.weightx = 1;
        JComboBox<Room> cb = new JComboBox<>();
        cb.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cb.setBackground(CARD);
        cb.setForeground(TEXT);
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? new Color(45, 45, 65) : CARD);
                setForeground(TEXT);
                return this;
            }
        });
        updateCB(cb);
        p.add(cb, g);
        
        g.gridy = 5; g.gridx = 0; g.weightx = 0; p.add(lbl("入住日期:"), g);
        g.gridx = 1; g.weightx = 1; p.add(in, g);
        
        g.gridy = 6; g.gridx = 0; g.weightx = 0; p.add(lbl("退房日期:"), g);
        g.gridx = 1; g.weightx = 1; p.add(out, g);
        
        g.gridy = 7; g.gridx = 0; g.gridwidth = 2; g.weightx = 1;
        g.insets = new Insets(20, 10, 10, 10);
        JButton create = gradBtn("创建预订", PRIMARY, SECONDARY);
        create.setPreferredSize(new Dimension(140, 45));
        create.addActionListener(e -> createResv(name, phone, email, cb, in, out));
        JButton refresh = styledBtn("刷新房间列表", ACCENT);
        refresh.setPreferredSize(new Dimension(160, 45));
        refresh.addActionListener(e -> updateCB(cb));
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bp.setBackground(BG);
        bp.add(create); bp.add(refresh);
        p.add(bp, g);
        
        return p;
    }
    
    JTextField field() {
        JTextField f = new JTextField(25);
        f.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        f.setBackground(CARD);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PRIMARY), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }
    
    JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        l.setForeground(new Color(166, 166, 166));
        return l;
    }
    
    JButton styledBtn(String t, Color c) {
        JButton b = new JButton(t);
        b.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        b.setBackground(c);
        b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 40));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(c.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(c); }
        });
        return b;
    }
    
    JButton gradBtn(String t, Color c1, Color c2) {
        return new JButton(t) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2d.setColor(c2.darker());
                else if (getModel().isRollover()) g2d.setColor(c1.brighter());
                else g2d.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
    }
    
    void styleTable(JTable t) {
        t.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        t.setRowHeight(35);
        t.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        t.getTableHeader().setBackground(CARD);
        t.getTableHeader().setForeground(TEXT);
        t.getTableHeader().setBorder(null);
        t.setBackground(CARD);
        t.setForeground(TEXT);
        t.setGridColor(new Color(45, 45, 65));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(new Color(79, 172, 254, 50));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(CARD);
                    c.setForeground(TEXT);
                    if (column == 3) {
                        c.setForeground(value.toString().equals("可用") ? SUCCESS : new Color(255, 159, 67));
                    }
                }
                return c;
            }
        });
    }
    
    void refreshRooms(DefaultTableModel m) {
        m.setRowCount(0);
        for (Room r : rooms) m.addRow(new Object[]{r.num, r.type, "¥" + r.price, r.avail ? "可用" : "已预订"});
    }
    
    void refreshResvs(DefaultTableModel m) {
        m.setRowCount(0);
        for (Reservation r : resvs) m.addRow(new Object[]{r.id, r.cust.name, r.room.num, r.in, r.out, "¥" + r.total()});
    }
    
    void updateCB(JComboBox<Room> cb) {
        cb.removeAllItems();
        for (Room r : rooms) if (r.avail) cb.addItem(r);
    }
    
    void createResv(JTextField name, JTextField phone, JTextField email, JComboBox<Room> cb, JTextField in, JTextField out) {
        if (name.getText().trim().isEmpty() || phone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "请填写完整的客户信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Room r = (Room) cb.getSelectedItem();
        if (r == null) { JOptionPane.showMessageDialog(null, "没有可用房间", "错误", JOptionPane.ERROR_MESSAGE); return; }
        resvs.add(new Reservation(nextId++, r, new Customer(name.getText().trim(), phone.getText().trim(), email.getText().trim()), in.getText().trim(), out.getText().trim()));
        r.avail = false;
        JOptionPane.showMessageDialog(null, "预订成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        name.setText(""); phone.setText(""); email.setText(""); in.setText("2026-03-05"); out.setText("2026-03-07");
        updateCB(cb);
    }
    
    void cancelResv(JTable t, DefaultTableModel m) {
        int row = t.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(null, "请选择要取消的预订", "提示", JOptionPane.WARNING_MESSAGE); return; }
        int id = (Integer) m.getValueAt(row, 0);
        for (int i = 0; i < resvs.size(); i++) {
            if (resvs.get(i).id == id) { resvs.get(i).room.avail = true; resvs.remove(i); break; }
        }
        refreshResvs(m);
        JOptionPane.showMessageDialog(null, "预订已取消", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
    
    void checkOut(JTable t, DefaultTableModel m) {
        int row = t.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(null, "请选择要办理退房的预订", "提示", JOptionPane.WARNING_MESSAGE); return; }
        int id = (Integer) m.getValueAt(row, 0);
        for (int i = 0; i < resvs.size(); i++) {
            if (resvs.get(i).id == id) { 
                int confirm = JOptionPane.showConfirmDialog(null, "确定要办理退房吗？\n总金额：¥" + resvs.get(i).total(), "确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    resvs.get(i).room.avail = true;
                    resvs.remove(i);
                    refreshResvs(m);
                    JOptionPane.showMessageDialog(null, "退房成功！感谢光临!", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            }
        }
    }
    
    public static void main(String[] args) throws Exception { new HotelSystem().run(); }
}
