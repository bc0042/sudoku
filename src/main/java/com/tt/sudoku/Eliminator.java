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
    private static String c1_linked = "c1-linked";
    private static String c2_linked = "c2-linked";

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
        List<Cell> list = getCellsContainsNum(num);
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

    private static List<Cell> getCellsContainsNum(int num) {
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

    public static void findAndExclude() {
        findChain();
        exclude();
        run();
    }

    /**
     * find all strong links
     * take two strong links
     * if closed then link them up
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

        // link orders of two links
        String[] orders = {"1234", "1243", "3412", "3421"};
        ArrayList<StrongLink> list = new ArrayList<>(set);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (i == j) continue;
                StrongLink link1 = list.get(i);
                StrongLink link2 = list.get(j);
                ArrayList<StrongLink> rest = new ArrayList<>(list);
                rest.remove(link1);
                rest.remove(link2);


                Cell[] cells = {link1.c1, link1.c2, link2.c1, link2.c2};
                for (String order : orders) {
                    Cell[] cells2 = new Cell[4];
                    for (int k = 0; k < order.length(); k++) {
                        char ch = order.charAt(k);
                        cells2[k] = cells[Integer.parseInt(""+ch)-1];
                    }
                    checkHeadAndTail(cells2, rest);
                    if(!excludeList.isEmpty()) return;
                }

            }
        }
    }

    private static void checkHeadAndTail(Cell[] cells, ArrayList<StrongLink> rest) {
        LinkedList<Cell> head = new LinkedList<>();
        head.add(cells[0]);
        head.add(cells[1]);
        LinkedList<Cell> tail = new LinkedList<>();
        tail.add(cells[2]);
        tail.add(cells[3]);
        List<Cell> excludes = getExcludes(head.getFirst(), tail.getLast());
        if (!excludes.isEmpty() && linkUp(head, tail, rest)) {
            excludeList = excludes;
        }
    }


    private static boolean linkUp(LinkedList<Cell> head, LinkedList<Cell> tail, ArrayList<StrongLink> rest) {
        // chain cannot contains the next link
        if (head.contains(tail.getFirst()) || head.contains(tail.getLast())) return false;

        //debug
//        if (head.getFirst().num == 7
//                && head.getLast().num == 7
//                && head.getFirst().r == 2 - 1
//                && head.getFirst().c == 7 - 1
//
//                && head.getLast().r == 2 - 1
//                && head.getLast().c == 4 - 1
//
//                && tail.getFirst().num == 2
//                && tail.getLast().num == 1
//                ) {
//            Debug.println();
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
                return linkUpRest(head2, tail, rest2, i, c1_linked);
            } else if (isLinked(head.getLast(), next.c2)) {
                return linkUpRest(head2, tail, rest2, i, c2_linked);
            }
        }
        return false;
    }

    private static boolean linkUpRest(LinkedList<Cell> head2, LinkedList<Cell> tail, ArrayList<StrongLink> rest2, int i, String type) {
        boolean linked;
        StrongLink next = rest2.get(i);
        if (c1_linked.equals(type)) {
            head2.add(next.c1);
            head2.add(next.c2);
        } else if (c2_linked.equals(type)) {
            head2.add(next.c2);
            head2.add(next.c1);
        }
        if (isLinked(head2.getLast(), tail.getFirst())) {
            head2.addAll(tail);
            chain = head2;
            return true;
        } else {
            rest2.remove(i);
            linked = linkUp(head2, tail, rest2);
        }
        if (linked) {
            return true;
        } else {
            head2.removeLast();
            head2.removeLast();
            return linkUp(head2, tail, rest2);
        }
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
//                Debug.println(String.format("make: r%sc%s-%s", head.r + 1, head.c + 1, head.num));
            } else {
                // loop
                if (head.candidates.size() > 2) {
                    List<Integer> retain = Arrays.asList(head.num, tail.num);
                    Cell e = new Cell(head);
                    e.excludes = new ArrayList<>(head.candidates);
                    e.excludes.removeAll(retain);
                    excludeList.add(e);
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
//                        Debug.println(String.format("exclude: r%sc%s-%s", cell.r + 1, cell.c + 1, head.num));
                    }
                }
            } else if (head.r == tail.r || head.c == tail.c || head.sameBlock(tail)) {
                // head num not equals to tail num
                if (head.candidates.contains(tail.num)) {
                    Cell e = new Cell(head);
                    e.excludes = Collections.singletonList(tail.num);
                    excludeList.add(e);
//                    Debug.println(String.format("exclude: r%sc%s-%s", head.r + 1, head.c + 1, tail.num));
                }
                if (tail.candidates.contains(head.num)) {
                    Cell e = new Cell(tail);
                    e.excludes = Collections.singletonList(head.num);
                    excludeList.add(e);
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


    public static void findHiddenSingle() {
        Debug.println("find hidden single..");
        List<Cell> singles = new ArrayList<>();
        for (int num = 1; num <= Cell.maxNum; num++) {
            List<Cell> list = getCellsContainsNum(num);
            Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
            for (Map.Entry<Integer, List<Cell>> entry : map.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, row", num, cell.r + 1, cell.c + 1));
                    cell.num = num;
                    singles.add(cell);
                }
            }
            Map<Integer, List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
            for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, col", num, cell.r + 1, cell.c + 1));
                    cell.num = num;
                    singles.add(cell);
                }
            }
            Map<Point, List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                    cell -> new Point(cell.r / 3, cell.c / 3)));
            for (Map.Entry<Point, List<Cell>> entry : map3.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, block", num, cell.r + 1, cell.c + 1));
                    cell.num = num;
                    singles.add(cell);
                }
            }
        }
        for (Cell single : singles) {
            single.candidates.clear();
            single.candidates.add(single.num);
        }
        run();
    }

    public static void exclude() {
        if (chain != null && chain.size() > 2) {
            Debug.printChain(chain);
        }
        if (excludeList != null) {
            Debug.printExcludeList(excludeList);
            for (Cell cell : excludeList) {
                cell.candidates.removeAll(cell.excludes);
            }
        }
    }
}
