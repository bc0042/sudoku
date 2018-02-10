package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 1/31/18.
 */
public class ChainSolver {
    static int maxSteps = 2 * 2;
    static int alsSize = 2;
    static boolean alsEnable = false;
    private static ListMap linkMap = new ListMap();

    public static boolean findChain() {
        findLinkMap();
        List<StrongLink> allLinks = new ArrayList<>();
        for (Map.Entry<Integer, Set<StrongLink>> entry : linkMap.entrySet()) {
            allLinks.addAll(entry.getValue());
        }
        List<int[]> combinations = CombineUtil.getCombinations(allLinks.size(), 2);
        for (int[] c : combinations) {
            StrongLink link1 = allLinks.get(c[0]);
            StrongLink link2 = allLinks.get(c[1]);
            List<LinkNode[]> orders = link1.getOrders(link2);
            for (LinkNode[] headAndTail : orders) {
//                Debug.debug(Arrays.asList(headAndTail));
                if (headAndTail[0].close(headAndTail[3])) {
                    List<LinkNode> chain = findSteps(headAndTail);
                    if (chain != null) {
                        Debug.printChain(chain);
                        Main.paintChain(chain);
                        exclude(chain);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<LinkNode> findSteps(LinkNode[] nodes) {
        if (nodes[1].link(nodes[2])) {
            // 2 strong links
            return Arrays.asList(nodes);
        } else {
            LinkedList<LinkNode> headNodes = new LinkedList<>();
            headNodes.add(nodes[0]);
            headNodes.add(nodes[1]);
            LinkedList<LinkNode> tailNodes = new LinkedList<>();
            tailNodes.add(nodes[2]);
            tailNodes.add(nodes[3]);
            return findSteps(headNodes, tailNodes);
        }
    }

    private static LinkedList<LinkNode> findSteps(LinkedList<LinkNode> headNodes, LinkedList<LinkNode> tailNodes) {
        Debug.debug(headNodes, tailNodes);
        while (true) {
            Set<StrongLink> possibleLinks = linkMap.getPossibleLinks(headNodes.getLast());
            for (StrongLink next : possibleLinks) {
                if (headNodes.size() >= maxSteps) {
                    return null;
                }
                if (headNodes.contains(next.node1) || headNodes.contains(next.node2)) {
                    continue;
                }

                if (headNodes.getLast().link(next.node1)) {
                    headNodes.add(next.node1);
                    headNodes.add(next.node2);
                } else if (headNodes.getLast().link(next.node2)) {
                    headNodes.add(next.node2);
                    headNodes.add(next.node1);
                } else {
                    continue;
                }

                if (headNodes.getLast().link(tailNodes.getFirst())) {
                    headNodes.addAll(tailNodes);
                    return headNodes;
                } else {
                    LinkedList<LinkNode> headNodes2 = new LinkedList<>(headNodes);
                    LinkedList<LinkNode> steps = findSteps(headNodes2, tailNodes);
                    if (steps != null) {
                        return steps;
                    } else {
                        headNodes.removeLast();
                        headNodes.removeLast();
                    }
                }
            }
            return null;
        }
    }

    private static void findLinkMap() {
        linkMap.clear();
        for (int i = 1; i <= Cell.maxNum; i++) {
            Set<StrongLink> set = findStrongLinksByNum(i);
            linkMap.add(i, set);
        }
        // xy links
        Set<StrongLink> xyLinks = findXYLinks();
        linkMap.addAll(xyLinks);
        // almost locked set
        if (alsEnable) {
            Set<StrongLink> als = findALS();
            linkMap.addAll(als);
        }

        Debug.println("link size: " + linkMap.size());
        Debug.println("max steps: " + maxSteps);
    }

    private static Set<StrongLink> findALS() {
        Set<StrongLink> set = new HashSet<>();
        for (int i = 0; i < Board.rows; i++) {
            List<Cell> row = new ArrayList<>();
            List<Cell> col = new ArrayList<>();
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.size() > 1) {
                    row.add(cell);
                }
                Cell cell2 = Board.cells[j][i];
                if (cell2.candidates.size() > 1) {
                    col.add(cell2);
                }
            }
            Set<StrongLink> als = findALS(row);
            Set<StrongLink> als2 = findALS(col);
            set.addAll(als);
            set.addAll(als2);
        }
        return set;
    }

    private static Set<StrongLink> findALS(List<Cell> list) {
        Set<StrongLink> list2 = new HashSet<>();
        List<int[]> combinations = CombineUtil.getCombinations(list.size(), alsSize);
        for (int[] c : combinations) {
            Set<Cell> cells = new HashSet<>();
            Set<Integer> cans = new HashSet<>();
            for (int i = 0; i < alsSize; i++) {
                Cell cell = list.get(c[i]);
                cells.add(cell);
                cans.addAll(cell.candidates);
            }
            if (cans.size() == alsSize + 1) {
                List<StrongLink> strongLinks = createStrongLinks(cans, cells);
                list2.addAll(strongLinks);
            }
        }
        return list2;
    }

    private static List<StrongLink> createStrongLinks(Set<Integer> cans, Set<Cell> cells) {
        List<StrongLink> links = new ArrayList<>();
        ArrayList<Integer> cans2 = new ArrayList<>(cans);
        List<int[]> combinations = CombineUtil.getCombinations(cans2.size(), 2);
        for (int[] c : combinations) {
            Integer c1 = cans2.get(c[0]);
            Integer c2 = cans2.get(c[1]);
            LinkNode node1 = new LinkNode();
            LinkNode node2 = new LinkNode();
            for (Cell cell : cells) {
                if (cell.candidates.contains(c1)) {
                    Cell cell1 = new Cell(cell);
                    cell1.linkNum = c1;
                    node1.cells.add(cell1);
                }
                if (cell.candidates.contains(c2)) {
                    Cell cell2 = new Cell(cell);
                    cell2.linkNum = c2;
                    node2.cells.add(cell2);
                }
            }
            StrongLink strongLink = new StrongLink(node1, node2);
            links.add(strongLink);
        }
        return links;
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
            } else if (entry.getValue().size() > 2) {
                StrongLink groupedSL = groupedStrongLink2(entry.getValue(), num);
                if (groupedSL != null) {
                    strongLinks.add(groupedSL);
                }
            }
        }
        return strongLinks;
    }

    public static Set<StrongLink> findXYLinks() {
        Set<StrongLink> set = new HashSet<>();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.size() == 2) {
                    set.add(new StrongLink(cell));
                }
            }
        }
        return set;
    }

    private static StrongLink groupedStrongLink2(List<Cell> list, int num) {
        Map<Integer, List<Cell>> map = list.stream().collect(Collectors.groupingBy(cell -> cell.r));
        if (map.size() == 2) {
            Iterator<Map.Entry<Integer, List<Cell>>> itr = map.entrySet().iterator();
            return new StrongLink(itr.next().getValue(), itr.next().getValue(), num);
        }
        map = list.stream().collect(Collectors.groupingBy(cell -> cell.c));
        if (map.size() == 2) {
            Iterator<Map.Entry<Integer, List<Cell>>> itr = map.entrySet().iterator();
            return new StrongLink(itr.next().getValue(), itr.next().getValue(), num);
        }
        return null;
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

    private static void exclude(List<LinkNode> chain) {
        List<Cell> excludes = chain.get(0).exclude(chain.get(chain.size() - 1));
        Main.paintExcludes(excludes);
        for (Cell cell : excludes) {
            Board.cells[cell.r][cell.c].candidates.removeAll(cell.excludes);
            Debug.println(String.format("exclude: r%dc%d-%s", cell.r + 1, cell.c + 1, cell.excludes));
        }

    }
}
