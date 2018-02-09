package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by BC on 1/31/18.
 */
public class StrongLink {
    LinkNode node1 = new LinkNode();
    LinkNode node2 = new LinkNode();
    private static String[] orders = {"1234", "1243", "2134", "2143"};

    public StrongLink(List<Cell> value, int num) {
        Cell c1 = new Cell(value.get(0));
        Cell c2 = new Cell(value.get(1));
        c1.linkNum = num;
        c2.linkNum = num;
        this.node1.cells.add(c1);
        this.node2.cells.add(c2);
    }

    public StrongLink(Cell cell) {
        Cell c1 = new Cell(cell);
        Cell c2 = new Cell(cell);
        c1.linkNum = cell.candidates.get(0);
        c2.linkNum = cell.candidates.get(1);
        this.node1.cells.add(c1);
        this.node2.cells.add(c2);
    }

    public StrongLink(List<Cell> list1, List<Cell> list2, int num) {
        for (Cell cell : list1) {
            Cell c1 = new Cell(cell);
            c1.linkNum = num;
            this.node1.cells.add(c1);
        }
        for (Cell cell : list2) {
            Cell c2 = new Cell(cell);
            c2.linkNum = num;
            this.node2.cells.add(c2);
        }
    }

    public StrongLink(LinkNode node1, LinkNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public String toString() {
        return "StrongLink{" +
                "node1=" + node1 +
                ", node2=" + node2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrongLink that = (StrongLink) o;
        if(this.node1.equals(that.node2) && this.node2.equals(that.node1)){
            return true;
        }
        return Objects.equals(node1, that.node1) &&
                Objects.equals(node2, that.node2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1, node2);
    }

    public void paintMe(Graphics g, int cellWidth) {
        int size = (int) (cellWidth / Cell.factor);
        Cell c1 = node1.cells.get(0);
        Cell c2 = node2.cells.get(0);
        Point p1 = c1.getCandidatePoint(c1.linkNum, cellWidth);
        Point p2 = c2.getCandidatePoint(c2.linkNum, cellWidth);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);

        for (Cell cell : node1.cells) {
            p1 = cell.getCandidatePoint(cell.linkNum, cellWidth);
            g.drawArc(p1.x, p1.y - size, size, size, 0, 360);
        }
        for (Cell cell : node2.cells) {
            p2 = cell.getCandidatePoint(cell.linkNum, cellWidth);
            g.drawArc(p2.x, p2.y - size, size, size, 0, 360);
        }
    }

    public List<LinkNode[]> getOrders(StrongLink link2) {
        List<LinkNode[]> list = new ArrayList<>();
        LinkNode[] nodes = {this.node1, this.node2, link2.node1, link2.node2};
        for (String order : orders) {
            LinkNode[] nodes2 = new LinkNode[4];
            for (int i = 0; i < order.length(); i++) {
                char ch = order.charAt(i);
                nodes2[i] = nodes[Integer.parseInt(ch+"")-1];
            }
            list.add(nodes2);
        }
        return list;
    }

}
