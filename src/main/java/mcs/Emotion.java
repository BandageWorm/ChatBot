package mcs;

import java.io.*;
import java.util.ArrayList;

/**
 * Emotion analysis demo.
 * Created by kurtg on 17/1/21.
 */
public class Emotion {
    private ArrayList<String> posDict;
    private ArrayList<String> negDict;

    public Emotion() {
        this.posDict = getDict(FilePath.get("Text\\ntusd-positive.txt"));
        this.negDict = getDict(FilePath.get("Text\\ntusd-negative.txt"));
    }

    private static ArrayList<String> getDict(String path) {
        ArrayList<String> set = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                set.add(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return set;
    }

    public boolean isPositive(ArrayList<String> words){
        int positive = 0;
        for (String word : words) {
            if (posDict.contains(word)) positive++;
            if (negDict.contains(word)) positive--;
        }
        return positive >= 0;
    }

    public static void main(String[] args) {
        try {
        Emotion eParser = new Emotion();
        BufferedReader testReader = new BufferedReader(new FileReader(new File(FilePath.get("Text\\train-id-post-cn"))));
        String line;
        while ((line = testReader.readLine()) != null) {
            String text = line.split("\t")[1];
            ArrayList<String> words = Util.segment(text);
            System.out.print((eParser.isPositive(words) ? "\uD83D\uDE01" : "\uD83D\uDE22") + "\t" + text);
            System.out.print(" | ");
            for (String s : words)
                System.out.print(" " + s);
            System.out.print("\n");
        }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
