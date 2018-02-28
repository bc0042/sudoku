package com.tt.sudoku;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by BC on 1/31/18.
 */
public class Board extends JPanel {
    static int maxWidth = 800;
    static int startX = 5;
    static int startY = 5;
    static int rows = 9;
    static int cols = 9;
    static Cell[][] cells;
    static Color bg;

    public Board() {
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


    public int getCellWidth() {
        int width = this.getWidth();
        width = width > maxWidth ? maxWidth : width;
        width = (width - startX * 2) / rows;
        return width;
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
        width = width > maxWidth ? maxWidth : width;
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

    public void paintChain(List<LinkNode> list) {
        if (list.size() <= 2) return;
        Graphics g = getGraphics();
        g.setColor(Color.red);
        int cellWidth = getCellWidth();
        for (int i = 0; i < list.size(); i += 2) {
            LinkNode node1 = list.get(i);
            LinkNode node2 = list.get(i + 1);
            StrongLink link = new StrongLink(node1, node2);
            link.paintMe(g, cellWidth);
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

    public void printForcingChain(List<LinkedList<LinkNode>> forcingChain) {
        Graphics g = getGraphics();
        int cellWidth = getCellWidth();
        for (LinkedList<LinkNode> chain : forcingChain) {
            LinkedList<LinkNode> chain2 = new LinkedList<>(chain);
            LinkNode first = chain2.removeFirst();
            LinkNode last = chain2.removeLast();
            g.setColor(Color.green);
            first.paintMe(g, cellWidth);
            g.setColor(Color.blue);
            last.paintMe(g, cellWidth);
            paintChain(chain2);
        }
    }

    public static List<Cell> getCandidateCells() {
        List<Cell> list = new ArrayList<>();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.size() > 1) {
                    list.add(cell);
                }
            }
        }
        return list;
    }
}
