package com.tt.sudoku;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by BC on 1/31/18.
 */
public class Debug {

    public static void println(Object... args) {
        Object ts = new Timestamp(new Date().getTime());
        System.out.print(String.format("[%s]: ", ts));
        StringBuffer sb = new StringBuffer();
        for (Object arg : args) {
            sb.append(arg + ", ");
        }
        if (sb.toString().endsWith(", ")) {
            sb.delete(sb.length() - 2, sb.length() - 1);
        }
        System.out.println(sb);
    }

    public static void exit() {
        System.exit(11);
    }

    public static void printChain(List<Cell> list) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i += 2) {
            Cell c1 = list.get(i);
            Cell c2 = list.get(i + 1);
            sb.append(String.format("r%sc%s-%s==>", c1.r + 1, c1.c + 1, c1.num));
            sb.append(String.format("r%sc%s-%s-->", c2.r + 1, c2.c + 1, c2.num));
        }
        println(sb.toString());
    }

    public static void printExcludeList(List<Cell> excludeList) {
        for (Cell cell : excludeList) {
            println(String.format("exclude: r%sc%s-%s", cell.r + 1, cell.c + 1, cell.excludes));
        }
    }
}
