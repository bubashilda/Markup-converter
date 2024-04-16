package md2html;

import java.io.*;

public class Md2Html {
    public static void main(String[] args) {
        MdFileSource file = new MdFileSource(args[0], args[1]);
        MdFileParser parser = new MdFileParser(file);
        try {
            file.write(parser.parse());
            file.close();
        } catch (IOException e) {
            System.out.println("Something went wrong during parsing: " + e.getMessage());
        }
    }
}
