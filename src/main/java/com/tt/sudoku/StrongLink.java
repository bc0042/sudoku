package com.tt.sudoku;

import java.util.List;
import java.util.Objects;

/**
 * Created by BC on 1/31/18.
 */
public class StrongLink {
    Cell c1;
    Cell c2;

    public StrongLink(List<Cell> value, int num) {
        this.c1 = new Cell(value.get(0));
        this.c2 = new Cell(value.get(1));
        this.c1.num = num;
        this.c2.num = num;
    }

    public StrongLink(Cell cell) {
        this.c1 = new Cell(cell);
        this.c2 = new Cell(cell);
        this.c1.num = cell.candidates.get(0);
        this.c2.num = cell.candidates.get(1);
    }

    @Override
    public String toString() {
        return "StrongLink{" +
                "c1=" + c1 +
                ", c2=" + c2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrongLink that = (StrongLink) o;
        if (Objects.equals(c1, that.c2) &&
                Objects.equals(c2, that.c1)) return true;
        return Objects.equals(c1, that.c1) &&
                Objects.equals(c2, that.c2);
    }

    @Override
    public int hashCode() {

        return Objects.hash(c1, c2);
    }

    public boolean contains(Cell cell) {
        return this.c1.equals(cell) || this.c2.equals(cell);
    }
}
