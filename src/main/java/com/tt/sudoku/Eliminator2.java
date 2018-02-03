package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class Eliminator2 {
    private static boolean found;
    private static int linked;

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


    public static List<StrongLink> findStrongLinks(int num) {
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

    public static List<StrongLink> findStrongLinks2() {
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


    public static void findChain() {
        List<StrongLink> strongLinks = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            List<StrongLink> list2 = Eliminator2.findStrongLinks(i);
            strongLinks.addAll(list2);
        }
        // xy links
        strongLinks.addAll(Eliminator2.findStrongLinks2());

        Debug.println("all strong links: " + strongLinks.size());
        HashSet<StrongLink> set = new HashSet<>(strongLinks);
        Debug.println("distinct strong links: " + set.size());
//        board.paintLinks(list3);

        found = false;
        for (int i = 0; i < set.size(); i++) {
            ArrayList<StrongLink> strongLinks2 = new ArrayList<>(set);
            StrongLink first = strongLinks2.remove(i);

//            LinkedList<Cell> chain = new LinkedList<>();
//            chain.add(first.c1);
//            chain.add(first.c2);
//            findClosedChain(chain, strongLinks2);

            LinkedList<Cell> chain2 = new LinkedList<>();
            chain2.add(first.c2);
            chain2.add(first.c1);
            findClosedChain(chain2, strongLinks2);
        }
    }

    /**
     * // 取两条强链
     * // 判断是否能连上
     * // 是: 判断是否闭合
     * //   是: 找出能排除的数
     * //   否: 取下一条强链继续
     * // 否: 取下一条强链继续
     */
    private static LinkedList<Cell> findClosedChain(LinkedList<Cell> chain, List<StrongLink> rest) {
        if (found) return null;
        Iterator<StrongLink> itr = rest.iterator();
        while (itr.hasNext()) {
            String flag = "0";
            StrongLink next = itr.next();

            if (chain.contains(next.c2)) {
                itr.remove();
                continue;
            }

            if (isLinked(chain, next.c1)) {
                flag = "12";
            } else if (isLinked(chain, next.c2)) {
                flag = "21";
            }

            if (!"0".equals(flag)) {
                // linked
                linked++;
                LinkedList<Cell> chain2 = new LinkedList<>(chain);
                ArrayList<StrongLink> rest2 = new ArrayList<>(rest);
                if ("12".equals(flag)) {
                    chain2.add(next.c1);
                    chain2.add(next.c2);
                } else if ("21".equals(flag)) {
                    chain2.add(next.c2);
                    chain2.add(next.c1);
                }

                List<Cell> excludeList = isClosed(chain2);
                if (!excludeList.isEmpty()) {
                    found = true;
                    Debug.printChain(chain2);
                    Debug.printExcludeList(excludeList);
//                    Main.paintChain(chain2);
//                    Main.paintExcludeList(excludeList);
                    System.out.println(linked);
                    return chain2;
                } else {
                    itr.remove();
                    findClosedChain(chain2, rest2);
                }
            }
        }
        return null;
    }

    private static List<Cell> isClosed(LinkedList<Cell> chain) {
        Cell head = chain.getFirst();
        Cell tail = chain.getLast();
        List<Cell> excludeList = new ArrayList<>();

        if (head.overlap(tail)) {
            if (head.num == tail.num) {
                // nice loop
                Cell e = new Cell(head);
                e.excludes = new ArrayList<>(head.candidates);
                e.excludes.remove(head.num);
                excludeList.add(e);
                head.candidates.clear();
                head.candidates.add(head.num);
//                Debug.println(String.format("make: r%sc%s-%s", head.r + 1, head.c + 1, head.num));
            } else {
                // loop
                if (head.candidates.size() > 2) {
                    List<Integer> retain = Arrays.asList(head.num, tail.num);
                    Cell e = new Cell(head);
                    e.excludes = new ArrayList<>(head.candidates);
                    e.excludes.removeAll(retain);
                    excludeList.add(e);
                    head.candidates.retainAll(retain);
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
                        cell.candidates.remove(new Integer(head.num));
//                        Debug.println(String.format("exclude: r%sc%s-%s", cell.r + 1, cell.c + 1, head.num));
                    }
                }
            } else if (head.r == tail.r || head.c == tail.c || head.sameBlock(tail)) {
                // head num not equals to tail num
                if (head.candidates.contains(tail.num)) {
                    Cell e = new Cell(head);
                    e.excludes = Collections.singletonList(tail.num);
                    excludeList.add(e);
                    head.candidates.remove(new Integer(tail.num));
//                    Debug.println(String.format("exclude: r%sc%s-%s", head.r + 1, head.c + 1, tail.num));
                }
                if (tail.candidates.contains(head.num)) {
                    Cell e = new Cell(tail);
                    e.excludes = Collections.singletonList(head.num);
                    excludeList.add(e);
                    tail.candidates.remove(new Integer(head.num));
//                    Debug.println(String.format("exclude: r%sc%s-%s", tail.r + 1, tail.c + 1, head.num));
                }
            }
        }
        return excludeList;
    }

    private static boolean isLinked(LinkedList<Cell> chain, Cell next) {
        Cell prev = chain.getLast();
        if (prev.overlap(next) && prev.num != next.num) {
            return true;
        }
        if (prev.num == next.num && !chain.contains(next) &&
                (prev.r == next.r || prev.c == next.c || prev.sameBlock(next))) {
            return true;
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
        for (int num = 1; num <= 9; num++) {
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
}