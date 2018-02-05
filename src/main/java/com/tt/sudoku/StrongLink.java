package com.tt.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by BC on 1/31/18.
 */
public class StrongLink {
    LinkNode node1 = new LinkNode();
    LinkNode node2 = new LinkNode();
    private static String[] orders = {"1234", "1243", "3412", "3421"};

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
        return Objects.equals(node1, that.node1) &&
                Objects.equals(node2, that.node2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1, node2);
    }

    public List<List<LinkNode>> arrange(StrongLink link) {
        List<List<LinkNode>> list = new ArrayList<>();
        List<LinkNode> cells = Arrays.asList(this.node1, this.node2, link.node1, link.node2);
        for (String order : orders) {
            List<LinkNode> cells2 = new ArrayList<>();
            for (int k = 0; k < order.length(); k++) {
                char ch = order.charAt(k);
                cells2.add(cells.get(Integer.parseInt("" + ch) - 1));
            }
            list.add(cells2);
        }
        return list;
    }
}
