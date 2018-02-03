package com.tt.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by BC on 1/31/18.
 */
public class Main {
    private static Board board;

    public static void main(String[] args) {
        JFrame jFrame = new JFrame();
        board = new Board();
        jFrame.add(board, BorderLayout.CENTER);
        jFrame.add(btn(), BorderLayout.SOUTH);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
        Debug.println("show..");

//        String s = "9..6.47287..95...66...72...5.91.7..43845691721....3..9...2....7....962.12967.....";
//        board.parseData(s);

        board.parseFromFile();
        Eliminator.run();
        board.repaint();
    }

    private static JButton btn() {
        JButton b = new JButton("Find SL");
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Eliminator.hiddenSingle();
                Eliminator.run();
                Eliminator.findChain();
                board.paintChain(Eliminator.chain);
                board.paintExcludeList(Eliminator.excludeList);
                Eliminator.exclude();
                Eliminator.run();
//                Eliminator.run();
//                board.repaint();
            }
        });
        return b;
    }

}
