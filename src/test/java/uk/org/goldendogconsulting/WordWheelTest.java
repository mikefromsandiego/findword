package uk.org.goldendogconsulting;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordWheelTest extends TestCase {

    private static final Logger LOG = LogManager.getLogger(WordWheelTest.class);


    public void testConstructor() {
        try {
            WordWheel wordWheel = new WordWheel();
            assertNotNull(wordWheel);
        } catch (PermutateStringException e) {
            fail("Unexpected exception " +e.getClass().getName() +" thrown, " +e.getMessage());
        }
    }
    public void testFindWordsUpperCase() {
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords("ABCDEFGHI");
            assertNotNull(wordWheel);
        } catch (PermutateStringException e) {
            fail("Unexpected exception " +e.getClass().getName() +" thrown, " +e.getMessage());
        }
    }

    public void testFindWordsLowerCaseArgs() {
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords("ocninlrei");
            List<String> words = wordWheel.findNineLetterWords();
            assertFalse(words.isEmpty());
        } catch (PermutateStringException e) {
            fail("Unexpected exception " +e.getClass().getName() +" thrown, " +e.getMessage());
        }
    }

    public void testFindWordsInvalidArgsNumber() {
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords("9BCDEFGHI");
            fail("no expected exception thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("word 9BCDEFGHI is invalid"));
        }
    }

    public void testFindWordsInvalidArgsWhiteSpace() {
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords("A C\tEFGHI");
            fail("no expected exception thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("word A C\tEFGHI is invalid"));
        }
    }

    public void testLetterExtraction() {
        final String TESTLETTERS = "ABCDEFGHI";
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords(TESTLETTERS);
            assertEquals(wordWheel.getCentreLetter(), TESTLETTERS.substring(0,1));
            assertEquals(wordWheel.getWheelLetters(), TESTLETTERS.substring(1));
        } catch (PermutateStringException e) {
            fail("Unexpected exception " +e.getClass().getName() +" thrown, " +e.getMessage());
        }
    }

    public void testUniquenessCount() {
        final Map<String, Integer> testStringsMap = Stream.of(new Object[][] {
                { "ABCDEFGHI", 9 }, { "ABCDEFGHH", 8 }, { "ABCDEFGGG", 7 }, { "ABCDEFFGG", 7 },
                { "ABCDEFFFF", 6 }, { "ABCDEEFFF", 6 }, { "ABCDDEEFF", 6 }, { "ABCDEEEEE", 5 },
                { "ABCDDEEEE", 5 }, { "ABBCCDDEE", 5 }, { "ABCCDDEEE", 5 },
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
        for (String testString : testStringsMap.keySet()) {
            try {
                WordWheel wordWheel = new WordWheel();
                wordWheel.findWords(testString);
                assertEquals(wordWheel.getUniqueCharacterCount(), (int) testStringsMap.get(testString));
            } catch (PermutateStringException permutateStringException) {
                fail("Unexpected exception " +permutateStringException.getClass().getName() +" thrown, " +permutateStringException.getMessage());
            }
        }
    }

    public void testExistingNineLetterWordSearch() {
        final String[] testStringArray = { "OCNINLREI", "RUYDAROTC"};
        for (String testString : testStringArray) {
            try {
                WordWheel wordWheel = new WordWheel();
                wordWheel.findWords(testString);
                List<String> nineLetterWords = wordWheel.findNineLetterWords();
                assertFalse(nineLetterWords.isEmpty());
                LOG.info("Nine letter word is " +nineLetterWords.get(0));
            } catch (PermutateStringException permutateStringException) {
                fail("Unexpected exception " + permutateStringException.getClass().getName() + " thrown, " + permutateStringException.getMessage());
            }
        }
    }

    public void testNonExistingNineLetterWordSearch() {
        final String testString = "ZQJXKVBYW"; // Sourced from https://11points.com/11-least-used-letters-english-3-decently-surprising/
        try {
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords(testString);
            List<String> nineLetterWords = wordWheel.findNineLetterWords();
            assertTrue(nineLetterWords.isEmpty());
        } catch (PermutateStringException permutateStringException) {
            fail("Unexpected exception " +permutateStringException.getClass().getName() +" thrown, " +permutateStringException.getMessage());
        }
    }

    public void testFindWordsDuplicateSourceLetters() {
        final Map<String, Integer> testStringsMap = Stream.of(new Object[][] {
                { "ABCDEFGHI", 9 }, { "ABCDEFGHH", 8 }, { "ABCDEFGGG", 7 }, { "ABCDEFFGG", 7 },
                { "ABCDEFFFF", 6 }, { "ABCDEEFFF", 6 }, { "ABCDDEEFF", 6 }, { "ABCDEEEEE", 5 },
                { "ABCDDEEEE", 5 }, { "ABBCCDDEE", 5 }, { "ABCCDDEEE", 5 },
        }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
        for (String testString : testStringsMap.keySet()) {
            try {
                long startTime = System.nanoTime();
                WordWheel wordWheel = new WordWheel();
                boolean result = wordWheel.findWords(testString);
                assertEquals(wordWheel.getUniqueCharacterCount(), (int) testStringsMap.get(testString));
                LOG.info("Evaluating word " +testString +" containing " +wordWheel.getUniqueCharacterCount()
                        +" unique letters took " +computeMsecTime(startTime, System.nanoTime()) +" msecs");
                assertTrue(result);
                assertEquals(wordWheel.getCombinationsToFind(), wordWheel.getCombinationsSet().size());
            } catch (PermutateStringException permutateStringException) {
                fail("Unexpected exception " +permutateStringException.getClass().getName() +" thrown, " +permutateStringException.getMessage());
            }
        }
    }

    public void testThreadedFind() {
        try {
            long startTime = System.nanoTime();
            WordWheel wordWheel = new WordWheel();
            wordWheel.findWords("ABCDEFGHI");
            long endTime = System.nanoTime();
            assertFalse(wordWheel.getCombinationsSet().isEmpty());
            assertEquals(wordWheel.getCombinationsSet().size(), wordWheel.getCombinationsToFind());
            System.out.println("valid words " +wordWheel.getValidWords().size());
            assertEquals(68, wordWheel.getValidWords().size());
            System.out.println("Time taken " +computeMsecTime(startTime, endTime) +" msecs");
        } catch (PermutateStringException e) {
            fail("Unexpected exception " +e.getClass().getName() +" thrown, " +e.getMessage());
        }
    }

    private long computeMsecTime(long startTime, long endTime) {
        final long MILLION = 1000000;
        return ((endTime - startTime)/MILLION);
    }
}