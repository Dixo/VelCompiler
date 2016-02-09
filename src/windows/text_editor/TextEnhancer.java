package windows.text_editor;

import windows.utils.WindowColors;

import javax.swing.text.*;
import java.util.*;

/**
 * Created by Dixo on 2/3/2016.
 */
public class TextEnhancer {

    private String directives;
    private String references;
    private String properties;

    public TextEnhancer() {

        bulidRegEx();
    }

    public DefaultStyledDocument getEnhancer() {

        StyleContext cont = StyleContext.getDefaultStyleContext();

        AttributeSet directive_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.DIRECTIVE_COLOR);
        AttributeSet reference_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.REFERENCE_COLOR);
        AttributeSet properties_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.PROPERTIES_COLOR);
        AttributeSet plainText_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.CODE_HOLDER_TEXT_COLOR);
        AttributeSet commentLine_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.COMMENT_LINE_COLOR);
        AttributeSet string_attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, WindowColors.STRING_COLOR);

        DefaultStyledDocument doc = new DefaultStyledDocument() {

            //this boolean is used to make the bracket after a formal reference the same color as the reference
            private boolean nextBracketIsReferenceEnder = false;
            private SortedMap<Integer, String> multiLineCommentLocation;

            public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
                super.insertString(offset, str, a);

                String text = getText(0, getLength());


                int before = findLastNonWordChar(text, offset);
                int after = findFirstNonWordChar(text, offset + str.length());
                int wordL = before;
                int wordR = before;
                int beforeWordL = before;
                int lineBegining = findLineBegining(text, offset);
                int lineEnding = findLineEnding(text, offset);

                // default all text on a line
                setCharacterAttributes(lineBegining, lineEnding - lineBegining, plainText_attr, false);

                dealWithStrings(text, lineBegining, lineEnding);
                dealWithLineComments(text, lineBegining, lineEnding);
                dealWithMultiLineComments(text, lineBegining, lineEnding);

                while (wordR <= after) {

                    if (wordR == after || String.valueOf(text.charAt(wordR)).matches("\\W")) {

                        // checking for properties or methods
                        if (text.substring(wordL, wordR).matches("(\\W)*(" + properties + ")")) {
                            setCharacterAttributes(wordL, wordR - wordL, properties_attr, false);

                            // checking for directives
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(" + directives + ")")) {
                            setCharacterAttributes(wordL, wordR - wordL, directive_attr, false);

                            // checking for references
                        } else if (text.substring(wordL, wordR).matches("(\\W)*(" + references + ")")) {

                            // check if it is the formal or informal style of referencing
                            if (String.valueOf(text.charAt(wordL)).equals("{")) {
                                if (String.valueOf(text.charAt(beforeWordL)).equals("$")) {
                                    setCharacterAttributes(beforeWordL, wordR - wordL, reference_attr, false);
                                    nextBracketIsReferenceEnder = true;
                                }

                            } else {
                                setCharacterAttributes(wordL, wordR - wordL, reference_attr, false);

                            }

                            // if nothing matched it means text must be set to white
                        } else {

                            //check if last close curly bracket is from formal reference
                            if (String.valueOf(text.charAt(wordL)).equals("}") && nextBracketIsReferenceEnder) {
                                setCharacterAttributes(wordL, wordR - wordL, reference_attr, true);
                                nextBracketIsReferenceEnder = false;
                            }
                        }

                        wordL = wordR;
                        if (wordL > 0) {
                            beforeWordL = wordL - 1;
                        }
                    }
                    wordR++;
                }
            }

            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                String text = getText(0, getLength());
                int before = findLastNonWordChar(text, offs);
                int after = findFirstNonWordChar(text, offs);
                int lineBegining = findLineBegining(text, offs);
                int lineEnding = findLineEnding(text, offs);


                // default all text on a line
                setCharacterAttributes(lineBegining, lineEnding - lineBegining, plainText_attr, false);

                dealWithStrings(text, lineBegining, lineEnding);
                dealWithLineComments(text, lineBegining, lineEnding);
                dealWithMultiLineComments(text, lineBegining, lineEnding);
                // dealing with quotes removal
//                for( int index = offs; index < len; index ++){
//                    if(.contains(index)){
//                        int location =
                //            setCharacterAttributes(index, );

