import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

public class SnakeLadder extends JFrame {

    private static final int BOARD_SIZE = 100;
    private static final int ROWS = 10;

    private Board board;
    private BoardPanel boardPanel;
    private DicePanel dicePanel;

    private int playerPos = 1;
    private int moveCount = 0;
    private boolean gameWon = false;
    private boolean animating = false;
    private final Random rand = new Random();

    private JLabel posLabel, moveLabel, statusLabel, optimalLabel;
    private JTextArea logArea;
    private JButton rollBtn, resetBtn, bfsBtn;
    private int optimalThrows = -1;
    private BFSSolver.Result bfsResult;

    public SnakeLadder() {
        super("Snake and Ladder");
        board = new Board();
        boardPanel = new BoardPanel();
        dicePanel = new DicePanel();

        computeOptimal();

        setLayout(new BorderLayout(8, 8));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().setBackground(new Color(240, 235, 220));

        buildUI();
        setupKeys();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setVisible(true);
        log("Game started at cell 1. Roll the dice!");
    }

    void computeOptimal() {
        bfsResult = BFSSolver.solve(board, 1, BOARD_SIZE);
        if (bfsResult != null) optimalThrows = bfsResult.throws_;
    }

    // ============================================================
    // BOARD MODEL
    // ============================================================

    static class Board {
        final Map<Integer, Integer> snakes = new HashMap<>();
        final Map<Integer, Integer> ladders = new HashMap<>();
        final int[] row = new int[BOARD_SIZE + 1];
        final int[] col = new int[BOARD_SIZE + 1];

        Board() {
            snakes.put(16, 6);  snakes.put(46, 25); snakes.put(49, 11);
            snakes.put(62, 19); snakes.put(64, 60); snakes.put(74, 53);
            snakes.put(89, 68); snakes.put(92, 88); snakes.put(95, 75);
            snakes.put(99, 80);

            ladders.put(2, 38);  ladders.put(7, 14);  ladders.put(8, 31);
            ladders.put(15, 26); ladders.put(21, 42); ladders.put(28, 84);
            ladders.put(36, 44); ladders.put(51, 67); ladders.put(71, 91);
            ladders.put(78, 98); ladders.put(87, 94);

            for (int c = 1; c <= BOARD_SIZE; c++) {
                int r = (c - 1) / ROWS;
                row[c] = r;
                col[c] = r % 2 == 0 ? (c - 1) % ROWS : ROWS - 1 - (c - 1) % ROWS;
            }
        }

        int dest(int cell) {
            if (ladders.containsKey(cell)) return ladders.get(cell);
            if (snakes.containsKey(cell)) return snakes.get(cell);
            return cell;
        }

        boolean isLadder(int c) { return ladders.containsKey(c); }
        boolean isSnake(int c) { return snakes.containsKey(c); }
    }

    // ============================================================
    // BFS SOLVER
    // ============================================================

    static class BFSSolver {
        static class Result {
            int throws_;
            java.util.List<Integer> path;
            java.util.List<Integer> rolls;
            Result(int t, java.util.List<Integer> p, java.util.List<Integer> r) {
                throws_ = t; path = p; rolls = r;
            }
        }

        static Result solve(Board board, int start, int end) {
            boolean[] seen = new boolean[BOARD_SIZE + 1];
            int[] dist = new int[BOARD_SIZE + 1];
            int[] parent = new int[BOARD_SIZE + 1];
            int[] roll = new int[BOARD_SIZE + 1];
            Arrays.fill(dist, -1);
            Queue<Integer> q = new LinkedList<>();
            seen[start] = true; dist[start] = 0; q.offer(start);

            while (!q.isEmpty()) {
                int cur = q.poll();
                if (cur == end) break;
                for (int d = 1; d <= 6; d++) {
                    int nxt = cur + d;
                    if (nxt > BOARD_SIZE) continue;
                    nxt = board.dest(nxt);
                    if (!seen[nxt]) {
                        seen[nxt] = true; dist[nxt] = dist[cur] + 1;
                        parent[nxt] = cur; roll[nxt] = d;
                        q.offer(nxt);
                    }
                }
            }

            java.util.List<Integer> path = new ArrayList<>(), rolls = new ArrayList<>();
            if (dist[end] == -1) return new Result(-1, path, rolls);
            int c = end;
            while (c != start) { path.add(c); rolls.add(roll[c]); c = parent[c]; }
            path.add(start); Collections.reverse(path); Collections.reverse(rolls);
            return new Result(dist[end], path, rolls);
        }
    }

