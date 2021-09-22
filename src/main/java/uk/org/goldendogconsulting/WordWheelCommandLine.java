package uk.org.goldendogconsulting;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.List;
import java.util.Set;

public class WordWheelCommandLine {

    public static void main(String[] args) {
        // create Options object
        Options options = new Options();

        // add option "-c"
        options.addOption("c", true, "centre letter");
        // add option "-w"
        options.addOption("w", true, "wheel letters");
        // add option "-9"
        options.addOption("9", false, "list nine letter words");
        // add option "-t"
        options.addOption("t", false, "list total of valid words found");
        // add option "-l"
        options.addOption("l", false, "list valid words found");
        // add option "-s"
        options.addOption("s", false, "display statistics found");

        //***Parsing Stage***
        //Create a parser
        CommandLineParser parser = new DefaultParser();

        //parse the options passed as command line arguments
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException parseException) {
            System.out.println("ParseException " +parseException.getMessage());
            parseException.printStackTrace();
            printArgs();
        }
        if (cmd == null) {
            System.out.println("Null object produced on parsing.");
            printArgs();
        } else if (cmd.hasOption("c") && (cmd.hasOption("w"))) {
            String centre = cmd.getOptionValue("c").toUpperCase();
            String wheel = cmd.getOptionValue("w").toUpperCase();
            System.out.println("centre : " + centre + ", wheel : " + wheel);
            try {
                WordWheel wordWheel = new WordWheel();
                wordWheel.findWords(centre + wheel);
                Set<String> validWords = wordWheel.getValidWords();
                if (cmd.hasOption("9")) {
                    System.out.println("CL looking for nine letter words");
                    List<String> words = wordWheel.findNineLetterWords();
                    if ((words == null) || words.isEmpty()) {
                        System.out.println("No nine letter words found");
                    } else {
                        for (String word : words) {
                            System.out.println(word);
                        }
                    }
                }
                if (cmd.hasOption("t")) {
                    System.out.println("Found " +validWords.size() + " valid words");
                }
                if (cmd.hasOption("s")) {
                    System.out.println("Stats on words found");
                    for (int idx = 2; idx < 9; idx++ ) {
                        int wordCount = 0;
                        for (String word : validWords) {
                            if (word.length() == idx) {
                                wordCount +=1;
                            }
                        }
                        System.out.println(idx +" - " +wordCount);
                    }
                }
                if (cmd.hasOption("l")) {
                    System.out.println("Words found");
                    for (int idx = 2; idx < 9; idx++ ) {
                        System.out.println("Words of " +idx +" characters in length.");
                        int lineWordCount = 0;
                        for (String word : validWords) {
                            if (word.length() == idx) {
                                System.out.print(word +" ");
                                lineWordCount +=1;
                                if (lineWordCount %8 == 0) {
                                    System.out.println();
                                }
                            }
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
            } catch (PermutateStringException permutateStringException) {
                System.out.println(permutateStringException.getMessage());
                printArgs();
            }
        } else  {
            System.err.println("Error, mandatory command line arguments not present.");
            printArgs();
        }
    }

    public static void printArgs() {
        System.out.println("-c and -w mandatory options, all other optional");
        System.out.println("syntax java org.goldendogconsulting.com -c <Centre letter> -w <Wheel letters> -9 -t -l -s ");
        System.out.println("\t-c - Centre Letter");
        System.out.println("\t-w - Wheel letters, must be 8 letters, no white space non alpha characters");
        System.out.println("\t-9 - list nine letter words found");
        System.out.println("\t-t - give total number of valid words found");
        System.out.println("\t-l - list the words found, ordered by size, minimum word length is 2 characters");
        System.out.println("\t-s - give stats on all valid words");
    }
}
