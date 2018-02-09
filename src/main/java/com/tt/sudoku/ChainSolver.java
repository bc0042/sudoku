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
                if(headAndTail[0].close(headAndTail[3])){
                    List<LinkNode> chain = findSteps(headAndTail);
                    if (chain !=null) {
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
        if(nodes[1].link(nodes[2])){
            // 2 strong links
            return Arrays.asList(nodes);
        }else{
            LinkedList<LinkNode> headNodes = new LinkedList<>();
            headNodes.add(nodes[0]);
            headNodes.add(nodes[1]);
            LinkedList<LinkNode> tailNodes = new LinkedList<>();
            tailNodes.add(nodes[2]);
            tailNodes.add(nodes[3]);
            return findSteps(headNodes, tailNodes, null);
        }
    }

    private static LinkedList<LinkNode> findSteps(LinkedList<LinkNode> headNodes, LinkedList<LinkNode> tailNodes, List<StrongLink> deadLinks) {
        if(headNodes.size()+2 >= maxSteps) return null;

        Set<StrongLink> possibleLinks = linkMap.getPossibleLinks(headNodes);
        if(deadLinks != null){
//            possibleLinks.removeAll(deadLinks); // too slow
            Iterator<StrongLink> itr = possibleLinks.iterator();
            while(itr.hasNext()){
                StrongLink next = itr.next();
                for (StrongLink deadLink : deadLinks) {
                    if(next.equals(deadLink)) itr.remove();
                }
            }
        }
        LinkedList<LinkNode> headNodes2 = new LinkedList<>(headNodes);
        for (StrongLink next : possibleLinks) {
            if(headNodes2.contains(next.node1) || headNodes2.contains(next.node2)){
                continue;
            }

            if(headNodes.getLast().link(next.node1)){
                headNodes2.add(next.node1);
                headNodes2.add(next.node2);
            }else if(headNodes.getLast().link(next.node2)){
                headNodes2.add(next.node2);
                headNodes2.add(next.node1);
            }else{
                continue;
            }

            if(headNodes2.getLast().link(tailNodes.getFirst())){
                headNodes2.addAll(tailNodes);
                return headNodes2;
            }else{
                LinkedList<LinkNode> ret = findSteps(headNodes2, tailNodes, null);
                if(ret != null){
                    return ret;
                }else{
                    LinkNode node1 = headNodes2.removeLast();
                    LinkNode node2 = headNodes2.removeLast();
                    StrongLink deadLink = new StrongLink(node1, node2);
                    if(deadLinks == null){
                        deadLinks = new ArrayList<>();
                    }
                    deadLinks.add(deadLink);
                    return findSteps(headNodes2, tailNodes, deadLinks);
                }
            }
        }
        return null;
    }


    private static void findLinkMap() {
        linkMap.clear();
        for (int i = 1; i <= Cell.maxNum; i++) {
            Set<StrongLink> set = findStrongLinksByNum(i);
            linkMap.add(i, set);
        }
        // xy links
        List<StrongLink> xyLinks = findXYLinks();
        linkMap.addAll(xyLinks);
        // almost locked set
        List<StrongLink> als = findALS();
        linkMap.addAll(als);

        Debug.println("link size: " + linkMap.size());
        Debug.println("max steps: " + maxSteps);
    }

    private static List<StrongLink> findALS() {
        List<StrongLink> list = new ArrayList<>();
        for (int i = 0; i < Board.rows; i++) {
            List<Cell> row = new ArrayList<>();
            List<Cell> col = new ArrayList<>();
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if(cell.candidates.size()>1){
                    row.add(cell);
                }
                Cell cell2 = Board.cells[j][i];
                if(cell2.candidates.size()>1){
                    col.add(cell2);
                }
            }
            List<StrongLink> als = findALS(row);
            List<StrongLink> als2 = findALS(col);
            list.addAll(als);
            list.addAll(als2);
        }
        return list;
    }

    private static List<StrongLink> findALS(List<Cell> list) {
        int n = 3;
        List<StrongLink> list2 = new ArrayList<>();
        List<int[]> combinations = CombineUtil.getCombinations(list.size(), n);
        for (int[] c : combinations) {
            Cell cell1 = list.get(c[0]);
            Cell cell2 = list.get(c[1]);
            Cell cell3 = list.get(c[2]);
            Set<Integer> cans = new HashSet<>();
            cans.addAll(cell1.candidates);
            cans.addAll(cell2.candidates);
            cans.addAll(cell3.candidates);
            if(cans.size() == n+1){
                List<StrongLink> strongLinks = createStrongLinks(cans, cell1, cell2, cell3);
                list2.addAll(strongLinks);
            }
        }
        return list2;
    }

    private static List<StrongLink> createStrongLinks(Set<Integer> cans, Cell... cells) {
        List<StrongLink> links = new ArrayList<>();
        ArrayList<Integer> cans2 = new ArrayList<>(cans);
        List<int[]> combinations = CombineUtil.getCombinations(cans2.size(), cells.length);
        for (int[] c : combinations) {
            Integer c1 = cans2.get(c[0]);
            Integer c2 = cans2.get(c[1]);
            LinkNode node1 = new LinkNode();
            LinkNode node2 = new LinkNode();
            for (Cell cell : cells) {
                if(cell.candidates.contains(c1)){
                    Cell cell1 = new Cell(cell);
                    cell1.linkNum = c1;
                    node1.cells.add(cell1);
                }
                if(cell.candidates.contains(c2)){
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
            }else if (entry.getValue().size() > 2) {
                StrongLink groupedSL = groupedStrongLink2(entry.getValue(), num);
                if (groupedSL != null) {
                    strongLinks.add(groupedSL);
                }
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
