package md2html;

import java.util.*;
import java.io.*;

public class MdFileParser {

    private final MdFileSource file;
    private int indexCharInBlock;
    private String block;
    private boolean couldCloseTag;
    private static final int maxLengthMarkdownTag;
    private static final Map<Character, String> specialSymbols = new HashMap<>();
    private static final Map<String, String> matchMdHtml = new HashMap<>();

    MdFileParser(MdFileSource file) {
        this.file = file;
    }

    static {
        specialSymbols.put('<', "&lt;");
        specialSymbols.put('>', "&gt;");
        specialSymbols.put('&', "&amp;");
        matchMdHtml.put("*", "</em>");
        matchMdHtml.put("_", "</em>");
        matchMdHtml.put("**", "</strong>");
        matchMdHtml.put("__", "</strong>");
        matchMdHtml.put("++", "</u>");
        matchMdHtml.put("--", "</s>");
        matchMdHtml.put("`", "</code>");
        matchMdHtml.put("~", "</mark>");
        matchMdHtml.put("```", "</pre>");
        maxLengthMarkdownTag = 3;
    }

    public String parse() throws IOException {
        String block = file.getParagraph();
        StringBuilder result = new StringBuilder();
        while (!block.isEmpty()) {
            this.indexCharInBlock = 0;
            this.block = block;
            result.append(parseBlock()).append(System.lineSeparator());
            block = file.getParagraph();
        }
        return result.toString();
    }

    private String parseBlock() {
        StringBuilder res = new StringBuilder();
        int countGrids = countGridsHeader();
        if (countGrids > 0) {
            res.append("<h").append(countGrids).append(">");
            indexCharInBlock = countGrids + 1;
            res.append(parseContent(null, true));
            res.append("</h").append(countGrids).append(">");
        } else {
            res.append("<p>").append(parseContent(null, true)).append("</p>");
        }
        return res.toString();
    }

    private String parseMarkdown() {
        String openTag = getOpenSequence();
        indexCharInBlock += openTag.length();
        StringBuilder res = new StringBuilder();
        couldCloseTag = false;
        if (openTag.equals("```")) {
            res.append(parseContent(openTag, false));
        } else {
            res.append(parseContent(openTag, true));
        }
        if (couldCloseTag) {
            res = new StringBuilder(matchMdHtml.get(openTag).replace("/", "") + res);
        } else {
            res = new StringBuilder(openTag + res);
        }
        return res.toString();
    }

    private String parseContent(String closeSequence, boolean needToParse) {
        StringBuilder result = new StringBuilder();
        while (indexCharInBlock < block.length()) {
            if (specialSymbols.containsKey(block.charAt(indexCharInBlock))) {
                result.append(specialSymbols.get(block.charAt(indexCharInBlock)));
            } else if (block.charAt(indexCharInBlock) == '\\' && checkMarkdownSymbol()) {
                result.append(block.charAt(indexCharInBlock + 1));
                indexCharInBlock++;
            } else if (checkOpenSequence() && !getOpenSequence().equals(closeSequence) && needToParse) {
                result.append(parseMarkdown());
            } else if (checkCloseSequence(closeSequence)) {
                indexCharInBlock += closeSequence.length() - 1;
                couldCloseTag = true;
                return result.append(matchMdHtml.get(closeSequence)).toString();
            } else {
                result.append(block.charAt(indexCharInBlock));
            }
            indexCharInBlock++;
        }
        return result.toString();
    }

    private int countGridsHeader() {
        int indexGrid = 0;
        while (indexGrid < block.length() && block.charAt(indexGrid) == '#') {
            indexGrid++;
        }
        if (indexGrid == block.length() || !Character.isWhitespace(block.charAt(indexGrid))) {
            return 0;
        }
        return indexGrid;
    }

    private boolean checkCloseSequence(String closeSequence) {
        return closeSequence != null && block.startsWith(closeSequence, indexCharInBlock);
    }

    private boolean checkMarkdownSymbol() {
        String markdownContainer = "*`_";
        return indexCharInBlock + 1 < block.length() &&
                markdownContainer.contains(String.valueOf(block.charAt(indexCharInBlock + 1)));
    }

    private boolean checkOpenSequence() {
        StringBuilder s = new StringBuilder(String.valueOf(block.charAt(indexCharInBlock)));
        for (int i = 1; i < maxLengthMarkdownTag && indexCharInBlock + i < block.length(); i++) {
            if (matchMdHtml.containsKey(s.toString())) {
                return true;
            }
            s.append(block.charAt(indexCharInBlock + i));
        }
        return false;
    }

    private String getOpenSequence() {
        String s = block.substring(indexCharInBlock,
                Integer.min(indexCharInBlock + maxLengthMarkdownTag, block.length()));
        int iterations = s.length();
        for (int i = 0; i < iterations; i++) {
            if (matchMdHtml.containsKey(s))
                return s;
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
