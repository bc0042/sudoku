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


//        for (int i = 0; i < strongLinks.size(); i++) {
//            for (int j = 0; j < strongLinks.size(); j++) {
//                if (i == j) continue;
//                StrongLink link1 = strongLinks.get(i);
//                StrongLink link2 = strongLinks.get(j);
//                ArrayList<StrongLink> restLinks = new ArrayList<>(strongLinks);
//                restLinks.remove(link1);
//                restLinks.remove(link2);
//
//                List<LinkedList<LinkNode>> arrange = link1.arrange(link2);
//                for (LinkedList<LinkNode> endPoints : arrange) {
//                    List<LinkNode> steps = new ArrayList<>();
//                    if (endPoints.getFirst().close(endPoints.getLast()) && findNextStep(endPoints, restLinks, steps)) {
//                        // find excludes
//                        return;
//                    }
//                }
//            }
//        }
    }

    private static boolean findNextStep(LinkedList<StrongLink> takenLinks, LinkedList<LinkNode> steps) {
        if (steps.size() > maxSteps) return false;

        if (steps.getFirst().close(steps.getLast())) {
            Debug.printSteps(steps);
            return true;
        }

        LinkNode last = steps.getLast();
        int linkNum = last.getFirstCell().linkNum;

        // link by number
        Collection<StrongLink> links = listMap.get(linkNum);
        for (StrongLink next : links) {
            if(takenLinks.contains(next)) continue;
            if (last.link(next.node1) && !next.node2.overlap(steps)) {
                LinkedList<StrongLink> takenLinks2 = new LinkedList<>(takenLinks);
                LinkedList<LinkNode> steps2 = new LinkedList<>(steps);
                takenLinks2.add(next);
                steps2.add(next.node1);
                steps2.add(next.node2);
                return findNextStep(takenLinks2, steps2);
            }
            if (last.link(next.node2) && !next.node1.overlap(steps)) {
                LinkedList<StrongLink> takenLinks2 = new LinkedList<>(takenLinks);
                LinkedList<LinkNode> steps2 = new LinkedList<>(steps);
                takenLinks2.add(next);
                steps2.add(next.node2);
                steps2.add(next.node1);
                return findNextStep(takenLinks2, steps2);
            }
        }

        // link by cell
        List<StrongLink> overlap = listMap.getOverlap(last);


        return false;
    }

    /**
     *  // debug
     if (steps.size() > 1) {
     if (steps.get(0).getFirstCell().linkNum == 7
     //                    && steps.get(0).getFirstCell().r == 6 - 1
     && steps.get(1).getFirstCell().linkNum == 7
     //                    && steps.get(2).getFirstCell().linkNum == 7
     //                    && steps.get(3).getFirstCell().linkNum == 5
     //                    && steps.get(4).getFirstCell().linkNum == 5
     //                    && steps.get(5).getFirstCell().linkNum == 9
     //                    && steps.get(5).getFirstCell().r == 9 - 1
     ) {
     //                System.out.println();
     }
     }
     *
     *
     */

    private static boolean findNextStep(List<LinkNode> endPoints, ArrayList<StrongLink> restLinks, List<LinkNode> steps) {



        if (steps.size() > maxSteps) return false;

        if (steps.isEmpty()) {
            steps.add(endPoints.get(0));
            steps.add(endPoints.get(1));
        }

        for (int i = 0; i < restLinks.size(); i++) {
            StrongLink next = restLinks.get(i);
            // steps cannot contains the next node
            if (steps.contains(next.node1) || steps.contains(next.node2)) continue;

            boolean linkHalf = false;
            LinkedList<LinkNode> steps2 = new LinkedList<>(steps);
            ArrayList<StrongLink> rest2 = new ArrayList<>(restLinks);
            if (steps2.getLast().link(next.node1)) {
                steps2.add(next.node1);
                steps2.add(next.node2);
                linkHalf = true;
            } else if (steps2.getLast().link(next.node2)) {
                steps2.add(next.node2);
                steps2.add(next.node1);
                linkHalf = true;
            }


            if (linkHalf && steps2.getLast().link(endPoints.get(2))) {
                steps2.add(endPoints.get(2));
                steps2.add(endPoints.get(3));
                Debug.printSteps(steps2);
//                Solver.steps = steps2;
                return true;
            } else if (linkHalf) {
                rest2.remove(i);
                boolean done = findNextStep(endPoints, rest2, steps2);
                if (done) {
                    return done;
                } else {
                    steps2.removeLast();
                    steps2.removeLast();
//                    rest2 = new ArrayList<>(restLinks);
                    return findNextStep(endPoints, rest2, steps2);
                }
            }

        }
        return false;
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
