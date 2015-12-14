package hangman;

import java.util.*;
import java.io.*;

/** EvilHangmanGame
 *
 * class that implements a game of Evil Hangman.
 *
 */
public class EvilHangmanGame implements IEvilHangmanGame {

    public static class EmptyDictionaryException extends Exception {}
    public static class BadGuessException extends Exception {
        public BadGuessException(String message) { super(message); }
    }

    private Set<String> possibleWords;
    private Map<String, Set<String>> partitions;
    private String currentPattern;
    private Set<Character> guessedLetters;
    private int wordLength;
    private int numGuesses;
    private boolean initialized;
    private boolean end;

    public EvilHangmanGame() {
        possibleWords = null;
        partitions = null;
        currentPattern = null;
        guessedLetters = new TreeSet<>();
        wordLength = 0;
        numGuesses = 0;
        end = false;
        initialized = false;
    }

    public void playGame(File dictionary, int wordLength, int numGuesses) {
        startGame(dictionary, wordLength);
        this.numGuesses = numGuesses;
        if (initialized) {
            while (!this.end) { doTurn(); }
            endGame();
        }
    }

    @Override
    public void startGame(File dictionary, int wordLength) {
        initialized = true;
        Scanner inFile = null;
        try {
            inFile = new Scanner(new BufferedInputStream(new FileInputStream(dictionary)));

            this.possibleWords = new TreeSet<>();
            this.wordLength = wordLength;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < wordLength; i++) {
                sb.append("_");
            }
            this.currentPattern = sb.toString();

            while(inFile.hasNext()) {
                String nextWord = inFile.next();
                nextWord = nextWord.toLowerCase();

                boolean add = true;
                for (int i = 0; i < nextWord.length(); i++) {
                    if(!Character.isAlphabetic(nextWord.charAt(i))) { add = false; }
                    else if (nextWord.length() != this.wordLength) { add = false; }
                }

                if (add) { possibleWords.add(nextWord); }
            }

            if (possibleWords.isEmpty()) {
                throw new EmptyDictionaryException();
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Dictionary file not found :(");
            initialized = false;
        }
        catch (EmptyDictionaryException e) {
            System.out.println("No possible words with that word length :(");
            initialized = false;
        }
        finally {
            if (inFile != null) { inFile.close(); }
        }
    }

    public void doTurn() {

        // Print current info and get input
        System.out.println(currentPattern);
        System.out.print("Guessed letters: ");
        for (Character c : guessedLetters) { System.out.print(c + " "); }
        System.out.println();
        System.out.println("Guesses Remaining: " + numGuesses);
        System.out.print("Please enter a letter: ");
        Scanner in = new Scanner(System.in);

        boolean validGuess; // Check for valid guess using BadGuessException and GuessAlreadyMadeException
        do {
            try {
                String input = in.nextLine().toLowerCase();
                System.out.println();
                char guess;

                // Check that input is a valid guess (single alphabetic character)
                if (validGuess(input)) {
                    validGuess = true;
                    guess = input.charAt(0);
                }
                else { throw new BadGuessException("Please enter a single letter from a-z: "); }

                // Make the guess
                possibleWords = makeGuess(guess);
                guessedLetters.add(guess);

                // Count number of letters in the new pattern and report
                int count = guessedLetterCount(guess);
                if (count == 0) {
                    System.out.println("Sorry, there are no \'" + guess + "\'s");
                    numGuesses--;
                }
                else if (count == 1) { System.out.println("Yes, there is " + count + " \'" + guess + "\'"); }
                else { System.out.println("Yes, there are " + count + " \'" + guess + "\'s"); }

                // End game?
                if (numGuesses == 0) { this.end = true; }
                if (!currentPattern.contains("_")) { this.end = true; }

            } catch (GuessAlreadyMadeException e) {
                validGuess = false;
            } catch (BadGuessException e) {
                validGuess = false;
                System.out.print(e.getMessage());
            }
        } while (!validGuess);
    }

    public boolean validGuess(String input) {
        boolean valid = true;

        // More than one letter entered
        if (input.length() != 1) { valid = false; }
        // Non-alphabet character
        else if (!Character.isAlphabetic(input.charAt(0))) { valid = false; }

        return valid;
    }

    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        // Already been guessed?
        for (Character c : guessedLetters) {
            if (c.equals(guess)) {
                System.out.println("\"" + c + "\" has already been guessed.");
                throw new GuessAlreadyMadeException();
            }
        }

        // Partition possible words
        partitions = new TreeMap<>();
        for (String s : possibleWords) {
            getPartitions(s, guess);
        }
        Set<String> largestSet = null;
        int largestSetSize = 0;

        // Choose best partition
        for (Map.Entry<String, Set<String>> p : partitions.entrySet()) {

            if (betterPartition(p, largestSetSize, guess)) {
                currentPattern = p.getKey();
                largestSet = p.getValue();
                largestSetSize = p.getValue().size();
            }

        }

        possibleWords = largestSet;
        return largestSet;
    }

    public void getPartitions(String s, char guess) {

        // Get pattern
        StringBuilder sb = new StringBuilder();
        String pattern;
        for (int i = 0; i < wordLength; i++) {
            if (s.charAt(i) == guess) { sb.append(guess); }
            else { sb.append(currentPattern.charAt(i)); }
        }
        pattern = sb.toString();

        if (partitions.containsKey(pattern)) {
            // Partition already exists, add word
            Set<String> partition = partitions.get(pattern);
            partition.add(s);
            partitions.put(s, partition);

        }
        else {
            // Create partition and add word
            Set<String> patternSet = new TreeSet<>();
            patternSet.add(s);
            partitions.put(pattern, patternSet);
        }
    }

    public boolean betterPartition (Map.Entry<String, Set<String>> pattern, int largestSetSize, char guess) {
        // Bigger set?
        boolean better = (pattern.getValue().size() > largestSetSize);

        // Smaller # of letters?
        if (!better) {
            boolean equal = (pattern.getValue().size() == largestSetSize);
            int numCharsP = wordLength - pattern.getKey().replace(Character.toString(guess), "").length();
            int numCharsCurr = wordLength - currentPattern.replace(Character.toString(guess), "").length();
            boolean lessChars = numCharsP < numCharsCurr;
            better = equal && lessChars;
        }

        // rightmost letter?
        if (!better) {
            int currLetterPos = wordLength;
            int pLetterPos = wordLength;

            boolean change = true;
            while (pLetterPos == currLetterPos && change) {
                change = false;
                for (int i = currLetterPos - 1; i >=0; i--) {
                    if (currentPattern.charAt(i) == guess && currLetterPos == -1) {
                        currLetterPos = i;
                        change = true;
                    }
                    if (pattern.getKey().charAt(i) == guess && pLetterPos == -1) {
                        pLetterPos = i;
                        change = true;
                    }
                }
            }

            better = pLetterPos > currLetterPos;
        }

        return better;
    }

    public int guessedLetterCount(char guess) {
        int count = 0;

        for (int i = 0; i < wordLength; i++) {
            if (currentPattern.charAt(i) == guess) { count++; }
        }

        return count;
    }

    public void endGame() {
        if (currentPattern.contains("_")) {
            System.out.println("Sorry, you lost :P - Word: " + possibleWords.iterator().next());
        }
        else {
            System.out.println("Aww, you win!! :( - Word: " + currentPattern);
        }
    }
}
