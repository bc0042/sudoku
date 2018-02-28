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
    static boolean alsEnabled = true;
    static ListMap linkMap = new ListMap();

    public static boolean findChain() {
        createLinkMap();
        Set<StrongLink> set = linkMap.getAllLinks();
        List<StrongLink> allLinks = new ArrayList<>(set);
        List<int[]> combinations = CombineUtil.getCombinations(allLinks.size(), 2);
        for (int[] c : combinations) {
            StrongLink link1 = allLinks.get(c[0]);
            StrongLink link2 = allLinks.get(c[1]);
            List<LinkNode[]> orders = link1.getOrders(link2);
            for (LinkNode[] headAndTail : orders) {
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
        if (nodes[1].weakLink(nodes[2])) {
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
        Set<StrongLink> possibleLinks = linkMap.getPossibleLinks(headNodes.getLast());
        for (StrongLink next : possibleLinks) {
            if (headNodes.size() >= maxSteps) {
                return null;
            }
            if (headNodes.contains(next.node1) || headNodes.contains(next.node2)) {
                continue;
            }

            if (headNodes.getLast().weakLink(next.node1)) {
                headNodes.add(next.node1);
                headNodes.add(next.node2);
            } else if (headNodes.getLast().weakLink(next.node2)) {
                headNodes.add(next.node2);
                headNodes.add(next.node1);
            } else {
                continue;
            }

            if (headNodes.getLast().weakLink(tailNodes.getFirst())) {
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

    private static void createLinkMap() {
        linkMap.clear();
        for (int i = 1; i <= Cell.maxNum; i++) {
            Set<StrongLink> set = findStrongLinksByNum(i);
            linkMap.add(i, set);
        }

        // xy links
        Set<StrongLink> xyLinks = findXYLinks();
        linkMap.addAll(xyLinks);

        // almost locked set
        if (alsEnabled) {
            Set<StrongLink> als = findALS();
            linkMap.addAll(als);
        }

        Debug.println("strong link size: " + linkMap.size());
        Debug.println(String.format("maxSteps=%d, alsSize=%d, alsEnabled=%s", maxSteps, alsSize, alsEnabled));
    }

    private static Set<StrongLink> findALS() {
        Set<StrongLink> set = new HashSet<>();
        List<Cell> cells = Board.getCandidateCells();
        Map<Integer, List<Cell>> rows = cells.stream().collect(Collectors.groupingBy(c -> c.r));
        for (Map.Entry<Integer, List<Cell>> entry : rows.entrySet()) {
            List<Cell> row = entry.getValue();
            set.addAll(findALS(row));
        }
        Map<Integer, List<Cell>> cols = cells.stream().collect(Collectors.groupingBy(c -> c.c));
        for (Map.Entry<Integer, List<Cell>> entry : cols.entrySet()) {
            List<Cell> col = entry.getValue();
            set.addAll(findALS(col));
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
                List<StrongLink> strongLinks = createALSLinks(cans, cells);
                list2.addAll(strongLinks);
            }
        }
        return list2;
    }

    private static List<StrongLink> createALSLinks(Set<Integer> cans, Set<Cell> cells) {
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

    public static Set<StrongLink> findXYLinks() {
        Set<StrongLink> set = new HashSet<>();
        List<Cell> cells = Board.getCandidateCells();
        cells.removeIf(c -> c.candidates.size() != 2);
        for (Cell cell : cells) {
            set.add(new StrongLink(cell));
        }
        return set;
    }

    public static Set<StrongLink> findStrongLinksByNum(int num) {
        List<Cell> list = SimpleSolver.getCellsContainsNum(num);
        Set<StrongLink> strongLinks = new HashSet<>();
        // strong weakLink in rows
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
        // strong weakLink in cols
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
        // strong weakLink in blocks
        Map<Point, List<Cell>> map3 = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        for (Map.Entry<Point, List<Cell>> entry : map3.entrySet()) {
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

    private static StrongLink groupedStrongLink(List<Cell> list, int num) {
        Map<Point, List<Cell>> map = list.stream().collect(Collectors.groupingBy(
                cell -> new Point(cell.r / 3, cell.c / 3)));
        if (map.size() == 2) {
            Iterator<Map.Entry<Point, List<Cell>>> itr = map.entrySet().iterator();
            return new StrongLink(itr.next().getValue(), itr.next().getValue(), num);
        }
        return null;
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

    private static void exclude(List<LinkNode> chain) {
        List<Cell> excludes = chain.get(0).exclude(chain.get(chain.size() - 1));
        Main.paintExcludes(excludes);
        for (Cell cell : excludes) {
            Board.cells[cell.r][cell.c].candidates.removeAll(cell.excludes);
            Debug.println(String.format("exclude: r%dc%d-%s", cell.r + 1, cell.c + 1, cell.excludes));
        }

    }

    /**
     * start with 3 cells with the same number
     * end with 1 cell contains the same number
     * then eliminate the number in the cell of the end
     */
    public static void forcingChain() {
        Debug.println("forcing chain..");
        int n = 3;
        createLinkMap();

        List<AbstractMap.SimpleEntry<Integer, List<Cell>>> list = new ArrayList<>();
        List<Cell> cells = Board.getCandidateCells();
        Map<Integer, List<Cell>> map = cells.stream().collect(Collectors.groupingBy(c -> c.r));
        for (Map.Entry<Integer, List<Cell>> entry : map.entrySet()) {
            List<Cell> row = entry.getValue();
            Set<Integer> cans = new HashSet<>();
            for (Cell cell : row) {
                cans.addAll(cell.candidates);
            }
            for (Integer can : cans) {
                Map<Boolean, List<Cell>> collect = row.stream().collect(Collectors.groupingBy(
                        c -> c.candidates.size() > 1 && c.candidates.contains(can)));
                for (Map.Entry<Boolean, List<Cell>> entry2 : collect.entrySet()) {
                    if (entry2.getKey() && entry2.getValue().size() == n) {
                        list.add(new AbstractMap.SimpleEntry<>(can, entry2.getValue()));
                    }
                }
            }
        }

        Map<Integer, List<Cell>> map2 = cells.stream().collect(Collectors.groupingBy(c -> c.c));
        for (Map.Entry<Integer, List<Cell>> entry : map2.entrySet()) {
            List<Cell> col = entry.getValue();
            Set<Integer> cans = new HashSet<>();
            for (Cell cell : col) {
                cans.addAll(cell.candidates);
            }
            for (Integer can : cans) {
                Map<Boolean, List<Cell>> collect = col.stream().collect(Collectors.groupingBy(
                        c -> c.candidates.size() > 1 && c.candidates.contains(can)));
                for (Map.Entry<Boolean, List<Cell>> entry2 : collect.entrySet()) {
                    if (entry2.getKey() && entry2.getValue().size() == n) {
                        list.add(new AbstractMap.SimpleEntry<>(can, entry2.getValue()));
                    }
                }
            }
        }

        for (AbstractMap.SimpleEntry<Integer, List<Cell>> entry : list) {
            List<LinkedList<LinkNode>> forcingChain = findForcingChain(entry);
            if (forcingChain != null) {
                Debug.printForcingChain(forcingChain);
                Main.printForcingChain(forcingChain);
                Cell last = forcingChain.get(0).getLast().getFirstCell();
                last.candidates.remove(new Integer(last.linkNum));
                return;
            }
        }
        Debug.println("forcing chain not found..");
        maxSteps += 2;
    }

    private static List<LinkedList<LinkNode>> findForcingChain(AbstractMap.SimpleEntry<Integer, List<Cell>> entry) {
        Integer num = entry.getKey();
        List<Cell> cells = entry.getValue();
        List<Cell> destCells = Board.getCandidateCells();
        destCells.removeIf(cell -> !cell.candidates.contains(num));
        for (Cell destCell : destCells) {
            List<LinkedList<LinkNode>> chains = new ArrayList<>();
            for (Cell startCell : cells) {
                startCell.linkNum = num;
                destCell.linkNum = num;
                LinkedList<LinkNode> starts = new LinkedList<>();
                LinkedList<LinkNode> ends = new LinkedList<>();
                starts.add(new LinkNode(startCell));
                ends.add(new LinkNode(destCell));
                if (!startCell.equals(destCell)) {
                    if (starts.getFirst().weakLink(ends.getLast())) {
                        starts.addAll(ends);
                        chains.add(starts);
                        continue;
                    }
                    LinkedList<LinkNode> chain = findSteps(starts, ends);
                    if (chain != null) {
                        chains.add(chain);
                    } else {
                        break;
                    }
                }
            }
            if (chains.size() == 3) {
                return chains;
            }
        }
        return null;
    }

    public static void reset() {
        Debug.println("reset..");
        maxSteps = 6;
        alsSize = 3;
        alsEnabled = true;
        Debug.println(String.format("maxSteps=%d, alsSize=%d, alsEnabled=%s", maxSteps, alsSize, alsEnabled));
    }
}
