package com.tt.sudoku;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MyKeyListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if ((e.getKeyCode() == KeyEvent.VK_F) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
            handleFind();
        }
        if ((e.getKeyCode() == KeyEvent.VK_R) && ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)) {
            handleRefresh();
        }
        if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            handlePaste();
        }
        if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            handleCopy();
        }
        if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
            handleSave();
        }
        if (e.getKeyCode() == KeyEvent.VK_F12) {
            ChainSolver.forcingChain();
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            ChainSolver.reset();
        }
    }

    private void handleSave() {
        String s = handleCopy();
        try {
            String file = MyKeyListener.class.getClassLoader().getResource(ParseHelper.dataFile).getFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(s);
            writer.close();
            Debug.println("save to file..");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            String trim = data.toString().trim();
            Debug.println("paste: \n" + trim);
            ParseHelper.parse(trim);
            Main.refresh();
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleCopy() {
        StringBuffer sb = new StringBuffer();
        String replacement = "=";
        try {
            List<String> data = ParseHelper.getDatafile();
            for (String line : data) {
                sb.append(line.replaceAll("\\d+", replacement));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = sb.toString();
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.cols; j++) {
                List<Integer> candidates = Board.cells[i][j].candidates;
                StringBuffer sb2 = new StringBuffer();
                for (Integer integer : candidates) {
                    sb2.append(integer);
                }
                str = str.replaceFirst(replacement, sb2.toString());
            }
        }
        copy2Clipboard(str);
        Debug.println("copy to clipboard.. \n" + str);
        return str;
    }

    @Deprecated
    private void handleCopy2() {
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
        copy2Clipboard(sb.toString());
        Debug.println("copy to clipboard: " + sb);
    }

    private void copy2Clipboard(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection sel = new StringSelection(str);
        clipboard.setContents(sel, sel);
    }

    private void handleRefresh() {
        Main.refresh();
    }

    public static void handleFind() {
        SimpleSolver.checkCandidates();
        SimpleSolver.hiddenSingle();
//        SimpleSolver.hiddenPair(); // seems useless

        if (!ChainSolver.findChain()) {
            Debug.println("no chains found!!");
            ChainSolver.maxSteps += 2;
            if (ChainSolver.maxSteps > 10) {
                ChainSolver.maxSteps = 4;
                ChainSolver.alsEnabled = true;
                Debug.println(String.format("== ALS enabled size-%d ==", ChainSolver.alsSize));
            }
            if (ChainSolver.alsEnabled && ChainSolver.maxSteps > 8) {
                ChainSolver.alsSize++;
                ChainSolver.maxSteps = 4;
                Debug.println(String.format("== ALS enabled size-%d ==", ChainSolver.alsSize));
            }
        }
    }
}
