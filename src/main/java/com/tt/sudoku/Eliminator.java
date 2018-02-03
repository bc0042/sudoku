package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class Eliminator {
    static List<Cell> chain = new ArrayList<>();
    static List<Cell> excludeList = new ArrayList<>();

    public static void run() {
        Debug.println("eliminate..");
        for (int r = 0; r < Board.rows; r++) {
            for (int c = 0; c < Board.cols; c++) {
                Cell cell = Board.cells[r][c];
                if (cell.candidates.size() == 1) {
                    excludeByNum(cell);
                }
            }
        }
    }

    private static void excludeByNum(Cell cell) {
        Integer num = cell.candidates.get(0);
        for (Point p : getAffectPoints(cell)) {
            Cell cell1 = Board.cells[p.x][p.y];
            if (cell1.candidates.remove(num) && cell1.candidates.size() == 1) {
                // if only one candidate, check again
                excludeByNum(cell1);
            }
        }
    }


    public static List<StrongLink> findStrongLinksByNum(int num) {
        List<Cell> list = getCellsByCandidate(num);
        List<StrongLink> strongLinks = new ArrayList<>();
        // strong link in rows
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        for (Map.Entry<Integer, List<Cell>> entry : map.entrySet()) {
            if (entry.getValue().size() == 2) {
                StrongLink sl = new StrongLink(entry.getValue(), num);
                strongLinks.add(sl);
            }
        }
        // strong link in cols
        Map<Integer, List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
            if (entry.getValue().size() == 2) {
                StrongLink sl = new StrongLink(entry.getValue(), num);
                strongLinks.add(sl);
            }
        }
        // strong link in blocks
        Map<Point, List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        for (Map.Entry<Point, List<Cell>> entry : map3.entrySet()) {
            if (entry.getValue().size() == 2) {
                StrongLink sl = new StrongLink(entry.getValue(), num);
                strongLinks.add(sl);
            }
        }
        return strongLinks;
    }

    private static List<Cell> getCellsByCandidate(int num) {
        List<Cell> list = new ArrayList<>();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.contains(num) && cell.candidates.size() > 1) {
                    list.add(cell);
                }
            }
        }
        return list;
    }

    public static List<StrongLink> findXYLinks() {
        List<StrongLink> list = new ArrayList<>();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.size() == 2) {
                    list.add(new StrongLink(cell));
                }
            }
        }
        return list;
    }

    /**
     * find all strong links
     * take two strong links
     * if closed then linkup them
     */
    public static void findChain() {
        List<StrongLink> strongLinks = new ArrayList<>();
        for (int i = 1; i <= Cell.maxNum; i++) {
            List<StrongLink> list2 = Eliminator.findStrongLinksByNum(i);
            strongLinks.addAll(list2);
        }
        // xy links
        strongLinks.addAll(Eliminator.findXYLinks());

        Debug.println("all strong links: " + strongLinks.size());
        HashSet<StrongLink> set = new HashSet<>(strongLinks);
        Debug.println("distinct strong links: " + set.size());

        ArrayList<StrongLink> list = new ArrayList<>(set);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (i == j) continue;
                StrongLink link1 = list.get(i);
                StrongLink link2 = list.get(j);
                ArrayList<StrongLink> rest = new ArrayList<>(list);
                rest.remove(link1);
                rest.remove(link2);

                List<Cell> excludes1 = getExcludes(link1.c1, link2.c1);
                if (!excludes1.isEmpty()) {
                    LinkedList<Cell> head = new LinkedList<>();
                    LinkedList<Cell> tail = new LinkedList<>();
                    head.add(link1.c1);
                    head.add(link1.c2);
                    tail.add(link2.c2);
                    tail.add(link2.c1);
                    if (linkUp(head, tail, rest)) {
                        excludeList = excludes1;
                        return;
                    }
                }

                List<Cell> excludes2 = getExcludes(link1.c1, link2.c2);
                if (!excludes2.isEmpty()) {
                    LinkedList<Cell> head = new LinkedList<>();
                    LinkedList<Cell> tail = new LinkedList<>();
                    head.add(link1.c1);
                    head.add(link1.c2);
                    tail.add(link2.c1);
                    tail.add(link2.c2);
                    if (linkUp(head, tail, rest)) {
                        excludeList = excludes2;
                        return;
                    }
                }


                List<Cell> excludes3 = getExcludes(link1.c2, link2.c1);
                if (!excludes3.isEmpty()) {
                    LinkedList<Cell> head = new LinkedList<>();
                    LinkedList<Cell> tail = new LinkedList<>();
                    head.add(link1.c2);
                    head.add(link1.c1);
                    tail.add(link2.c2);
                    tail.add(link2.c1);
                    if (linkUp(head, tail, rest)) {
                        excludeList = excludes3;
                        return;
                    }
                }

                List<Cell> excludes4 = getExcludes(link1.c2, link2.c2);
                if (!excludes4.isEmpty()) {
                    LinkedList<Cell> head = new LinkedList<>();
                    LinkedList<Cell> tail = new LinkedList<>();
                    head.add(link1.c2);
                    head.add(link1.c1);
                    tail.add(link2.c1);
                    tail.add(link2.c2);
                    if (linkUp(head, tail, rest)) {
                        excludeList = excludes4;
                        return;
                    }
                }
            }
        }
    }

    private static boolean linkUp(LinkedList<Cell> head, LinkedList<Cell> tail, ArrayList<StrongLink> rest) {
        // chain cannot contains the next link
        if (head.contains(tail.getFirst()) || head.contains(tail.getLast())) return false;

        //debug
//        if (head.getFirst().num == 6
//                && head.getLast().num == 6
//                && head.getFirst().r == 5 - 1
//                && head.getFirst().c == 2 - 1
//                && tail.getFirst().num == 5
//                && tail.getLast().num == 5
//                ) {
//            System.out.println();
//        }

        if (head.size() == 2) {
            if (isLinked(head.getLast(), tail.getFirst())) {
                head.addAll(tail);
                chain = head;
                return true;
            }
        }

        for (int i = 0; i < rest.size(); i++) {
            StrongLink next = rest.get(i);
            // chain cannot contains the next link
            if (head.contains(next.c1) || head.contains(next.c2)) continue;

            LinkedList<Cell> head2 = new LinkedList<>(head);
            ArrayList<StrongLink> rest2 = new ArrayList<>(rest);
            if (isLinked(head.getLast(), next.c1)) {
                head2.add(next.c1);
                head2.add(next.c2);
                if (isLinked(next.c2, tail.getFirst())) {
                    head2.addAll(tail);
                    chain = head2;
                    return true;
                } else {
                    rest2.remove(i);
                    return linkUp(head2, tail, rest2);
                }
            } else if (isLinked(head.getLast(), next.c2)) {
                head2.add(next.c2);
                head2.add(next.c1);
                if (isLinked(next.c1, tail.getFirst())) {
                    head2.addAll(tail);
                    chain = head2;
                    return true;
                } else {
                    rest2.remove(i);
                    return linkUp(head2, tail, rest2);
                }
            }
        }
        return false;
    }

    private static List<Cell> getExcludes(Cell head, Cell tail) {
        List<Cell> excludeList = new ArrayList<>();
        if (head.overlap(tail)) {
            if (head.num == tail.num) {
                // nice loop
                Cell e = new Cell(head);
                e.excludes = new ArrayList<>(head.candidates);
                e.excludes.remove(new Integer(head.num));
                excludeList.add(e);
//                head.candidates.clear();
//                head.candidates.add(head.num);
//                Debug.println(String.format("make: r%sc%s-%s", head.r + 1, head.c + 1, head.num));
            } else {
                // loop
                if (head.candidates.size() > 2) {
                    List<Integer> retain = Arrays.asList(head.num, tail.num);
                    Cell e = new Cell(head);
                    e.excludes = new ArrayList<>(head.candidates);
                    e.excludes.removeAll(retain);
                    excludeList.add(e);
//                    head.candidates.retainAll(retain);
//                    Debug.println(String.format("retain: r%sc%s-%s", head.r + 1, head.c + 1, head.num + "," + tail.num));
                }
            }
        } else {
            // not the same cell
            if (head.num == tail.num) {
                List<Point> list1 = getAffectPoints(head);
                List<Point> list2 = getAffectPoints(tail);
                list1.retainAll(list2);
                for (Point p : list1) {
                    Cell cell = Board.cells[p.x][p.y];
                    if (cell.candidates.contains(head.num)) {
                        Cell e = new Cell(cell);
                        e.excludes = Collections.singletonList(head.num);
                        excludeList.add(e);
//                        cell.candidates.remove(new Integer(head.num));
//                        Debug.println(String.format("exclude: r%sc%s-%s", cell.r + 1, cell.c + 1, head.num));
                    }
                }
            } else if (head.r == tail.r || head.c == tail.c || head.sameBlock(tail)) {
                // head num not equals to tail num
                if (head.candidates.contains(tail.num)) {
                    Cell e = new Cell(head);
                    e.excludes = Collections.singletonList(tail.num);
                    excludeList.add(e);
//                    head.candidates.remove(new Integer(tail.num));
//                    Debug.println(String.format("exclude: r%sc%s-%s", head.r + 1, head.c + 1, tail.num));
                }
                if (tail.candidates.contains(head.num)) {
                    Cell e = new Cell(tail);
                    e.excludes = Collections.singletonList(head.num);
                    excludeList.add(e);
//                    tail.candidates.remove(new Integer(head.num));
//                    Debug.println(String.format("exclude: r%sc%s-%s", tail.r + 1, tail.c + 1, head.num));
                }
            }
        }
        return excludeList;
    }

    private static boolean isLinked(Cell prev, Cell next) {
        if (prev.overlap(next)) {
            if (prev.num != next.num) {
                return true;
            }
        } else {
            if (prev.num == next.num &&
                    (prev.r == next.r || prev.c == next.c || prev.sameBlock(next))) {
                return true;
            }
        }
        return false;
    }


    private static List<Point> getAffectPoints(Cell cell) {
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


    public static void hiddenSingle() {
        Debug.println("find hidden single..");
        for (int num = 1; num <= Cell.maxNum; num++) {
            List<Cell> list = getCellsByCandidate(num);
            Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
            for (Map.Entry<Integer, List<Cell>> entry : map.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, row", num, cell.r + 1, cell.c + 1));
                    cell.candidates.retainAll(Collections.singletonList(num));
                }
            }
            Map<Integer, List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
            for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, col", num, cell.r + 1, cell.c + 1));
                    cell.candidates.retainAll(Collections.singletonList(num));
                }
            }
            Map<Point, List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                    cell -> new Point(cell.r / 3, cell.c / 3)));
            for (Map.Entry<Point, List<Cell>> entry : map3.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, block", num, cell.r + 1, cell.c + 1));
                    cell.candidates.retainAll(Collections.singletonList(num));
                    Debug.println(cell);
                }
            }
        }
    }

    public static void exclude() {
        if (chain != null && chain.size() > 2) {
            Debug.printChain(chain);
            chain.clear();
        }
        if (excludeList != null) {
            Debug.printExcludeList(excludeList);
            for (Cell cell : excludeList) {
                cell.candidates.removeAll(cell.excludes);
            }
            excludeList.clear();
        }
    }
}
