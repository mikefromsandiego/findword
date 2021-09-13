package org.goldendogconsulting;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Object that allows a String to be parsed for all combinations of that String.
 * This object allows this parsing to be done within a set of threads.
 *
 * @author David Scholefield
 *
 */
public class FindCombination implements Callable<Boolean> {

    private final Set<String> combinationsSet;
    private final Set<String> validWords;
    private final int expectedCombinationSize;
    private final String centreLetter;
    private final Set<String> dictionary;

    private final String permutation;

    /**
     *
     * @param permutation : String to be permutated
     * @param combinationsSet : combinations of String found, this is a thread safe collection
     * @param validWords : valid words found, this is a thread safe collection
     * @param expectedCombinationSize : for every string, there is a finite number of combinations available. This that limit
     * @param centreLetter : the centre letter of the word wheel all combinations MUST contain this letter
     * @param dictionary : dictionary of known valid words
     */
    public FindCombination(String permutation, Set<String>combinationsSet, Set<String>validWords,
                           int expectedCombinationSize, String centreLetter, Set<String>dictionary) {
        this.permutation = permutation;
        this.combinationsSet = combinationsSet;
        this.validWords = validWords;
        this.expectedCombinationSize = expectedCombinationSize;
        this.centreLetter = centreLetter;
        this.dictionary = dictionary;
    }

    /**
     * Implementation of the access method (i.e. the Callable interface), allowing this object to be part of a Future.
     */
    // ToDo make the return value a string giving details of what has been found
    @Override
    public Boolean call() throws Exception {
//        System.out.println("DEBUG: evaluating \"" +permutation +" combinations set size " +combinationsSet.size() +", target set size " +expectedCombinationSize +" valid words size is " +validWords.size());
        // Otherwise call substring() on the particular permutation passed in
        for (int i = 0; i < permutation.length(); i++) {
            // Check if we have exhausted/discovered all the combinations? If so, then exit
            if (combinationsSet.size() >= expectedCombinationSize) {
                return false;
            }
            substring(permutation, "", i);
        }
        return true;
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
}
