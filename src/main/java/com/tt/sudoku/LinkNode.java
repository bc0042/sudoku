package com.tt.sudoku;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by BC on 2/5/18.
 */
public class LinkNode {
    List<Cell> cells = new ArrayList<>();

    public int sameRow(LinkNode p) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(p.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        if (map.size() == 1) {
            Integer row = map.entrySet().iterator().next().getKey();
            if(this.cells.size()==1 && !p.cells.contains(this.cells.get(0))){
                return row;
            }
            if(p.cells.size()==1 && !this.cells.contains(p.cells.get(0))){
                return row;
            }
        }
        return 0;
    }

    public Integer sameCol(LinkNode p) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(p.cells);
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        if (map.size() == 1) {
            Integer col = map.entrySet().iterator().next().getKey();
            if(this.cells.size()==1 && !p.cells.contains(this.cells.get(0))){
                return col;
            }
            if(p.cells.size()==1 && !this.cells.contains(p.cells.get(0))){
                return col;
            }
        }
        return 0;
    }

    public Point sameBlock(LinkNode p) {
        List<Cell> list = new ArrayList<>();
        list.addAll(this.cells);
        list.addAll(p.cells);
        Map<Point, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> new Point(cell.r/3, cell.c/3)));
        if (map.size() == 1) {
            Point block = map.entrySet().iterator().next().getKey();
            if(this.cells.size()==1 && !p.cells.contains(this.cells.get(0))){
                return block;
            }
            if(p.cells.size()==1 && !this.cells.contains(p.cells.get(0))){
                return block;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (Cell cell : cells) {
            sb.append(String.format("r%dc%d,", cell.r+1, cell.c+1));
        }
        sb.append("]-"+cells.get(0).linkNum);
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

    public Cell getSingle() {
        return cells.get(0);
    }

    public List<Point> getAffectBlock() {
        List<Point> list = new ArrayList<>();
        List<Point> list2 = new ArrayList<>();
        for (Cell cell : cells) {
            Point point = new Point(cell.r, cell.c);
            list.add(point);
        }
        Point first = list.get(0);
        int r = first.x /3;
        int c = first.y /3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                list2.add(new Point(r*3+i, c*3+j));
            }
        }
        list2.removeAll(list);
        return list2;
    }
}
