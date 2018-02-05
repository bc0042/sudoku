package com.tt.sudoku;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by BC on 1/31/18.
 */
public class Cell {
    int r;
    int c;
    int x;
    int y;
    int linkNum;
    List<Integer> candidates;
    List<Integer> excludes;

    static final double factor = 3.5;
    static final double factor2 = 1.5;
    static final int maxNum = 9;

    public Cell(int r, int c) {
        this.r = r;
        this.c = c;
        candidates = new ArrayList<>();
        for (int i = 1; i <= maxNum; i++) {
            candidates.add(i);
        }
    }

    public Cell(Cell cell) {
        this.r = cell.r;
        this.c = cell.c;
        this.x = cell.x;
        this.y = cell.y;
        this.candidates = cell.candidates;
    }

    public void paintCandidates(Graphics g, int cellWidth) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        this.x = c * cellWidth + Board.startX;
        this.y = r * cellWidth + Board.startY;
        if (candidates.size() == 1) {
            int size = (int) (cellWidth / factor2);
            Integer i = candidates.get(0);
            g2.setColor(Board.bg);
            g2.fillRect(x + 1, y + 1, cellWidth - 1, cellWidth - 1);
            g2.setColor(Color.black);
            g2.setFont(new Font(null, 0, size));
            g2.drawString(i + "", x + size / 2, y + size);
        } else {
            int size = (int) (cellWidth / factor);
            g2.setFont(new Font(null, 0, size));
            for (int i = 1; i <= maxNum; i++) {
                if (candidates.contains(i)) {
                    g2.setColor(Color.lightGray);
                } else {
                    g2.setColor(Board.bg);
                }
                Point p = getCandidatePoint(i, cellWidth);
                g2.drawString(i + "", p.x, p.y);
            }
        }
    }

    public Point getCandidatePoint(int num, int cellWidth) {
        int size = (int) (cellWidth / factor);
        int x2 = (int) (x + size / factor);
        int y2 = (int) (y + size / factor);
        int r = (num - 1) / 3 + 1;
        int c = (num - 1) % 3;
        return new Point(x2 + c * size, y2 + r * size);
    }

    public List<Point> getAffectCells() {
        Cell cell = this;
        List<Point> list = new ArrayList<>();
        for (int i = 0; i < Board.rows; i++) {
            if (i != cell.r) {
                list.add(new Point(i, cell.c));
            }
        }
        for (int i = 0; i < Board.cols; i++) {
            if (i != cell.c) {
                list.add(new Point(cell.r, i));
            }
        }
        int r1 = cell.r / 3;
        int c1 = cell.c / 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int r2 = r1 * 3 + i;
                int c2 = c1 * 3 + j;
                if (r2 != cell.r && c2 != cell.c) {
                    list.add(new Point(r2, c2));
                }
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return r == cell.r &&
                c == cell.c &&
                x == cell.x &&
                y == cell.y &&
                linkNum == cell.linkNum &&
                Objects.equals(candidates, cell.candidates);
    }

    @Override
    public int hashCode() {

        return Objects.hash(r, c, x, y, candidates, linkNum);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "r=" + (r + 1) +
                ", c=" + (c + 1) +
                ", candidates=" + candidates +
                ", excludes=" + excludes +
                ", linkNum=" + linkNum +
                '}';
    }

    public boolean overlap(Cell cell) {
        return this.r == cell.r && this.c == cell.c;
    }

    public boolean sameBlock(Cell cell) {
        int r1 = cell.r / 3;
        int c1 = cell.c / 3;
        int r2 = this.r / 3;
        int c2 = this.c / 3;
        return r1 == r2 && c1 == c2;
    }
}
