package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class Eliminator {
    static List<LinkPoint> chain = new ArrayList<>();
    static List<Cell> excludeList = new ArrayList<>();
    private static String p1_linked = "c1-linked";
    private static String p2_linked = "c2-linked";

    public static void checkCandidates() {
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
        for (Point p : cell.getAffectCells()) {
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
            } else if (entry.getValue().size() > 2) {
                StrongLink groupedSL = groupedStrongLink(entry.getValue(), num);
                if (groupedSL != null) {
                    strongLinks.add(groupedSL);
                }
            }
        }
        // strong link in cols
        Map<Integer, List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
            if (entry.getValue().size() == 2) {
                StrongLink sl = new StrongLink(entry.getValue(), num);
                strongLinks.add(sl);
            } else if (entry.getValue().size() > 2) {
                StrongLink groupedSL = groupedStrongLink(entry.getValue(), num);
                if (groupedSL != null) {
                    strongLinks.add(groupedSL);
                }
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

    private static StrongLink groupedStrongLink(List<Cell> list, int num) {
        Map<Point, List<Cell>> map = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        if (map.size() == 2) {
            Iterator<Map.Entry<Point, List<Cell>>> itr = map.entrySet().iterator();
            return new StrongLink(itr.next().getValue(), itr.next().getValue(), num);
        }
        return null;
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
        checkCandidates();
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

//        Debug.println("links: "+strongLinks.size());
//        for (StrongLink link : strongLinks) {
//            if(link.list1.size()>1 || link.list2.size()>1){
//                Debug.println(link);
//            }
//        }
//        Debug.exit();

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


                List<LinkPoint> cells = Arrays.asList(link1.p1, link1.p2, link2.p1, link2.p2);
                for (String order : orders) {
                    List<LinkPoint> cells2 = new ArrayList<>();
                    for (int k = 0; k < order.length(); k++) {
                        char ch = order.charAt(k);
                        cells2.add(cells.get(Integer.parseInt("" + ch) - 1));
                    }
                    checkHeadAndTail(cells2, rest);
                    if (!excludeList.isEmpty()) return;
                }

            }
        }
    }

    private static void checkHeadAndTail(List<LinkPoint> cells, ArrayList<StrongLink> restLinks) {
        LinkedList<LinkPoint> headPoints = new LinkedList<>();
        headPoints.add(cells.get(0));
        headPoints.add(cells.get(1));
        LinkedList<LinkPoint> tailPoints = new LinkedList<>();
        tailPoints.add(cells.get(2));
        tailPoints.add(cells.get(3));
        List<Cell> excludes = getExcludes(headPoints.getFirst(), tailPoints.getLast());
        if (!excludes.isEmpty() && linkUp(headPoints, tailPoints, restLinks)) {
            excludeList = excludes;
        }
    }

    private static boolean linkUp(LinkedList<LinkPoint> headPoints, LinkedList<LinkPoint> tailPoints, ArrayList<StrongLink> restLinks) {
        // chain cannot contains the next link
        if (headPoints.contains(tailPoints.getFirst()) || headPoints.contains(tailPoints.getLast())) return false;

        //debug
//        if (head.getFirst().linkNum == 7
//                && head.getLast().linkNum == 7
//                && head.getFirst().r == 2 - 1
//                && head.getFirst().c == 7 - 1
//
//                && head.getLast().r == 2 - 1
//                && head.getLast().c == 4 - 1
//
//                && tail.getFirst().linkNum == 2
//                && tail.getLast().linkNum == 1
//                ) {
//            Debug.println();
//        }

        if (headPoints.size() == 2) {
            if (isLinked(headPoints.getLast(), tailPoints.getFirst())) {
                headPoints.addAll(tailPoints);
                chain = headPoints;
                return true;
            }
        }

        for (int i = 0; i < restLinks.size(); i++) {
            StrongLink next = restLinks.get(i);
            // chain cannot contains the next link
            if (headPoints.contains(next.p1) || headPoints.contains(next.p2)) continue;

            LinkedList<LinkPoint> headPoints2 = new LinkedList<>(headPoints);
            ArrayList<StrongLink> rest2 = new ArrayList<>(restLinks);
            if (isLinked(headPoints.getLast(), next.p1)) {
                return linkUpRest(headPoints2, tailPoints, rest2, i, p1_linked);
            } else if (isLinked(headPoints.getLast(), next.p2)) {
                return linkUpRest(headPoints2, tailPoints, rest2, i, p2_linked);
            }
        }
        return false;
    }

    private static boolean linkUpRest(LinkedList<LinkPoint> headPoints2, LinkedList<LinkPoint> tailPoints, ArrayList<StrongLink> rest2, int i, String type) {
        boolean linked;
        StrongLink next = rest2.get(i);
        if (p1_linked.equals(type)) {
            headPoints2.add(next.p1);
            headPoints2.add(next.p2);
        } else if (p2_linked.equals(type)) {
            headPoints2.add(next.p2);
            headPoints2.add(next.p1);
        }
        if (isLinked(headPoints2.getLast(), tailPoints.getFirst())) {
            headPoints2.addAll(tailPoints);
            chain = headPoints2;
            return true;
        } else {
            rest2.remove(i);
            linked = linkUp(headPoints2, tailPoints, rest2);
        }
        if (linked) {
            return true;
        } else {
            headPoints2.removeLast();
            headPoints2.removeLast();
            return linkUp(headPoints2, tailPoints, rest2);
        }
    }

    private static boolean isLinked(LinkPoint p1, LinkPoint p2) {
        if (p1.cells.size() == 1 && p2.cells.size() == 1) {
            Cell prev = p1.cells.get(0);
            Cell next = p2.cells.get(0);
            if (prev.overlap(next)) {
                if (prev.linkNum != next.linkNum) {
                    return true;
                }
            } else {
                if (prev.linkNum == next.linkNum &&
                        (prev.r == next.r || prev.c == next.c || prev.sameBlock(next))) {
                    return true;
                }
            }
        } else {
            // grouped links
            boolean sameNum = p1.cells.get(0).linkNum == p2.cells.get(0).linkNum;
            int row = p1.sameRow(p2);
            if (row != 0 && sameNum) {
                return true;
            }
            int col = p1.sameCol(p2);
            if (col != 0 && sameNum) {
                return true;
            }
            Point block = p1.sameBlock(p2);
            if (block != null && sameNum) {
                return true;
            }
        }
        return false;
    }

    private static List<Cell> getExcludes(LinkPoint p1, LinkPoint p2) {
        if (p1.cells.size() == 1 && p2.cells.size() == 1) {
            return getExcludes(p1.cells.get(0), p2.cells.get(0));
        } else {
            // grouped links
            List<Cell> excludes = new ArrayList<>();
            int linkNum = p1.cells.get(0).linkNum;
            int linkNum2 = p2.cells.get(0).linkNum;
            if (linkNum != linkNum2) return excludes;

            int row = p1.sameRow(p2);
            int col = p1.sameCol(p2);
            if (row != 0) {
                // same row, exclude cols in the links
                List<Integer> cols = new ArrayList<>();
                for (Cell cell : p1.addAll(p2)) {
                    cols.add(cell.c);
                }
                for (int i = 0; i < Board.cols; i++) {
                    Cell e = Board.cells[row][i];
                    if (!cols.contains(i) && e.candidates.contains(linkNum)) {
                        e.excludes = Collections.singletonList(linkNum);
                        excludes.add(e);
                    }
                }
            } else if (col != 0) {
                // same col, exclude rows in the links
                List<Integer> rows = new ArrayList<>();
                for (Cell cell : p1.addAll(p2)) {
                    rows.add(cell.r);
                }
                for (int i = 0; i < Board.rows; i++) {
                    Cell e = Board.cells[i][col];
                    if (!rows.contains(i) && e.candidates.contains(linkNum)) {
                        e.excludes = Collections.singletonList(linkNum);
                        excludes.add(e);
                    }
                }
            } else {
                if(p1.isSingle()){
                    List<Point> affectCells = p1.getSingle().getAffectCells();
                    List<Point> affectCells2 = p2.getAffectBlock();
                    if (affectCells2.retainAll(affectCells)) {
                        for (Point point : affectCells2) {
                            Cell e = Board.cells[point.x][point.y];
                            if (e.candidates.contains(linkNum)) {
                                e.excludes = Collections.singletonList(linkNum);
                                excludes.add(e);
                            }
                        }
                    }
                }
                if(p2.isSingle()){
                    List<Point> affectCells = p2.getSingle().getAffectCells();
                    List<Point> affectCells2 = p1.getAffectBlock();
                    if (affectCells2.retainAll(affectCells)) {
                        for (Point point : affectCells2) {
                            Cell e = Board.cells[point.x][point.y];
                            if (e.candidates.contains(linkNum)) {
                                e.excludes = Collections.singletonList(linkNum);
                                excludes.add(e);
                            }
                        }
                    }
                }
            }
            return excludes;
        }
    }

    private static List<Cell> getExcludes(Cell head, Cell tail) {
        List<Cell> excludeList = new ArrayList<>();
        if (head.overlap(tail)) {
            if (head.linkNum == tail.linkNum) {
                // nice loop
                Cell e = new Cell(head);
                e.excludes = new ArrayList<>(head.candidates);
                e.excludes.remove(new Integer(head.linkNum));
                excludeList.add(e);
//                Debug.println(String.format("make: r%sc%s-%s", head.r + 1, head.c + 1, head.linkNum));
            } else {
                // loop
                if (head.candidates.size() > 2) {
                    List<Integer> retain = Arrays.asList(head.linkNum, tail.linkNum);
                    Cell e = new Cell(head);
                    e.excludes = new ArrayList<>(head.candidates);
                    e.excludes.removeAll(retain);
                    excludeList.add(e);
//                    Debug.println(String.format("retain: r%sc%s-%s", head.r + 1, head.c + 1, head.linkNum + "," + tail.linkNum));
                }
            }
        } else {
            // not the same cell
            if (head.linkNum == tail.linkNum) {
                List<Point> list1 = head.getAffectCells();
                List<Point> list2 = tail.getAffectCells();
                list1.retainAll(list2);
                for (Point p : list1) {
                    Cell cell = Board.cells[p.x][p.y];
                    if (cell.candidates.contains(head.linkNum)) {
                        Cell e = new Cell(cell);
                        e.excludes = Collections.singletonList(head.linkNum);
                        excludeList.add(e);
//                        Debug.println(String.format("exclude: r%sc%s-%s", cell.r + 1, cell.c + 1, head.linkNum));
                    }
                }
            } else if (head.r == tail.r || head.c == tail.c || head.sameBlock(tail)) {
                // head linkNum not equals to tail linkNum
                if (head.candidates.contains(tail.linkNum)) {
                    Cell e = new Cell(head);
                    e.excludes = Collections.singletonList(tail.linkNum);
                    excludeList.add(e);
//                    Debug.println(String.format("exclude: r%sc%s-%s", head.r + 1, head.c + 1, tail.linkNum));
                }
                if (tail.candidates.contains(head.linkNum)) {
                    Cell e = new Cell(tail);
                    e.excludes = Collections.singletonList(head.linkNum);
                    excludeList.add(e);
//                    Debug.println(String.format("exclude: r%sc%s-%s", tail.r + 1, tail.c + 1, head.linkNum));
                }
            }
        }
        return excludeList;
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
                    cell.linkNum = num;
                    singles.add(cell);
                }
            }
            Map<Integer, List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
            for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, col", num, cell.r + 1, cell.c + 1));
                    cell.linkNum = num;
                    singles.add(cell);
                }
            }
            Map<Point, List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                    cell -> new Point(cell.r / 3, cell.c / 3)));
            for (Map.Entry<Point, List<Cell>> entry : map3.entrySet()) {
                if (entry.getValue().size() == 1) {
                    Cell cell = entry.getValue().get(0);
                    Debug.println(String.format("hidden single: %s, r%sc%s, block", num, cell.r + 1, cell.c + 1));
                    cell.linkNum = num;
                    singles.add(cell);
                }
            }
        }
        for (Cell single : singles) {
            single.candidates.clear();
            single.candidates.add(single.linkNum);
        }
        checkCandidates();
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
