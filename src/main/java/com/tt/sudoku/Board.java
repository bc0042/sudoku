package com.tt.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by BC on 1/31/18.
 */
public class Board extends JPanel {
    static int startX = 5;
    static int startY = 5;
    static int rows = 9;
    static int cols = 9;
    static Cell[][] cells;
    static Color bg;

    public Board() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    handlePaste();
                }
                if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    handleCopy();
                }
            }
        });
        setFocusable(true);
        initCells();
    }

    private void initCells() {
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

    private void handleCopy() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                List<Integer> candidates = cells[i][j].candidates;
                if (candidates.size() == 1) {
                    sb.append(candidates.get(0));
                } else {
                    sb.append(".");
                }
            }
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection sel = new StringSelection(sb.toString());
        clipboard.setContents(sel, sel);
    }

    private void handlePaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            String trim = data.toString().trim();
            Debug.println(trim);

            int length = trim.length();
            if (length == rows * cols) {
                parseData(trim);
            } else {
                Debug.println("data err");
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseFromFile() {
        Debug.println("parseFromFile..");
        String file = "data.txt";
        InputStream is = Board.class.getClassLoader().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> list = new ArrayList<>();
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) break;

                if (line.startsWith("|")) {
                    String[] split = line.split(" ");
                    for (String s : split) {
                        if (s.matches("\\d+")) {
                            list.add(s);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (list.size() == 81) {
            for (int i = 0; i < list.size(); i++) {
                int r = i / rows;
                int c = i % cols;
                Cell cell = new Cell(r, c);
                cell.candidates.clear();
                String s = list.get(i);
                for (int j = 0; j < s.length(); j++) {
                    cell.candidates.add(Integer.parseInt("" + s.charAt(j)));
                }
                cells[r][c] = cell;
            }
        }
        repaint();
    }

    public void parseData(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int r = i / cols;
            int c = i % cols;
            if ('.' != ch) {
                cells[r][c].candidates.clear();
                cells[r][c].candidates.add(Integer.parseInt(ch + ""));
            } else {
                cells[r][c] = new Cell(r, c);
            }
        }
        Eliminator.run();
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (bg == null) {
            bg = this.getBackground();
        }
        paintBoard(g);
        paintNumbers(g);
        Debug.println("paint..");
    }

    private void paintNumbers(Graphics g) {
        int cellWidth = getCellWidth();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell c = cells[i][j];
//                g2.fillRect(c.x, c.y, 5, 5);
                c.paintCandidates(g, cellWidth);
            }
        }
    }

    private void paintBoard(Graphics g) {
        int width = this.getWidth() - startX * 2;
        int cellWidth = getCellWidth();
        g.drawRect(startX, startY, width, width);

        for (int i = 1; i < rows; i++) {
            if (i % 3 == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.lightGray);
            }
            g.drawLine(startX, startY + cellWidth * i, startX + width, startY + cellWidth * i);
        }
        for (int i = 1; i < cols; i++) {
            if (i % 3 == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.lightGray);
            }
            g.drawLine(startX + cellWidth * i, startY, startX + cellWidth * i, startY + width);
        }
    }

    public int getCellWidth() {
        return (this.getWidth() - startX * 2) / rows;
    }

    public void paintChain(List<Cell> list) {
        if (list.size() <= 2) return;
        Graphics g = getGraphics();
        g.setColor(Color.red);
        int cellWidth = getCellWidth();
        int size = (int) (cellWidth / Cell.factor);
        for (int i = 0; i < list.size(); i += 2) {
            Cell c1 = list.get(i);
            Cell c2 = list.get(i + 1);
            Point p1 = c1.getCandidatePoint(c1.num, cellWidth);
            Point p2 = c2.getCandidatePoint(c2.num, cellWidth);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            g.drawArc(p1.x, p1.y - size, size, size, 0, 360);
            g.drawArc(p2.x, p2.y - size, size, size, 0, 360);
        }
    }

    public void paintExcludeList(List<Cell> excludeList) {
        Graphics g = getGraphics();
        g.setColor(Color.blue);
        int cellWidth = getCellWidth();
        int size = (int) (cellWidth / Cell.factor);
        for (Cell cell : excludeList) {
            for (Integer e : cell.excludes) {
                Point p1 = cell.getCandidatePoint(e, cellWidth);
                g.drawArc(p1.x, p1.y - size, size, size, 0, 360);
            }
        }
    }
}
