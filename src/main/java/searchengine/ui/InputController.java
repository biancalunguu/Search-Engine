package searchengine.ui;

import java.util.Scanner;

/**
 * Captures user input from the command line.
 *
 * In a real GUI this would listen for keystrokes in real-time and apply
 * debounce logic. In a CLI, we simply read a full line on Enter.
 */
public class InputController {

    private final Scanner scanner = new Scanner(System.in);

    /** Prints the prompt and waits for the user to type a query. */
    public String readQuery() {
        System.out.print("Search > ");
        return scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
