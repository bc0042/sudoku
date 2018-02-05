package com.tt.sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ParseHelper {

    public static void parse(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int r = i / Board.cols;
            int c = i % Board.cols;
            if ('.' != ch) {
                Board.cells[r][c].candidates.clear();
                Board.cells[r][c].candidates.add(Integer.parseInt(ch + ""));
            } else {
                Board.cells[r][c] = new Cell(r, c);
            }
        }
        Eliminator.checkCandidates();
        Main.refresh();
    }

    public static void parseFromFile() {
        Debug.println("parseFromFile..");
        String file = "data.txt";
        InputStream is = Board.class.getClassLoader().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> list = new ArrayList<>();
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) break;

                if (line.startsWith("|")) {
                    String[] split = line.split(" ");
                    for (String s : split) {
                        if (s.matches("\\d+")) {
                            list.add(s);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (list.size() == 81) {
            for (int i = 0; i < list.size(); i++) {
                int r = i / Board.rows;
                int c = i % Board.cols;
                Cell cell = new Cell(r, c);
                cell.candidates.clear();
                String s = list.get(i);
                for (int j = 0; j < s.length(); j++) {
                    cell.candidates.add(Integer.parseInt("" + s.charAt(j)));
                }
                Board.cells[r][c] = cell;
            }
        }
        Eliminator.checkCandidates();
    }
}
