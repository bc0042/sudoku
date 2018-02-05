package com.tt.sudoku;

import java.util.List;
import java.util.Objects;

/**
 * Created by BC on 1/31/18.
 */
public class StrongLink {
    LinkPoint p1 = new LinkPoint();
    LinkPoint p2 = new LinkPoint();

    public StrongLink(List<Cell> value, int num) {
        Cell c1 = new Cell(value.get(0));
        Cell c2 = new Cell(value.get(1));
        c1.linkNum = num;
        c2.linkNum = num;
        this.p1.cells.add(c1);
        this.p2.cells.add(c2);
    }

    public StrongLink(Cell cell) {
        Cell c1 = new Cell(cell);
        Cell c2 = new Cell(cell);
        c1.linkNum = cell.candidates.get(0);
        c2.linkNum = cell.candidates.get(1);
        this.p1.cells.add(c1);
        this.p2.cells.add(c2);
    }

    public StrongLink(List<Cell> list1, List<Cell> list2, int num) {
        for (Cell cell : list1) {
            Cell c1 = new Cell(cell);
            c1.linkNum = num;
            this.p1.cells.add(c1);
        }
        for (Cell cell : list2) {
            Cell c2 = new Cell(cell);
            c2.linkNum = num;
            this.p2.cells.add(c2);
        }
    }

    @Override
    public String toString() {
        return "StrongLink{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrongLink that = (StrongLink) o;
        return Objects.equals(p1, that.p1) &&
                Objects.equals(p2, that.p2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }
}