    // ============================================================
    // DICE PANEL
    // ============================================================

    static class DicePanel extends JPanel {
        private int val = 1;
        void setVal(int v) { val = Math.max(1, Math.min(6, v)); repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = Math.min(getWidth(), getHeight()), pad = 8, rw = s - pad * 2, rh = s - pad * 2;
            g2.setColor(new Color(250, 250, 250));
            g2.fillRoundRect(pad, pad, rw, rh, 14, 14);
            g2.setColor(new Color(50, 50, 50));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(pad, pad, rw, rh, 14, 14);
            g2.setColor(new Color(30, 30, 30));
            int cx = s / 2, cy = s / 2, dr = Math.max(5, rw / 9), ox = rw / 4, oy = rh / 4;
            switch (val) {
                case 1: dot(g2, cx, cy, dr); break;
                case 2: dot(g2, cx - ox, cy + oy, dr); dot(g2, cx + ox, cy - oy, dr); break;
                case 3: dot(g2, cx - ox, cy + oy, dr); dot(g2, cx, cy, dr); dot(g2, cx + ox, cy - oy, dr); break;
                case 4: dot(g2, cx - ox, cy - oy, dr); dot(g2, cx + ox, cy - oy, dr);
                        dot(g2, cx - ox, cy + oy, dr); dot(g2, cx + ox, cy + oy, dr); break;
                case 5: dot(g2, cx - ox, cy - oy, dr); dot(g2, cx + ox, cy - oy, dr);
                        dot(g2, cx, cy, dr); dot(g2, cx - ox, cy + oy, dr); dot(g2, cx + ox, cy + oy, dr); break;
                case 6: dot(g2, cx - ox, cy - oy, dr); dot(g2, cx + ox, cy - oy, dr);
                        dot(g2, cx - ox, cy, dr); dot(g2, cx + ox, cy, dr);
                        dot(g2, cx - ox, cy + oy, dr); dot(g2, cx + ox, cy + oy, dr); break;
            }
        }
        private void dot(Graphics2D g, int x, int y, int r) { g.fillOval(x - r, y - r, r * 2, r * 2); }
        public Dimension getPreferredSize() { return new Dimension(90, 90); }
    }

    // ============================================================
    // BOARD PANEL
    // ============================================================

    class BoardPanel extends JPanel {
        private Set<Integer> bfsPathCells = new HashSet<>();

        BoardPanel() {
            setPreferredSize(new Dimension(580, 580));
            setBackground(new Color(230, 220, 200));
        }

        void setPlayer(int pos) { playerPos = pos; repaint(); }
        void showBFSPath(java.util.List<Integer> path) {
            bfsPathCells.clear();
            if (path != null) bfsPathCells.addAll(path);
            repaint();
        }
        void clearBFSPath() { bfsPathCells.clear(); repaint(); }

        private int pad() { return 35; }
        private int cellSize() {
            int sz = Math.min(getWidth(), getHeight()) - pad() * 2;
            return Math.max(1, sz / ROWS);
        }

        private Point cellPt(int cell) {
            int cs = cellSize(), p = pad();
            int r = (ROWS - 1) - board.row[cell];
            return new Point(p + board.col[cell] * cs + cs / 2, p + r * cs + cs / 2);
        }

        private Rectangle cellRect(int cell) {
            int cs = cellSize(), p = pad();
            int r = (ROWS - 1) - board.row[cell];
            return new Rectangle(p + board.col[cell] * cs, p + r * cs, cs, cs);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int cs = cellSize(), p = pad();

            drawCells(g2, cs, p);
            drawCellHighlights(g2, cs, p);
            drawBFSPath(g2, cs, p);
            drawNumbers(g2, cs, p);
            drawLadders(g2, cs, p);
            drawSnakes(g2, cs, p);
            drawBorder(g2, cs, p);
            drawPlayer(g2, cs, p);
        }

