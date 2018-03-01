package com.tt.sudoku;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by BC on 2/7/18.
 */
public class SimpleSolver {
    public static void checkCandidates() {
        Debug.println("check candidates..");
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

    public static void hiddenSingle() {
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

    public static List<Cell> getCellsContainsNum(int num) {
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

    @Deprecated
    public static void hiddenPair() {
        for (int i = 0; i < Board.rows; i++) {
            List<Cell> row = new ArrayList<>();
            for (int j = 0; j < Board.cols; j++) {
                Cell cell = Board.cells[i][j];
                if (cell.candidates.size() > 1) {
                    row.add(cell);
                }
            }
            List<Integer> hiddenPair = getHiddenPair(row);
            if (hiddenPair != null) {
                Debug.println(String.format("hidden pairs: row%s %s", i + 1, hiddenPair));
                for (Cell cell : row) {
                    if(cell.candidates.containsAll(hiddenPair)){
                        cell.candidates.retainAll(hiddenPair);
                    }
                }
            }
        }
        // todo cols
    }

    private static List<Integer> getHiddenPair(List<Cell> list) {
        int size = 3;
        Set<Integer> set = new HashSet<>();
        for (Cell cell : list) {
            set.addAll(cell.candidates);
        }
        ArrayList<Integer> candidates = new ArrayList<>(set);
        List<int[]> combinations = CombineUtil.getCombinations(candidates.size(), size);
        for (int[] c : combinations) {
            List<Integer> subSet = new ArrayList<>();
            for (int i = 0; i < c.length; i++) {
                subSet.add(candidates.get(c[i]));
            }
//            Map<Boolean, List<Cell>> collect = list.stream().collect(Collectors.groupingBy(
//                    cell -> new ArrayList<>(cell.candidates).removeAll(subSet)));
            HashMap<Boolean, List<Cell>> collect = null;
            for (Map.Entry<Boolean, List<Cell>> entry : collect.entrySet()) {
                if (entry.getKey() && entry.getValue().size() == size) {
                    return subSet;
                }
            }
        }
        return null;
    }
}