//                    }
//                }
//                if (text.substring(before, after).matches("(\\W)*(" + reservedWords + ")")) {
//                    setCharacterAttributes(before, after - before, attr, false);
//                } else {
//                    setCharacterAttributes(before, after - before, plainText_attr, false);
//                }

            }

            private void dealWithStrings(String text, int lineBegining, int lineEnding) {

                List<Integer> doubleQuotesPositions = new ArrayList<>();

                int parser = lineBegining;

                while (parser < lineEnding) {
                    if (String.valueOf(text.charAt(parser)).equals("\"")) {
                        doubleQuotesPositions.add(parser);
                    }
                    parser++;
                }

                for (int index = 0; index < doubleQuotesPositions.size() / 2; index++) {
                    setCharacterAttributes(doubleQuotesPositions.get(index * 2), doubleQuotesPositions.get(index * 2 + 1) - doubleQuotesPositions.get(index * 2) + 1, string_attr, false);
                }
                if (doubleQuotesPositions.size() % 2 == 1) {
                    Integer lastOne = doubleQuotesPositions.get(doubleQuotesPositions.size() - 1);
                    setCharacterAttributes(lastOne, lineEnding - lastOne, string_attr, false);
                }
            }

            private void dealWithLineComments(String text, int lineBegining, int lineEnding) {

                String line = text.substring(lineBegining, lineEnding);
                int positionOfLineComment = line.indexOf("##");

                if (positionOfLineComment != -1) {
                    setCharacterAttributes(lineBegining + positionOfLineComment, lineEnding - positionOfLineComment, commentLine_attr, false);
                }
            }

            /**
             * This method pairs up the deliminators for multi line comments.
             * The method used, will pair "first occurances" meaning that if we have :
             * (1)#* (2)#* (3)*# (4)*#, 1 and 3 will get paired up
             *
             * @param text
             * @param lineBegining
             * @param lineEnding
             */
            private void dealWithMultiLineComments(String text, int lineBegining, int lineEnding) {

                // search for multiLine Comment deliminator
                multiLineCommentLocation = new TreeMap<>();
                getMultiLineCommentDelimitators(text, 0);

                // pair the deliminator
                for (Map.Entry<Integer, String> element1 : multiLineCommentLocation.entrySet()) {
                    for (Map.Entry<Integer, String> element2 : multiLineCommentLocation.entrySet()) {
                        if (element1.getValue().equals("#*") && element2.getValue().equals("*#") && element1.getKey() < element2.getKey()) {
                            setCharacterAttributes(element1.getKey(), element2.getKey() - element1.getKey() + 2, commentLine_attr, false);
                            break;
                        }
                    }
                }
            }

            /**
             * this method parses through the text and searches for strings like #* or *#.
             * this method is recursive. It keeps searching for strings until they are none left
             * the TreeMap that stores the found data, keeps it in a sorted manner so that the algorithm that
             * pairs (#*,*#) together will always pair the two "first occurrences"
             * @param text the entire text to be searched
             * @param start the position from where to start the search. As strings are being found, the search
             *              moves to the end of the text string
             */
            public void getMultiLineCommentDelimitators(String text, int start) {
                if (text.substring(start).contains("#*")) {
                    multiLineCommentLocation.put(start + text.substring(start).indexOf("#*"), "#*");
                    getMultiLineCommentDelimitators(text, start + text.substring(start).indexOf("#*") + 2);
                }
                if (text.substring(start).contains("*#")) {
                    multiLineCommentLocation.put(start + text.substring(start).indexOf("*#"), "*#");
                    getMultiLineCommentDelimitators(text, start + text.substring(start).indexOf("*#") + 2);
                }
            }

            // here is the end of the inner class
        };
        return doc;
    }

    private void bulidRegEx() {

        // the order in which strings are here defined is the order of them being checked

        properties = "";
        properties += "\\..+";

        directives = "";
        directives += "#.+";

        references = "";
        references += "(\\{|\\$).+";


    }

    private int findLastNonWordChar(String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
        }

        if (index < 0) {
            index = 0;
        }
        return index;
    }

    private int findFirstNonWordChar(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).matches("\\W")) {
                break;
            }
            index++;
        }
        return index;
    }

    private int findLineBegining(String text, int index) {
        while (--index >= 0) {
            if (String.valueOf(text.charAt(index)).equals("\n")) {
                break;
            }
        }

        if (index < 0) {
            index = 0;
        }
        return index;
    }

    private int findLineEnding(String text, int index) {
        while (index < text.length()) {
            if (String.valueOf(text.charAt(index)).equals("\n")) {
                break;
            }
            index++;
        }
        return index;
    }

    // unused side of things
    /**
     * @return 1 if it's the start of a Line Comment
     * 2 if it's the start of a Normal Multiline Comment
     * -1 if none of the other three are true
     * OBS: the Special Multiline Comment starts with #** and is therefore a child of th normal one which starts with /*
     */
    private int isCommentStart(String text, int bbforeWordL, int beforeWordL, int wordL) {

        // checking if it is a Line Comment or a Normal Multiline Comment
        if (wordL > 0) {
            if (String.valueOf(text.charAt(beforeWordL)).equals("#")) {

                switch (String.valueOf(text.charAt(wordL))) {
                    case "#":
                        return 1;
                    case "*":
                        return 2;
                }
            }
        }

        // if nothing is returned until here then it isn't a start of a comment
        return -1;
    }

    private int isCommentEnd(String text, int beforeWordL, int wordL, int typeOfComment) {

        switch (typeOfComment) {
            case -1:
                return -1;
            case 1:
                if (String.valueOf(text.charAt(wordL)).equals("\n")) {
                    return -1;
                }
                break;
            case 2:
                if (String.valueOf(text.charAt(beforeWordL)).equals("*") &&
                        String.valueOf(text.charAt(wordL)).equals("#")) {
                    return -1;
                }
                break;
        }
        // this should be impossible to reach
        return typeOfComment;
    }

    private boolean checkIfItIsString(String text, int beforeWordL, int wordL) {
        if (wordL == 0) {
            return String.valueOf(text.charAt(wordL)).equals("'") || String.valueOf(text.charAt(wordL)).equals("\"");
        } else {
            if (String.valueOf(text.charAt(wordL)).equals("'") || String.valueOf(text.charAt(wordL)).equals("\"")) {
                if (String.valueOf(text.charAt(beforeWordL)).equals("\\")) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}