        private void drawCells(Graphics2D g2, int cs, int p) {
            Color c1 = new Color(255, 240, 200), c2 = new Color(230, 210, 170);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < ROWS; c++) {
                    g2.setColor((r + c) % 2 == 0 ? c1 : c2);
                    g2.fillRect(p + c * cs, p + r * cs, cs, cs);
                }
            }
        }

        private void drawCellHighlights(Graphics2D g2, int cs, int p) {
            for (int c = 1; c <= BOARD_SIZE; c++) {
                Rectangle r = cellRect(c);
                if (board.isSnake(c)) { g2.setColor(new Color(220, 80, 80, 40)); g2.fill(r); }
                if (board.isLadder(c)) { g2.setColor(new Color(60, 160, 60, 40)); g2.fill(r); }
            }
        }

        private void drawBFSPath(Graphics2D g2, int cs, int p) {
            if (bfsPathCells.isEmpty()) return;
            for (int cell : bfsPathCells) {
                if (cell == playerPos) continue;
                Rectangle r = cellRect(cell);
                g2.setColor(new Color(255, 215, 0, 130));
                g2.fill(r);
                g2.setColor(new Color(255, 140, 0));
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(r.x, r.y, r.width, r.height);
            }
        }

        private void drawNumbers(Graphics2D g2, int cs, int p) {
            int fs = Math.max(11, cs / 4);
            g2.setFont(new Font("Segoe UI", Font.BOLD, fs));
            g2.setColor(new Color(40, 40, 40));
            FontMetrics fm = g2.getFontMetrics();
            for (int c = 1; c <= BOARD_SIZE; c++) {
                Point pt = cellPt(c);
                String s = String.valueOf(c);
                g2.drawString(s, pt.x - fm.stringWidth(s) / 2, pt.y + fm.getAscent() / 3);
            }
        }

        private void drawLadders(Graphics2D g2, int cs, int p) {
            g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Map.Entry<Integer, Integer> e : board.ladders.entrySet()) {
                Point p1 = cellPt(e.getKey()), p2 = cellPt(e.getValue());
                double dx = p2.x - p1.x, dy = p2.y - p1.y, len = Math.sqrt(dx * dx + dy * dy);
                if (len < 1) continue;
                double ox = -dy / len * 7, oy = dx / len * 7;
                int lx1 = (int)(p1.x - ox), ly1 = (int)(p1.y - oy);
                int lx2 = (int)(p2.x - ox), ly2 = (int)(p2.y - oy);
                int rx1 = (int)(p1.x + ox), ry1 = (int)(p1.y + oy);
                int rx2 = (int)(p2.x + ox), ry2 = (int)(p2.y + oy);

                g2.setColor(new Color(139, 90, 43));
                g2.draw(new Line2D.Float(lx1, ly1, lx2, ly2));
                g2.draw(new Line2D.Float(rx1, ry1, rx2, ry2));

                g2.setStroke(new BasicStroke(3));
                g2.setColor(new Color(180, 120, 60));
                int rungs = 5;
                for (int i = 1; i <= rungs; i++) {
                    float t = i / (float)(rungs + 1);
                    g2.draw(new Line2D.Float(
                        lx1 + (lx2 - lx1) * t, ly1 + (ly2 - ly1) * t,
                        rx1 + (rx2 - rx1) * t, ry1 + (ry2 - ry1) * t));
                }
            }
        }

        private void drawSnakes(Graphics2D g2, int cs, int p) {
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Map.Entry<Integer, Integer> e : board.snakes.entrySet()) {
                Point p1 = cellPt(e.getKey()), p2 = cellPt(e.getValue());
                double dx = p2.x - p1.x, dy = p2.y - p1.y, len = Math.sqrt(dx * dx + dy * dy);
                if (len < 1) continue;
                double mx = (p1.x + p2.x) / 2.0, my = (p1.y + p2.y) / 2.0;
                double px = -dy / len * len * 0.35, py = dx / len * len * 0.35;
                double ctrlX = mx + px, ctrlY = my + py;

                g2.setColor(new Color(50, 130, 50));
                QuadCurve2D curve = new QuadCurve2D.Float();
                curve.setCurve(p1.x, p1.y, (float)ctrlX, (float)ctrlY, p2.x, p2.y);
                g2.draw(curve);

                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(80, 180, 80));
                double tx = -dy / len * 3, ty = dx / len * 3;
                QuadCurve2D stripe = new QuadCurve2D.Float();
                stripe.setCurve(p1.x + (float)tx, p1.y + (float)ty,
                    (float)(ctrlX + tx), (float)(ctrlY + ty),
                    p2.x + (float)tx, p2.y + (float)ty);
                g2.draw(stripe);
                g2.setStroke(new BasicStroke(4));

                double angle = Math.atan2(p2.y - ctrlY, p2.x - ctrlX);
                int ss = 12;
                int[] xp = {p2.x, p2.x - (int)(ss * Math.cos(angle - 0.5)), p2.x - (int)(ss * Math.cos(angle + 0.5))};
                int[] yp = {p2.y, p2.y - (int)(ss * Math.sin(angle - 0.5)), p2.y - (int)(ss * Math.sin(angle + 0.5))};
                g2.setColor(new Color(50, 130, 50));
                g2.fillPolygon(xp, yp, 3);

                int eyeOff = 4;
                Point eye1 = new Point(
                    p1.x + (int)(eyeOff * Math.cos(angle + 0.8)),
                    p1.y + (int)(eyeOff * Math.sin(angle + 0.8)));
                Point eye2 = new Point(
                    p1.x + (int)(eyeOff * Math.cos(angle - 0.8)),
                    p1.y + (int)(eyeOff * Math.sin(angle - 0.8)));
                g2.setColor(Color.WHITE);
                g2.fillOval(eye1.x - 3, eye1.y - 3, 6, 6);
                g2.fillOval(eye2.x - 3, eye2.y - 3, 6, 6);
                g2.setColor(Color.BLACK);
                g2.fillOval(eye1.x - 1, eye1.y - 1, 3, 3);
                g2.fillOval(eye2.x - 1, eye2.y - 1, 3, 3);
            }
        }

        private void drawBorder(Graphics2D g2, int cs, int p) {
            g2.setColor(new Color(80, 50, 20));
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(p - 2, p - 2, ROWS * cs + 4, ROWS * cs + 4);
        }

        private void drawPlayer(Graphics2D g2, int cs, int p) {
            Point pt = cellPt(playerPos);
            int r = Math.max(16, cs / 3);

            g2.setColor(new Color(30, 100, 220, 60));
            g2.fillOval(pt.x - r - 4, pt.y - r - 4, r * 2 + 8, r * 2 + 8);

            RadialGradientPaint grad = new RadialGradientPaint(
                pt.x - r / 3, pt.y - r / 3, r,
                new float[]{0, 1}, new Color[]{new Color(100, 180, 255), new Color(20, 70, 180)});
            g2.setPaint(grad);
            g2.fillOval(pt.x - r, pt.y - r, r * 2, r * 2);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(pt.x - r, pt.y - r, r * 2, r * 2);

            g2.setFont(new Font("Segoe UI", Font.BOLD, r));
            FontMetrics fm = g2.getFontMetrics();
            String label = "P";
            g2.setColor(Color.WHITE);
            g2.drawString(label, pt.x - fm.stringWidth(label) / 2, pt.y + fm.getAscent() / 3);
        }
    }

    // ============================================================
    // UI BUILD
    // ============================================================

    private void buildUI() {
        add(boardPanel, BorderLayout.CENTER);

        JPanel side = new JPanel(new GridBagLayout());
        side.setPreferredSize(new Dimension(240, 0));
        side.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 5, 3, 5); g.gridx = 0;
        int gy = 0;

        JLabel title = new JLabel("Snake & Ladder");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.gridy = gy++; g.anchor = GridBagConstraints.CENTER;
        side.add(title, g);

        posLabel = new JLabel("Position: Cell 1");
        posLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        posLabel.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy = gy++;
        side.add(posLabel, g);

        dicePanel.setPreferredSize(new Dimension(80, 80));
        g.gridy = gy++;
        side.add(dicePanel, g);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(180, 60, 0));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy = gy++;
        side.add(statusLabel, g);

        moveLabel = new JLabel("Moves: 0");
        moveLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        moveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy = gy++;
        side.add(moveLabel, g);

        rollBtn = btn("Roll Dice", new Color(50, 160, 50), 16);
        rollBtn.addActionListener(e -> roll());
        g.gridy = gy++; side.add(rollBtn, g);

        resetBtn = new JButton("New Game");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> reset());
        g.gridy = gy++; side.add(resetBtn, g);

        bfsBtn = btn("BFS Solver", new Color(0, 100, 180), 14);
        bfsBtn.addActionListener(e -> showBFSDialog());
        g.gridy = gy++; side.add(bfsBtn, g);

        if (optimalThrows > 0) {
            optimalLabel = new JLabel("Optimal: " + optimalThrows + " throws");
            optimalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            optimalLabel.setForeground(new Color(0, 100, 0));
            optimalLabel.setHorizontalAlignment(SwingConstants.CENTER);
            g.gridy = gy++; g.insets = new Insets(12, 5, 3, 5);
            side.add(optimalLabel, g);
            g.insets = new Insets(3, 5, 3, 5);
        }

        logArea = new JTextArea(10, 22); logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setLineWrap(true); logArea.setWrapStyleWord(true);
        JScrollPane ls = new JScrollPane(logArea);
        ls.setBorder(BorderFactory.createTitledBorder("Log"));
        g.gridy = gy++; g.weightx = 1; g.weighty = 0.6; g.fill = GridBagConstraints.BOTH;
        side.add(ls, g);

        add(side, BorderLayout.EAST);
    }

    private JButton btn(String t, Color bg, int fs) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, fs));
        b.setBackground(bg); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        return b;
    }

    private void setupKeys() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke("ctrl R"), "roll");
        am.put("roll", new AbstractAction() { public void actionPerformed(ActionEvent e) { roll(); } });
        im.put(KeyStroke.getKeyStroke("ctrl N"), "new");
        am.put("new", new AbstractAction() { public void actionPerformed(ActionEvent e) { reset(); } });
        im.put(KeyStroke.getKeyStroke("F1"), "help");
        am.put("help", new AbstractAction() { public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(SnakeLadder.this,
                "Ctrl+R  Roll Dice\nCtrl+N  New Game\nF1      Help",
                "Keyboard Shortcuts", JOptionPane.INFORMATION_MESSAGE);
        }});
    }

    // ============================================================
    // BFS DIALOG
    // ============================================================

    private void showBFSDialog() {
        if (bfsResult == null || bfsResult.path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No solution found from cell 1 to 100.", "BFS Solver", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "BFS Optimal Solution", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        top.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JLabel title = new JLabel("Minimum throws: " + bfsResult.throws_, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0, 100, 0));
        top.add(title);

        StringBuilder pathStr = new StringBuilder("Path: ");
        for (int i = 0; i < bfsResult.path.size(); i++) {
            int cell = bfsResult.path.get(i);
            if (i == 0) {
                pathStr.append(cell);
            } else {
                int dice = bfsResult.rolls.get(i - 1);
                int intermediate = bfsResult.path.get(i - 1) + dice;
                pathStr.append(" [").append(dice).append("] ");
                if (intermediate != cell) {
                    pathStr.append(intermediate);
                    if (board.isLadder(intermediate)) pathStr.append("(L)");
                    else if (board.isSnake(intermediate)) pathStr.append("(S)");
                    pathStr.append(" \u2192 ").append(cell);
                } else {
                    pathStr.append(cell);
                }
            }
        }
        JLabel pathLabel = new JLabel(pathStr.toString());
        pathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        top.add(pathLabel);

        dialog.add(top, BorderLayout.NORTH);

        JTextArea detailArea = new JTextArea(10, 35);
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        detailArea.setBackground(new Color(250, 250, 245));

        StringBuilder sb = new StringBuilder("Step Details:\n\n");
        for (int i = 1; i < bfsResult.path.size(); i++) {
            int from = bfsResult.path.get(i - 1);
            int to = bfsResult.path.get(i);
            int dice = bfsResult.rolls.get(i - 1);
            int raw = from + dice;

            sb.append(String.format("%2d. ", i));
            sb.append("Roll ").append(dice).append(": ").append(from).append(" \u2192 ");
            if (raw != to) {
                sb.append(raw);
                if (board.isLadder(raw)) sb.append(" [Ladder to ").append(to).append("]");
                else if (board.isSnake(raw)) sb.append(" [Snake to ").append(to).append("]");
            } else {
                sb.append(to);
            }
            sb.append("\n");
        }
        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(detailArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));

        JButton highlightBtn = new JButton("Highlight on Board");
        highlightBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        highlightBtn.setBackground(new Color(0, 100, 180));
        highlightBtn.setForeground(Color.WHITE);
        highlightBtn.setFocusPainted(false);
        highlightBtn.addActionListener(e -> {
            boardPanel.showBFSPath(bfsResult.path);
            log("BFS path highlighted on board");
        });
        btnPanel.add(highlightBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ============================================================
    // GAME LOGIC
    // ============================================================

    private void roll() {
        if (gameWon || animating) return;
        animating = true; rollBtn.setEnabled(false);
        animateDice(rand.nextInt(6) + 1);
    }

    private void animateDice(int finalVal) {
        statusLabel.setText("Rolling...");
        javax.swing.Timer t = new javax.swing.Timer(70, null);
        int[] cnt = {0};
        t.addActionListener(e -> {
            cnt[0]++; dicePanel.setVal(rand.nextInt(6) + 1);
            if (cnt[0] >= 10) {
                t.stop(); dicePanel.setVal(finalVal);
                statusLabel.setText("Rolled " + finalVal + "!");
                log("Rolled " + finalVal);
                int land = playerPos + finalVal;
                if (land > BOARD_SIZE) {
                    statusLabel.setText("Need exact roll for 100!");
                    log("Rolled " + finalVal + " \u2014 need exact. Stay at " + playerPos);
                    finishTurn(); return;
                }
                animateMove(finalVal, land);
            }
        }); t.start();
    }

    private void animateMove(int roll, int land) {
        statusLabel.setText("Moving...");
        int[] step = {0};
        javax.swing.Timer t = new javax.swing.Timer(150, null);
        t.addActionListener(e -> {
            step[0]++;
            int show = playerPos + step[0];
            boardPanel.setPlayer(show);
            posLabel.setText("Position: Cell " + show);
            if (step[0] >= roll) {
                t.stop();
                int dest = board.dest(land);
                if (dest != land) animateSL(land, dest);
                else {
                    playerPos = land; moveCount++; moveLabel.setText("Moves: " + moveCount);
                    boardPanel.setPlayer(playerPos);
                    log("Moved to cell " + playerPos + (board.isLadder(land) ? " (Ladder!)" : board.isSnake(land) ? " (Snake!)" : ""));
                    finishTurn();
                }
            }
        }); t.start();
    }

    private void animateSL(int land, int dest) {
        boolean ladder = board.isLadder(land);
        statusLabel.setText((ladder ? "Ladder!" : "Snake!") + " " + land + " \u2192 " + dest);
        log((ladder ? "Ladder" : "Snake") + " " + land + " -> " + dest);
        boardPanel.setPlayer(land);
        javax.swing.Timer t = new javax.swing.Timer(600, null);
        t.addActionListener(e -> { t.stop();
            playerPos = dest; moveCount++; moveLabel.setText("Moves: " + moveCount);
            boardPanel.setPlayer(dest); posLabel.setText("Position: Cell " + dest);
            log((ladder ? "Climbed" : "Slid") + " to " + dest); finishTurn(); });
        t.setRepeats(false); t.start();
    }

    private void finishTurn() {
        if (playerPos == BOARD_SIZE) {
            gameWon = true; statusLabel.setText("You Win!");
            log("WIN! Reached 100 in " + moveCount + " moves!");
            rollBtn.setEnabled(false); animating = false;
            String msg = "Congratulations! You reached cell 100 in " + moveCount + " moves!";
            if (optimalThrows > 0) msg += "\n\nMinimum possible: " + optimalThrows + " throws";
            if (optimalThrows > 0 && moveCount == optimalThrows) msg += "\n\nPerfect game! You found the optimal path!";
            JOptionPane.showMessageDialog(this, msg, "You Win!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        animating = false; rollBtn.setEnabled(true); statusLabel.setText(" ");
    }

    private void reset() {
        playerPos = 1; moveCount = 0; gameWon = false; animating = false;
        dicePanel.setVal(1);
        boardPanel.clearBFSPath();
        boardPanel.setPlayer(1);
        posLabel.setText("Position: Cell 1"); moveLabel.setText("Moves: 0");
        statusLabel.setText(" "); rollBtn.setEnabled(true);
        logArea.setText("");
        log("New game started!");
    }

    private void log(String m) { logArea.append(m + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeLadder());
    }
}
