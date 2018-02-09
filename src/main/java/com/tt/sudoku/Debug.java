package com.tt.sudoku;

import java.sql.Timestamp;
import java.util.Date;
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

    public static void printChain(List<LinkNode> list) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i += 2) {
            LinkNode p1 = list.get(i);
            LinkNode p2 = list.get(i + 1);
            sb.append(String.format("%s==>%s-->", p1, p2));
        }
        println(sb.toString());
    }

    public static void debug(List<LinkNode> steps) {
        int head = 0; // 0 or 3
        if (steps.size() > 3) {
            if (steps.get(head).getFirstCell().linkNum == 3
                    && steps.get(head).cells.size() == 3
                    && steps.get(head).getFirstCell().r == 8 - 1
//                    && steps.get(3).getFirstCell().c == 4 - 1
                    && steps.get(1).getFirstCell().linkNum == 3
                    && steps.get(2).getFirstCell().linkNum == 3
                    && steps.get(3-head).getFirstCell().linkNum == 3
                    && steps.get(3-head).getFirstCell().r == 6 -1
//                    && steps.get(4).getFirstCell().linkNum == 5
//                    && steps.get(5).getFirstCell().linkNum == 9
//                    && steps.get(5).getFirstCell().r == 9 - 1
                    ) {
                Debug.println();
            }
        }
    }
}
