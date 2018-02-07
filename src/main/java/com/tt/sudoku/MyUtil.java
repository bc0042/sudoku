package com.tt.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by BC on 2/7/18.
 */
public class MyUtil {

    private static List<int[]> list = new ArrayList<>();

    public static List<int[]> getCombinations(int m, int n) {
        list.clear();
        int[] a = new int[n];
        combine(m, n, a);
        return list;
    }

    private static void combine(int m, int n, int[] a) {
        if (n == 0) {
            int[] a2 = Arrays.copyOf(a, a.length);
            list.add(a2);
            return;
        }
        for (int i = n; i <= m; i++) {
            a[n - 1] = i - 1;
            combine(i - 1, n - 1, a);
        }
    }
}
