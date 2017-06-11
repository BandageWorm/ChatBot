package mcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * test STC.
 * Created by kurtg on 17/2/8.
 */
public class Test {
    public static void main(String[] args) throws Exception{
        BufferedReader fr = new BufferedReader(new FileReader(new File(FilePath.get("Text\\test-id-post-cn"))));
        String ask;
        Engine engine = new Engine();
        while ((ask = fr.readLine()) != null){
            ask = ask.split("\t")[1];
            System.out.println(ask);
            long timer = System.currentTimeMillis();
            System.out.println(engine.reply(ask));
            timer = System.currentTimeMillis()-timer;
            System.out.print("takes " + timer/1000.0 + " s.\n\n");
        }
    }
}
