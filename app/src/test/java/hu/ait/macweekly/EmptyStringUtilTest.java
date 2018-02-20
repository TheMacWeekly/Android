package hu.ait.macweekly;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mackhartley on 2/15/18.
 */

public class EmptyStringUtilTest {
    private static final String hasSpaces1 = "Not an empty string";
    private static final String noSpaces1 = "Abcdefghijkl";
    private static final String singleCharUpper = "A";
    private static final String singleCharLower = "a";
    private static final String hasSpaces2 = "ajajajaja ajajajaja ajajajaja ajajajajajajajajajajajajaja";
    private static final String hasNewLine = "This is a test\n";
    private static final String onlyNewlines = "\n\n\n";
    private static final String onlySpaces = "   ";
    private static final String onlyTabs = "    ";
    private static final String onlySymbols = "()*./&^%$#@!";

    private static final String isEmpty = "";
    private static final String isNull = null;


    @Test
    public void testValidNonEmptyStringReturnsFalse() throws Exception {
        String[] falseTests = {hasSpaces1, noSpaces1, singleCharUpper, singleCharLower, hasSpaces2, hasNewLine, onlyNewlines, onlySpaces, onlyTabs, onlySymbols};
        for(String test : falseTests) {
            assertEquals(MacWeeklyUtils.isTextEmpty(test), false);
        }
    }

    @Test
    public void detectsEmptyStrings() throws Exception {
        String[] trueTests = {isEmpty, isNull};
        for(String test : trueTests) {
            assertEquals(MacWeeklyUtils.isTextEmpty(test), true);
        }
    }

}
