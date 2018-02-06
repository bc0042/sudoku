package com.tt.sudoku;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

public class MyKeyListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if ((e.getKeyCode() == KeyEvent.VK_F) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
            handleFind();
        }
        if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiers() & KeyEvent.ALT_MASK) != 0)) {
            handleRefresh();
        }
        if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            handlePaste();
        }
        if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            handleCopy();
        }
    }

    private void handlePaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            String trim = data.toString().trim();
            Debug.println(trim);

            int length = trim.length();
            if (length == Board.rows * Board.cols) {
                Debug.println("paste: "+trim);
                ParseHelper.parse(trim);
            } else {
                Debug.println("data err");
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCopy() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                List<Integer> candidates = Board.cells[i][j].candidates;
                if (candidates.size() == 1) {
                    sb.append(candidates.get(0));
                } else {
                    sb.append(".");
                }
            }
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection sel = new StringSelection(sb.toString());
        clipboard.setContents(sel, sel);
        Debug.println("copy to clipboard: " + sb);
    }

    private void handleRefresh() {
        Main.refresh();
    }

    public static void handleFind() {
        Solver.findChain();
//        Main.paintChain(Solver.steps);
        Main.paintExcludeList(Solver.excludeList);
        Solver.findHiddenSingle();
    }
}
