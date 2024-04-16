package md2html;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MdFileSource {

    private BufferedReader in;
    private Writer out;

    MdFileSource(String inputFile, String outputFile) {
        try {
            in = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Input error: " + e.getMessage());
        }
        try {
            out = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Output error: " + e.getMessage());
        }
    }

    public String getLine() throws IOException {
        return in.readLine();
    }

    public String getParagraph() throws IOException {
        StringBuilder result = new StringBuilder();
        String prefix = getLine();
        while (prefix != null && prefix.isEmpty()) {
            prefix = getLine();
        }
        if (prefix != null) {
            result.append(prefix);
            prefix = getLine();
        }
        while (prefix != null && !prefix.isEmpty()) {
            result.append(System.lineSeparator()).append(prefix);
            prefix = getLine();
        }
        return result.toString();
    }

    public void write(String s) {
        try {
            out.write(s);
        } catch (IOException e) {
            System.out.println("Output error: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        in.close();
        out.close();
    }
}
