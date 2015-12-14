package hangman;

import java.io.*;

/** RunEvilHangman
 *
 * a class to run an EvilHangmanGame
 *
 */
public class RunEvilHangman {

    public class UsageException extends Exception {
        public UsageException(String message) { super(message); }
    }

    public static void main(String[] args) {
        new RunEvilHangman().run(args);
    }

    public void run(String[] args) {

        try {
            checkUsage(args, "USAGE: java RunEvilHangman dictionary wordLength(>=2) guesses(>=1)");
            EvilHangmanGame g = new EvilHangmanGame();
            g.playGame(new File(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        catch (UsageException e) {
            System.out.println(e.getMessage());
        }

    }

    public void checkUsage(String[] args, String usage) throws UsageException {

        // wrong number of arguments
        if (args.length != 3) { throw new UsageException(usage + " - invalid # of arguments"); }

        // wordLength
        int wordLength;
        try { wordLength = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) { throw new UsageException(usage); }
        if (wordLength < 2) { throw new UsageException(usage + " - wordLength < 2"); }

        // guesses
        int guesses;
        try { guesses = Integer.parseInt(args[2]); }
        catch (NumberFormatException e) { throw new UsageException(usage); }
        if (guesses < 1) { throw new UsageException(usage + " - guesses < 1"); }

    }

}
