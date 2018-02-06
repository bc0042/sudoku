package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class Solver {
    static List<Cell> excludeList = new ArrayList<>();
    private static int maxSteps = 6 * 2;
    private static ListMap listMap;
    private static int link_node1 = 1;
    private static int link_node2 = 2;

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
        for (Point p : cell.getAffectPoints()) {
            Cell cell1 = Board.cells[p.x][p.y];
            if (cell1.candidates.remove(num) && cell1.candidates.size() == 1) {
                // if only one candidate, check again
                excludeByNum(cell1);
            }
        }
    }


    public static Set<StrongLink> findStrongLinksByNum(int num) {
        List<Cell> list = getCellsContainsNum(num);
        Set<StrongLink> strongLinks = new HashSet<>();
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
//            } else if (entry.getValue().size() > 2) {
//                StrongLink groupedSL = groupedStrongLink(entry.getValue(), num);
//                if (groupedSL != null) {
//                    strongLinks.add(groupedSL);
//                }
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

    public static void findChain() {
        findStrongLinks();
        findFirstChain();
//        exclude();
//        checkCandidates();
    }

    public static void findFirstChain() {
        for (Map.Entry<Integer, Collection<StrongLink>> entry : listMap.entrySet()) {
            Collection<StrongLink> links = entry.getValue();
            for (StrongLink link : links) {

                LinkedList<StrongLink> takenLinks = new LinkedList<>();
                takenLinks.add(link);
                LinkedList<LinkNode> steps = new LinkedList<>();

                //12
//                LinkedList<LinkNode> steps = new LinkedList<>();
                steps.add(link.node1);
                steps.add(link.node2);
                boolean stop = findNextStep(takenLinks, steps);
                if (stop) {
                    return;
                } else {
                    //21
                    steps = new LinkedList<>();
                    steps.add(link.node2);
                    steps.add(link.node1);
                    if (findNextStep(takenLinks, steps)) {
                        return;
                    }
                }
            }
        }
    }

    private static boolean findNextStep(LinkedList<StrongLink> takenLinks, LinkedList<LinkNode> steps) {
        if (steps.size() > maxSteps) return false;

        // debug
        if (steps.size() > 5) {
            if (steps.get(0).getFirstCell().linkNum == 7
                    && steps.get(0).getFirstCell().r == 6 - 1
                    && steps.get(1).getFirstCell().linkNum == 7
                    && steps.get(2).getFirstCell().linkNum == 7
                    && steps.get(3).getFirstCell().linkNum == 5
                    && steps.get(4).getFirstCell().linkNum == 5
                    && steps.get(5).getFirstCell().linkNum == 9
                    && steps.get(5).getFirstCell().r == 9 - 1
                    ) {
                System.out.println();
            }
        }

        if (steps.getFirst().close(steps.getLast())) {
            Debug.printSteps(steps);
            Main.paintChain(steps);
            return true;
        }

        LinkNode last = steps.getLast();
        int linkNum = last.getFirstCell().linkNum;

        // link by number
        Collection<StrongLink> links = listMap.get(linkNum);
        for (StrongLink next : links) {
            if (takenLinks.contains(next)) continue;
            if (last.link(next.node1) && !next.node1.overlap(steps) && !next.node2.overlap(steps)) {
                return findNextStep2(takenLinks, steps, next, link_node1);
            }
            if (last.link(next.node2) && !next.node1.overlap(steps) && !next.node2.overlap(steps)) {
                return findNextStep2(takenLinks, steps, next, link_node2);
            }
        }

        // link by cell
        List<StrongLink> overlap = listMap.getOverlap(last);
        for (StrongLink next : overlap) {
            if (takenLinks.contains(next)) continue;
            if (last.getFirstCell().linkNum != next.node1.getFirstCell().linkNum && next.node1.isSingle()
                    && last.getFirstCell().overlap(next.node1.getFirstCell())) {
                return findNextStep2(takenLinks, steps, next, link_node1);
            }
            if (last.getFirstCell().linkNum != next.node2.getFirstCell().linkNum && next.node2.isSingle()
                    && last.getFirstCell().overlap(next.node2.getFirstCell())) {
                return findNextStep2(takenLinks, steps, next, link_node2);
            }
        }

        return false;
    }

    private static boolean findNextStep2(LinkedList<StrongLink> takenLinks, LinkedList<LinkNode> steps, StrongLink next, int type) {
        LinkedList<StrongLink> takenLinks2 = new LinkedList<>(takenLinks);
        LinkedList<LinkNode> steps2 = new LinkedList<>(steps);
        takenLinks2.add(next);
        if(link_node1 == type){
            steps2.add(next.node1);
            steps2.add(next.node2);
        }else if(link_node2 == type){
            steps2.add(next.node2);
            steps2.add(next.node1);
        }
        boolean stop = findNextStep(takenLinks2, steps2);
        if(stop){
            return true;
        }else{
            steps2.removeLast();
            steps2.removeLast();
//            takenLinks2 = new LinkedList<>(takenLinks2);
//            takenLinks2.removeLast();
            return findNextStep(takenLinks, steps2);
        }
    }


    private static void findStrongLinks() {
        listMap = new ListMap();
        for (int i = 1; i <= Cell.maxNum; i++) {
            Set<StrongLink> set = Solver.findStrongLinksByNum(i);
            listMap.add(i, set);
        }

        // xy links
        List<StrongLink> xyLinks = Solver.findXYLinks();
        for (StrongLink link : xyLinks) {
            int linkNum1 = link.node1.getFirstCell().linkNum;
            int linkNum2 = link.node2.getFirstCell().linkNum;
            listMap.add(linkNum1, Collections.singletonList(link));
            listMap.add(linkNum2, Collections.singletonList(link));
        }
        Debug.println("link size: " + listMap.size());
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
//        if (steps != null && steps.size() > 2) {
//            Debug.printSteps(steps);
//        }
        if (excludeList != null) {
            Debug.printExcludeList(excludeList);
            for (Cell cell : excludeList) {
                cell.candidates.removeAll(cell.excludes);
            }
        }
    }
}
