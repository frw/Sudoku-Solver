import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Frederick
 * Date: 6/30/12
 * Time: 5:41 PM
 */
public class SudokuSolver
        extends JApplet {

    public static final int NUM_ROWS = 9 * 9 * 9;
    public static final int NUM_COLS = 9 * 9 * 4;

    public static final int GRID_OFFSET = 0;
    public static final int ROW_OFFSET = 81;
    public static final int COLUMN_OFFSET = 162;
    public static final int BOX_OFFSET = 243;

    private final boolean[][] matrix;

    private Grid grid;

    public SudokuSolver() {
        matrix = new boolean[NUM_COLS][NUM_ROWS];
        for (int v = 0; v < 9; v++) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int row = v * 81 + r * 9 + c;
                    matrix[GRID_OFFSET + r * 9 + c][row] = true;
                    matrix[ROW_OFFSET + r * 9 + v][row] = true;
                    matrix[COLUMN_OFFSET + c * 9 + v][row] = true;
                    matrix[BOX_OFFSET + (r / 3 * 3 + c / 3) * 9 + v][row] = true;
                }
            }
        }
    }

    @Override
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JLabel label = new JLabel("<html>Right-click the target cell or use the keyboard to input a number</html>",
                SwingConstants.CENTER);
        contentPane.add(label, BorderLayout.NORTH);

        grid = new Grid();
        contentPane.add(grid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grid.solution = solve(grid.values);
                grid.invalid = grid.solution == null;
                grid.repaint();
            }
        });
        buttonPanel.add(solveButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int[] row : grid.values) {
                    Arrays.fill(row, 0);
                }
                grid.solution = null;
                grid.invalid = false;
                grid.repaint();
            }
        });
        buttonPanel.add(resetButton);

        addKeyListener(grid);
        setFocusable(true);
    }

    public int[][] solve(int[][] values) {
        Set<Integer> columns = new HashSet<Integer>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = values[r][c] - 1;
                if (v != -1) {
                    int[] cols = new int[4];
                    cols[0] = GRID_OFFSET + r * 9 + c;
                    cols[1] = ROW_OFFSET + r * 9 + v;
                    cols[2] = COLUMN_OFFSET + c * 9 + v;
                    cols[3] = BOX_OFFSET + (r / 3 * 3 + c / 3) * 9 + v;
                    for (int i : cols) {
                        if (!columns.add(i)) {
                            return null;
                        }

                    }
                }
            }
        }
        DancingLinks dlx = new DancingLinks(matrix, NUM_COLS, NUM_ROWS);
        List<Integer> s = dlx.solve(columns);
        if (s != null) {
            int[][] solution = new int[9][9];
            for (int i : s) {
                int v = i / 81;
                i -= v * 81;
                int r = i / 9;
                i -= r * 9;
                int c = i;
                solution[r][c] = v + 1;
            }
            return solution;
        } else {
            return null;
        }
    }

    private class Grid
            extends JPanel
            implements MouseListener, KeyListener {

        private final Stroke stroke4 = new BasicStroke(4);
        private final Stroke stroke8 = new BasicStroke(8);
        private final Font font = new Font("Arial", Font.BOLD, 30);

        private final PopupMenu pm = new PopupMenu(this);

        private final int[][] values = new int[9][9];
        private int[][] solution = null;
        private boolean invalid = false;
        private int sR = -1, sC = -1;

        public Grid() {
            addMouseListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int oX = (getWidth() - 450) / 2;
            int oY = (getHeight() - 450) / 2;
            Point p = e.getPoint();
            int x = p.x - oX;
            int y = p.y - oY;
            if (x >= 0 && y >= 0 && x <= 449 && y <= 449) {
                setSelectedCell(y / 50, x / 50);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    pm.show(this, p.x, p.y);
                }
            } else {
                clearSelection();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (isCellSelected()) {
                int kc = e.getKeyCode();
                switch (kc) {
                    case KeyEvent.VK_1:
                    case KeyEvent.VK_2:
                    case KeyEvent.VK_3:
                    case KeyEvent.VK_4:
                    case KeyEvent.VK_5:
                    case KeyEvent.VK_6:
                    case KeyEvent.VK_7:
                    case KeyEvent.VK_8:
                    case KeyEvent.VK_9:
                        input(kc - KeyEvent.VK_0);
                        break;

                    case KeyEvent.VK_NUMPAD1:
                    case KeyEvent.VK_NUMPAD2:
                    case KeyEvent.VK_NUMPAD3:
                    case KeyEvent.VK_NUMPAD4:
                    case KeyEvent.VK_NUMPAD5:
                    case KeyEvent.VK_NUMPAD6:
                    case KeyEvent.VK_NUMPAD7:
                    case KeyEvent.VK_NUMPAD8:
                    case KeyEvent.VK_NUMPAD9:
                        input(kc - KeyEvent.VK_NUMPAD0);
                        break;

                    case KeyEvent.VK_DELETE:
                        input(0);
                        break;

                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_KP_UP:
                        setSelectedCell((sR + 8) % 9, sC);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_KP_DOWN:
                        setSelectedCell((sR + 1) % 9, sC);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_KP_LEFT:
                        setSelectedCell(sR, (sC + 8) % 9);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_RIGHT:
                        setSelectedCell(sR, (sC + 1) % 9);
                        break;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        public void clearSelection() {
            sR = -1;
            sC = -1;
            repaint();
        }

        public void setSelectedCell(int r,
                                    int c) {
            sR = r;
            sC = c;
            repaint();
        }

        public boolean isCellSelected() {
            return sR != -1 && sC != -1;
        }

        public void input(int v) {
            if (sR != -1 && sC != -1) {
                values[sR][sC] = v;
            }
            solution = null;
            invalid = false;
            repaint();
        }

        @Override
        public void paintComponent(Graphics g1) {
            Graphics2D g = (Graphics2D) g1;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Rectangle bounds = g.getClipBounds();

            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

            int oX = (bounds.width - 450) / 2;
            int oY = (bounds.height - 450) / 2;

            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (sR == r && sC == c) {
                        int sX = c * 50;
                        int sY = r * 50;
                        g.setColor(new Color(0, 255, 0, 100));
                        g.fillRect(oX + sX, oY + sY, 50, 50);
                    }
                    int value = values[r][c];
                    if (value != 0) {
                        String s = Integer.toString(value);
                        int x = c * 50 + (50 - fm.charWidth('0' + value)) / 2;
                        int y = r * 50 + 35;
                        g.setColor(invalid ? Color.RED : Color.BLACK);
                        g.drawString(s, oX + x, oY + y);
                    } else if (solution != null) {
                        String s = Integer.toString(solution[r][c]);
                        int x = c * 50 + (50 - fm.charWidth('0' + value)) / 2;
                        int y = r * 50 + 35;
                        g.setColor(Color.BLUE);
                        g.drawString(s, oX + x, oY + y);
                    }
                }
            }

            g.setColor(Color.BLACK);
            for (int i = 1; i < 9; i++) {
                int j = i * 50;
                if (i % 3 == 0) {
                    g.setStroke(stroke8);
                    j -= 4;
                } else {
                    g.setStroke(stroke4);
                    j -= 2;
                }
                g.drawLine(oX + 0, oY + j, oX + 450, oY + j);
                g.drawLine(oX + j, oY + 0, oX + j, oY + 450);
            }
            g.drawRect(oX + 0, oY + 0, 449, 449);
        }
    }

    private class PopupMenu
            extends JPopupMenu
            implements PopupMenuListener, ActionListener {

        private final Grid grid;

        public PopupMenu(Grid grid) {
            this.grid = grid;
            addPopupMenuListener(this);
            for (int i = 0; i <= 9; i++) {
                JMenuItem mi = new JMenuItem(i == 0 ? "" : Integer.toString(i));
                mi.addActionListener(this);
                add(mi);
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            repaint();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            int v = s.isEmpty() ? 0 : Integer.parseInt(s);
            grid.input(v);
        }
    }
}
