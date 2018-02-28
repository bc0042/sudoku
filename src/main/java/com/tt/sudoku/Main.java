package com.tt.sudoku;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by BC on 1/31/18.
 */
public class Main {
    private static Board board;

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        board = new Board();
        jFrame.addKeyListener(new MyKeyListener());
        jFrame.add(board, BorderLayout.CENTER);
        jFrame.add(new ButtonPanel(), BorderLayout.SOUTH);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setFocusable(true);
        jFrame.setVisible(true);
        Debug.println("show..");

//        String s = "9..6.47287..95...66...72...5.91.7..43845691721....3..9...2....7....962.12967.....";
//        ParseHelper.parse(s);
        ParseHelper.parseFromFile();
        SimpleSolver.hiddenSingle();
        refresh();
    }


    public static void refresh() {
        board.repaint();
    }

    public static void paintChain(List<LinkNode> chain) {
        board.paintChain(chain);
    }

    public static void paintExcludes(List<Cell> excludes) {
        board.paintExcludeList(excludes);
    }

    public static void printForcingChain(List<LinkedList<LinkNode>> forcingChain) {
        board.printForcingChain(forcingChain);
    }
}
