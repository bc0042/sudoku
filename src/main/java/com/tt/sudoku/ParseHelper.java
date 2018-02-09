package com.tt.sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseHelper {

    static String dataFile = "data.txt";
    ;

    public static void parse(String s) {
        if (s.length() > Board.rows * Board.cols) {
            String[] split = s.split("\n");
            List<String> list = Arrays.asList(split);
            parse2(list);
            return;
        }
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
        SimpleSolver.checkCandidates();
        Main.refresh();
    }

    public static void parse2(List<String> data) {
        List<String> list = new ArrayList<>();
        for (String line : data) {
            if (line.startsWith("|")) {
                String[] split2 = line.split(" ");
                for (String s2 : split2) {
                    if (s2.matches("\\d+")) {
                        list.add(s2);
                    }
                }
            }
        }
        if (list.size() == 81) {
            for (int i = 0; i < list.size(); i++) {
                int r = i / Board.rows;
                int c = i % Board.cols;
                Cell cell = new Cell(r, c);
                cell.candidates.clear();
                String s3 = list.get(i);
                for (int j = 0; j < s3.length(); j++) {
                    cell.candidates.add(Integer.parseInt("" + s3.charAt(j)));
                }
                Board.cells[r][c] = cell;
            }
        }
        SimpleSolver.checkCandidates();
    }


    public static void parseFromFile() {
        try {
            Debug.println("parseFromFile..");
            List<String> data = getDatafile();
            parse2(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getDatafile() throws IOException {
        InputStream is = Board.class.getClassLoader().getResourceAsStream(dataFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> list = new ArrayList<>();
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            list.add(line + "\n");
        }
        return list;
    }
}
