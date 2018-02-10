package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 2/5/18.
 */
public class LinkNode {
    List<Cell> cells = new ArrayList<>();

    public LinkNode() {

    }

    public LinkNode(Cell cell) {
        cells.add(cell);
    }

    public int sameRow(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        if (map.size() == 1) {
            Integer row = map.entrySet().iterator().next().getKey();
            if (node.cells.isEmpty()) {
                return row;
            }
            if (this.isSingle() && !node.cells.contains(this.getFirstCell())) {
                return row;
            }
            if (node.isSingle() && !this.cells.contains(node.getFirstCell())) {
                return row;
            }
        }
        return -1;
    }

    public int sameCol(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        if (map.size() == 1) {
            Integer col = map.entrySet().iterator().next().getKey();
            if (node.cells.isEmpty()) {
                return col;
            }
            if (this.isSingle() && !node.cells.contains(this.getFirstCell())) {
                return col;
            }
            if (node.isSingle() && !this.cells.contains(node.getFirstCell())) {
                return col;
            }
        }
        return -1;
    }

    public Point sameBlock(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Point, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> new Point(cell.r / 3, cell.c / 3)));
        if (map.size() == 1) {
            Point block = map.entrySet().iterator().next().getKey();
            if (node.cells.isEmpty()) {
                return block;
            }
            if (this.isSingle() && !node.cells.contains(this.getFirstCell())) {
                return block;
            }
            if (node.isSingle() && !this.cells.contains(node.getFirstCell())) {
                return block;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (Cell cell : cells) {
            sb.append(String.format("r%dc%d,", cell.r + 1, cell.c + 1));
        }
        sb.append("]-" + cells.get(0).linkNum);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkNode linkPoint = (LinkNode) o;
        return Objects.equals(cells, linkPoint.cells);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cells);
    }


    public boolean isSingle() {
        return cells.size() == 1;
    }

    public Cell getFirstCell() {
        return cells.get(0);
    }


    private Set<Point> getAffectPoints() {
        if (this.isSingle()) {
            return this.getFirstCell().getAffectPoints();
        } else {
            Set<Point> set = new HashSet<>();
            int row = this.sameRow(new LinkNode());
            if (row != -1) {
                for (int i = 0; i < Board.cols; i++) {
                    set.add(new Point(row, i));
                }
            }

            int col = this.sameCol(new LinkNode());
            if (col != -1) {
                for (int i = 0; i < Board.rows; i++) {
                    set.add(new Point(i, col));
                }
            }

            Point block = this.sameBlock(new LinkNode());
            if(block != null){
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int r2 = block.x * 3 + i;
                        int c2 = block.y * 3 + j;
                        set.add(new Point(r2, c2));
                    }
                }
            }

            for (Cell cell : this.cells) {
                set.remove(new Point(cell.r, cell.c));
            }
            return set;
        }
    }

    public boolean close(LinkNode node) {
        if (this.overlap(node) || node.overlap(this)) {
            return false;
        }

        if (this.getLinkNum() == node.getLinkNum()) {
            Set<Point> affectPoints = this.getAffectPoints();
            Set<Point> affectPoints2 = node.getAffectPoints();
            boolean retain = affectPoints.retainAll(affectPoints2);
            if (retain) {
                for (Point p : affectPoints) {
                    boolean contains = Board.cells[p.x][p.y].candidates.contains(this.getLinkNum());
                    if (contains) {
                        return true;
                    }
                }
            }
        } else {
            // different weakLink number
            if (this.sameRow(node) != -1 || this.sameCol(node) != -1 || this.sameBlock(node) != null) {
                if (this.isSingle() && this.getFirstCell().candidates.contains(node.getLinkNum())) {
                    return true;
                }
                if (node.isSingle() && node.getFirstCell().candidates.contains(this.getLinkNum())) {
                    return true;
                }

            }
        }
        return false;
    }

    public List<Cell> exclude(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        int linkNum1 = this.getLinkNum();
        int linkNum2 = node.getLinkNum();
        if (linkNum1 == linkNum2) {
            Set<Point> affectPoints = this.getAffectPoints();
            Set<Point> affectPoints2 = node.getAffectPoints();
            boolean retain = affectPoints.retainAll(affectPoints2);
            if (retain) {
                for (Point p : affectPoints) {
                    Cell cell = Board.cells[p.x][p.y];
                    boolean contains = cell.candidates.contains(linkNum1);
                    if (contains) {
                        cell.excludes = Collections.singletonList(linkNum1);
                        list.add(cell);
                    }
                }
            }
        } else {
            // different weakLink number
            if (this.sameRow(node) != -1 || this.sameCol(node) != -1 || this.sameBlock(node) != null) {
                if (this.isSingle() && this.getFirstCell().candidates.contains(linkNum2)) {
                    this.getFirstCell().excludes = Collections.singletonList(linkNum2);
                    list.add(this.getFirstCell());
                }
                if (node.isSingle() && node.getFirstCell().candidates.contains(linkNum1)) {
                    node.getFirstCell().excludes = Collections.singletonList(linkNum1);
                    list.add(node.getFirstCell());
                }

            }
        }
        return list;
    }

    public boolean weakLink(LinkNode node) {
        if (this.getLinkNum() == node.getLinkNum()) {
            if (this.sameRow(node) != -1 || this.sameCol(node) != -1 || this.sameBlock(node) != null) {
                return true;
            }
        } else {
            if (this.isSingle() && node.isSingle()
                    && this.getFirstCell().overlap(node.getFirstCell())) {
                return true;
            }
        }
        return false;
    }

    private boolean overlap(LinkNode node) {
        for (Cell cell : this.cells) {
            for (Cell cell1 : node.cells) {
                if (cell1.overlap(cell)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean singleOverlap(LinkNode node) {
        if (this.isSingle() && node.isSingle() && !this.sameLinkNum(node)
                && this.getFirstCell().overlap(node.getFirstCell())) {
            return true;
        }
        return false;
    }

    private boolean sameLinkNum(LinkNode node) {
        return this.getLinkNum() == node.getLinkNum();
    }

    public int getLinkNum() {
        return getFirstCell().linkNum;
    }

    public void paintMe(Graphics g, int cellWidth) {
        int size = (int) (cellWidth / Cell.factor);
        Cell c1 = getFirstCell();
        Point p1 = c1.getCandidatePoint(c1.linkNum, cellWidth);
        g.drawArc(p1.x, p1.y - size, size, size, 0, 360);
    }
}
