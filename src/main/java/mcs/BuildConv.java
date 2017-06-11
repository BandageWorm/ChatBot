package mcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class BuildConv {

    public static void main (String[] args) {
        try {
            BufferedReader postReader = new BufferedReader(new FileReader(new File(FilePath.get("DataSet/repos/repos-id-post-cn"))));
            BufferedReader cmntReader = new BufferedReader(new FileReader(new File(FilePath.get("DataSet/repos/repos-id-cmnt-cn"))));
            FileWriter fw = new FileWriter(new File(FilePath.get("Text/s2s.conv")));

            String postLine;
            String cmntLine;
            while ((postLine = postReader.readLine()) != null && (cmntLine = cmntReader.readLine()) != null) {
                String post = postLine.split("\t")[1];
                String cmnt = cmntLine.split("\t")[1];
                fw.write("E\n");
                fw.write("M " + Util.monoSegment(post) + "\n");
                fw.write("M " + Util.monoSegment(cmnt) + "\n");
                fw.flush();
            }
            fw.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
