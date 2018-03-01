package com.tt.sudoku;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    public static void printChain(List<LinkNode> chain) {
        println(chain2str(chain));
    }

    public static void printForcingChain(List<LinkedList<LinkNode>> chains) {
        println("forcing chain..");
        for (LinkedList<LinkNode> chain : chains) {
            LinkedList<LinkNode> chain2 = new LinkedList<>(chain);
            StringBuffer sb = new StringBuffer();
            LinkNode first = chain2.removeFirst();
            LinkNode last = chain2.removeLast();
            sb.append(first).append("-->");
            sb.append(chain2str(chain2));
            sb.append(last).append("-->");
            println(sb.toString());
        }
    }

    private static String chain2str(List<LinkNode> chain) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chain.size(); i += 2) {
            LinkNode p1 = chain.get(i);
            LinkNode p2 = chain.get(i + 1);
            sb.append(String.format("%s==>%s-->", p1, p2));
        }
        return sb.toString();
    }

    public static void exit() {
        System.exit(11);
    }

    public static void debug2(Set<StrongLink> als) {
        for (StrongLink a : als) {
            if (a.node1.cells.size() == 2
                    && a.node1.getLinkNum() == 7) {
                System.out.println();
            }
            if (a.node2.cells.size() == 2
                    && a.node2.getLinkNum() == 7) {
                System.out.println();
            }

        }
    }

    public static void debug(LinkedList<LinkNode> headNodes, LinkedList<LinkNode> tailNodes) {
        if(headNodes.size()!=4) return;
        int head = 0; // 0 or 3
        if (headNodes.get(head).getLinkNum() == 2
                && headNodes.get(head).cells.size() == 1
                && headNodes.get(head).getFirstCell().r == 4 - 1
                && headNodes.get(1).getLinkNum() == 7
                && headNodes.get(2).getLinkNum() == 7
                && headNodes.get(3).getLinkNum() == 8
                && tailNodes.get(0).getLinkNum() == 8
                && tailNodes.get(0).cells.size() == 1
                && tailNodes.get(0).getFirstCell().r == 9-1
                && tailNodes.get(1).getLinkNum() == 2
                ) {
            Debug.println();
        }
    }
}
