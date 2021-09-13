package org.goldendogconsulting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Engine to parse a String for valid words
 *
 * @author davidscholefield
 */
public class WordWheel {
    private final String centreLetter;
    private final String wheelLetters;
    private final Set<String> dictionary;
    private final Set<String> permutations;
    private final Map<String, String> permutationCombinationsLookup;
    private final Set<String> combinationsSet;
    private final Set<String> validWords;
    private final int uniqueCharacterCount;
    private final int combinationsToFind;

    private static final Logger LOG = LogManager.getLogger(WordWheel.class);

    /**
     *
     * @param word String (more like a collection of characters) to searched for, convention is that the first letter of
     *             the String should be in all combinations and words found.
     * @throws PermutateStringException exception thrown if an error in parsing is encountered
     */
    public WordWheel(String word) throws PermutateStringException {
        // Check validity of string passed
        if (!isAlpha(word)) {
            throw new PermutateStringException("word " +word
                    +" is invalid (null, not nine characters in length or contains non alpha characters");
        }
        centreLetter = word.toUpperCase().substring(0,1);
        wheelLetters = word.toUpperCase().substring(1);
        dictionary = new HashSet<>();
        permutations = new HashSet<>();
        permutationCombinationsLookup = new HashMap<>();
        // This does not work for threads
//        combinationsSet = new HashSet<>();
//        validWords = new HashSet<>();

        // This DOES work for threads
//        combinationsSet = Collections.synchronizedSet(new HashSet<>());
//        validWords = Collections.synchronizedSet(new HashSet<>());
        combinationsSet = new ConcurrentSkipListSet<>();
        validWords = new ConcurrentSkipListSet<>();

        uniqueCharacterCount = findUniqueCharacterCount(word);
        loadDictionary();
        loadPermutationsCombinationsLookup();

        // Calculate permutations
        findPermutation(word.toUpperCase(), "");
        combinationsToFind = Integer.parseInt(permutationCombinationsLookup.get(uniqueCharacterCount +Integer.toString(permutations.size())));

        LOG.debug("Looking for " +combinationsToFind +" combinations of \"" +word +"\"");
    }

    // Private class methods
    private static boolean isAlpha(String s) {
        return s != null && s.length() == 9 && s.chars().allMatch(Character::isLetter);
    }

    private int findUniqueCharacterCount(String word) {
        Set<Character> charSet = new HashSet<>();
        for (Character character : word.toCharArray()) {
            charSet.add(character);
        }
        return charSet.size();
    }

    private void findPermutation(String str, String ans) {
        // If string is empty, this is the bottom of the recursive call when a permutation has been found
        if (str.length() == 0) {
            permutations.add(ans);
            return;
        }

        for (int i = 0; i < str.length(); i++) {
            // ith character of str
            char ch = str.charAt(i);

            // Rest of the string after excluding the ith character
            String ros = str.substring(0, i) + str.substring(i + 1);

            // Recursive call
            findPermutation(ros, ans + ch);
        }
    }

    private void loadDictionary() {
        try (Scanner scanner = new Scanner(Objects.requireNonNull(WordWheel.class.getResourceAsStream("/dictionary.txt")))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.startsWith("--")) {
                    dictionary.add(line);
                }
            }
        }
    }

    private void loadPermutationsCombinationsLookup() {
        try (Scanner scanner = new Scanner(Objects.requireNonNull(WordWheel.class.getResourceAsStream("/permutationLookup.txt")))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] lineParts = line.split(":");

                // first part is name, second is number
                String key = lineParts[0].trim();
                String value = lineParts[1].trim();
                permutationCombinationsLookup.put(key, value);
            }
        }
    }

    private void substring(String content, String part, int index) {
        if (index >= content.length()) {
            return;
        }
        String sub = part + content.charAt(index);

        permutation("", sub);

        substring(content, sub, index + 1);
    }

    private void permutation(String prefix, String str) {
        int n = str.length();
        if (n == 0) {
            combinationsSet.add(prefix);
            if (centreLetter != null) {
                if ((prefix.contains(centreLetter)) && (dictionary.contains(prefix)))
                    validWords.add(prefix);
            }
        } else {
            for (int i = 0; i < n; i++) {
                permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i + 1, n));
            }
        }
    }

    // Public methods

    /**
     * Single threaded method to find
     * @return true
     */

 // To Do Depracare/remove this method
    public boolean findWords() {
        int count = 0;
        boolean success = false;
        long startTime = System.nanoTime();
        for (String permutation : permutations) {
            LOG.debug("Evaluating permutation " + count++ + " of " + permutations.size() + "(" + permutation + ") found "
                    +combinationsSet.size() + " combinations. looking for " +combinationsToFind +" possible combinations, "
                    +validWords.size() + " words found.");
            for (int i = 0; i < permutation.length(); i++) {
                substring(permutation, "", i);
            }
            // Safety exit if permutation limit is not found
            if (count == 400) {
                LOG.error("Whoops! failed to find maximum combinations possible (" +combinationsToFind +"), took " +computeMsecTime(startTime) +" msec");
                return false;
            }
            if (combinationsSet.size() >= combinationsToFind) {
                LOG.debug("Found all (" +combinationsSet.size() +") combinations for \"" +centreLetter +wheelLetters +"\"- exiting took " +computeMsecTime (startTime) +" msec");
                success = true;
                break;
            }
        }
        return success;
    }

    /**
     *
     * @return true if the source string has been searched and all possible combinations have been found but there are
     * more combinations still to be found, false if there are no more combinations to be found
     */
    public boolean findWordsThreaded() {
        List<Future<Boolean>> resultList = new ArrayList<>();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(40);
        for (String permutation : permutations) {
                FindCombination findCombination = new FindCombination(permutation, combinationsSet, validWords,
                        combinationsToFind,  centreLetter, dictionary);
                Future<Boolean> result = executor.submit(findCombination);
                resultList.add(result);
        }

        for(Future<Boolean> future : resultList)
        {
            try {
                future.get();
            }
            catch (InterruptedException | ExecutionException exception) {
                LOG.error("Exception " +exception.getClass().getName() +"occurred : " +exception.getMessage(), exception);
                return false;
            }
        }
        // Shut down the executor service
        executor.shutdown();
        return true;
    }

    private long computeMsecTime(long startTime) {
        long endTime = System.nanoTime();
        final long MILLION = 1000000;
        return ((endTime - startTime)/MILLION);
    }

    /**
     * This object does two things
     * <ul>
     *     <li>Create a set of all possible permutations from the string passed into the object constructor</li>
     *     <li>For each permutation all possible combinations of that string from two letters in length upwards</li>
     * </ul>
     * The first set can be easily searched for any valid words against a known dictionary list
     * @return List of Valid nine-letter words within the dictionary that have been found
     */
    public List<String> findNineLetterWords() {
        List<String> nineLetterWords = new ArrayList<>();
        for (String permutation : permutations) {
            if (dictionary.contains(permutation)) {
                nineLetterWords.add(permutation);
            }
        }
        LOG.debug("Found " +nineLetterWords.size() +" nine letter word(s)");
        return nineLetterWords;
    }

    // Accessors and mutators
    public int getUniqueCharacterCount() {
        return uniqueCharacterCount;
    }

    public String getCentreLetter() {
        return centreLetter;
    }

    public String getWheelLetters() {
        return wheelLetters;
    }

    public long getPermutationsSize() {
        return permutations.size();
    }

    public int getCombinationsToFind() {
        return combinationsToFind;
    }

    public Set<String> getCombinationsSet() {
        return combinationsSet;
    }

    public Set<String> getValidWords() {
        return validWords;
    }
}
