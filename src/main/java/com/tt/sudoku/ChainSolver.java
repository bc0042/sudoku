package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class ChainSolver {
    private static int link_node1 = 1;
    private static int link_node2 = 2;
    private static int maxSteps = 2 * 5;
    private static ListMap linkMap = new ListMap();


    public static void findChain() {
        findLinkMap();
        for (Map.Entry<Integer, Set<StrongLink>> entry : linkMap.entrySet()) {
            Collection<StrongLink> links = entry.getValue();
            for (StrongLink link : links) {
                LinkedList<StrongLink> takenLinks = new LinkedList<>();
                takenLinks.add(link);
                LinkedList<LinkNode> steps = new LinkedList<>();
                // link 1 and 2
                steps.add(link.node1);
                steps.add(link.node2);
                boolean stop = findNextStep(takenLinks, steps);
                if (stop) {
                    return;
                } else {
                    // link 2 and 1
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
//        Debug.debug(steps);

        if (steps.size() >= 4 && steps.getFirst().close(steps.getLast())) {
            Debug.printSteps(steps);
            Main.paintSteps(steps);
            List<Cell> excludes = steps.getFirst().exclude(steps.getLast());
            Main.paintExcludes(excludes);
            for (Cell cell : excludes) {
                Board.cells[cell.r][cell.c].candidates.removeAll(cell.excludes);
                Debug.println(String.format("exclude: r%dc%d-%s", cell.r + 1, cell.c + 1, cell.excludes));
            }
            return true;
        }
        LinkNode last = steps.getLast();
        int linkNum = last.getFirstCell().linkNum;
        // link by number
        Set<StrongLink> links = linkMap.get(linkNum);
        // link by cell
        Set<StrongLink> links2 = linkMap.getOverlap(last);
        links2.addAll(links);

        for (StrongLink next : links2) {
            if (takenLinks.contains(next)) continue;
            if (last.link(next.node1)) {
                return findNextStep2(takenLinks, steps, next, link_node1);
            } else if (last.link(next.node2)) {
                return findNextStep2(takenLinks, steps, next, link_node2);
            }
        }
        return false;
    }

    private static boolean findNextStep2(LinkedList<StrongLink> takenLinks, LinkedList<LinkNode> steps, StrongLink next, int type) {
        LinkedList<StrongLink> takenLinks2 = new LinkedList<>(takenLinks);
        LinkedList<LinkNode> steps2 = new LinkedList<>(steps);
        takenLinks2.add(next);
        if (link_node1 == type) {
            steps2.add(next.node1);
            steps2.add(next.node2);
        } else if (link_node2 == type) {
            steps2.add(next.node2);
            steps2.add(next.node1);
        }
        boolean stop = findNextStep(takenLinks2, steps2);
        if (stop) {
            return true;
        } else {
            steps2.removeLast();
            steps2.removeLast();
            return findNextStep(takenLinks2, steps2);
        }
    }


    private static void findLinkMap() {
        linkMap.clear();
        for (int i = 1; i <= Cell.maxNum; i++) {
            Set<StrongLink> set = findStrongLinksByNum(i);
            linkMap.add(i, set);
        }

        // xy links
        List<StrongLink> xyLinks = findXYLinks();
        for (StrongLink link : xyLinks) {
            int linkNum1 = link.node1.getFirstCell().linkNum;
            int linkNum2 = link.node2.getFirstCell().linkNum;
            linkMap.add(linkNum1, new HashSet<>(Collections.singletonList(link)));
            linkMap.add(linkNum2, new HashSet<>(Collections.singletonList(link)));
        }
        Debug.println("link size: " + linkMap.size());
    }

    public static Set<StrongLink> findStrongLinksByNum(int num) {
        java.util.List<Cell> list = SimpleSolver.getCellsContainsNum(num);
        Set<StrongLink> strongLinks = new HashSet<>();
        // strong link in rows
        Map<Integer, java.util.List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        for (Map.Entry<Integer, java.util.List<Cell>> entry : map.entrySet()) {
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
        Map<Integer, java.util.List<Cell>> map2 = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        for (Map.Entry<Integer, java.util.List<Cell>> entry : map2.entrySet()) {
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
        Map<Point, java.util.List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        for (Map.Entry<Point, java.util.List<Cell>> entry : map3.entrySet()) {
            if (entry.getValue().size() == 2) {
                StrongLink sl = new StrongLink(entry.getValue(), num);
                strongLinks.add(sl);
            }
        }
        return strongLinks;
    }

    public static java.util.List<StrongLink> findXYLinks() {
        java.util.List<StrongLink> list = new ArrayList<>();
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

    private static StrongLink groupedStrongLink(java.util.List<Cell> list, int num) {
        Map<Point, java.util.List<Cell>> map = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        if (map.size() == 2) {
            Iterator<Map.Entry<Point, java.util.List<Cell>>> itr = map.entrySet().iterator();
            return new StrongLink(itr.next().getValue(), itr.next().getValue(), num);
        }
        return null;
    }
}
