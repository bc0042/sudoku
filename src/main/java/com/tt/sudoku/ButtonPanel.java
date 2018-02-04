package com.tt.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ButtonPanel extends JPanel {
    public ButtonPanel() {
        add(btn());
        add(btn2());
    }

    private static Component btn2() {
        JButton b = new JButton("<HTML><U>R</U>efresh</HTML>");
        return b;
    }

    private static JButton btn() {
        JButton b = new JButton("<HTML><U>F</U>ind</HTML>");
        b.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyKeyListener.handleFind();
            }
        });
        return b;
    }
}
