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

    public int sameRow(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        if (map.size() == 1) {
            Integer row = map.entrySet().iterator().next().getKey();
            if (this.isSingle() && !node.cells.contains(this.getFirstCell())) {
                return row;
            }
            if (node.isSingle() && !this.cells.contains(node.getFirstCell())) {
                return row;
            }
        }
        return 0;
    }

    public int sameCol(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        if (map.size() == 1) {
            Integer col = map.entrySet().iterator().next().getKey();
            if (this.isSingle() && !node.cells.contains(this.getFirstCell())) {
                return col;
            }
            if (node.isSingle() && !this.cells.contains(node.getFirstCell())) {
                return col;
            }
        }
        return 0;
    }

    public Point sameBlock(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(node.cells);
        Map<Point, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> new Point(cell.r / 3, cell.c / 3)));
        if (map.size() == 1) {
            Point block = map.entrySet().iterator().next().getKey();
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

    public List<Cell> addAll(LinkNode p) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(p.cells);
        return list;
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
            if (row != 0) {
                for (int i = 0; i < Board.cols; i++) {
                    set.add(new Point(row, i));
                }
            } else {
                int col = this.sameCol(new LinkNode());
                if (col != 0) {
                    for (int i = 0; i < Board.rows; i++) {
                        set.add(new Point(i, col));
                    }
                }
            }

            int r1 = this.getFirstCell().r / 3;
            int c1 = this.getFirstCell().c / 3;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int r2 = r1 * 3 + i;
                    int c2 = c1 * 3 + j;
                    set.add(new Point(r2, c2));
                }
            }

            List<Point> list = new ArrayList<>();
            for (Cell cell : this.cells) {
                list.add(new Point(cell.r, cell.c));
            }

            set.removeAll(list);
            return set;
        }
    }

    public boolean close(LinkNode node) {
        if (this.overlap(node) || node.overlap(this)) {
            return false;
        }

        if (this.getFirstCell().linkNum == node.getFirstCell().linkNum) {
            Set<Point> affectPoints = this.getAffectPoints();
            Set<Point> affectPoints2 = node.getAffectPoints();
            boolean retain = affectPoints.retainAll(affectPoints2);
            if (retain) {
                for (Point p : affectPoints) {
                    boolean contains = Board.cells[p.x][p.y].candidates.contains(this.getFirstCell().linkNum);
                    if (contains) {
                        return true;
                    }
                }
            }
        } else {
            // different link number
            if (this.sameRow(node) != 0 || this.sameCol(node) != 0 || this.sameBlock(node) != null) {
                if (this.isSingle() && this.getFirstCell().candidates.contains(node.getFirstCell().linkNum)) {
                    return true;
                }
                if (node.isSingle() && node.getFirstCell().candidates.contains(this.getFirstCell().linkNum)) {
                    return true;
                }

            }
        }
        return false;
    }

    public List<Cell> exclude(LinkNode node) {
        List<Cell> list = new ArrayList<>();
        int linkNum1 = this.getFirstCell().linkNum;
        int linkNum2 = node.getFirstCell().linkNum;
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
            // different link number
            if (this.sameRow(node) != 0 || this.sameCol(node) != 0 || this.sameBlock(node) != null) {
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

    public boolean link(LinkNode node) {
        if (this.getFirstCell().linkNum == node.getFirstCell().linkNum) {
            if (this.sameRow(node) != 0 || this.sameCol(node) != 0 || this.sameBlock(node) != null) {
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
                if(cell1.overlap(cell)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean singleOverlap(LinkNode node) {
        if(this.isSingle() && node.isSingle() && !this.sameLinkNum(node)
                && this.getFirstCell().overlap(node.getFirstCell())){
            return true;
        }
        return false;
    }

    private boolean sameLinkNum(LinkNode node) {
        return this.getFirstCell().linkNum == node.getFirstCell().linkNum;
    }
}
