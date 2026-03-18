package turing.cli;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;
import turing.model.Transition;

public class CommandLine {
    private boolean isRunning = true;
    private boolean isFileOpened = false;
    private String currentFileName = "";

    private Map<String, TuringMachine> loadedMachines = new HashMap<>();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Turing Machine: Command Line Interface ---");
        System.out.println("Type 'help' for a list of all commands.");

        while (isRunning) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            if (cmd.equals("save") && parts.length > 1 && parts[1].equalsIgnoreCase("as")) {
                if (parts.length < 3) {
                    System.out.println("Error: Please provide a filename! (save as <file>)");
                } else {
                    String fileName = parts[2];
                    System.out.println("Saving everything to a new file: " + fileName);
                }
                continue;
            }
            switch (cmd) {
                case "open":
                    if (parts.length < 2) {
                        System.out.println("Usage: open <filename>");
                    } else {
                        currentFileName = parts[1];
                        isFileOpened = true;
                        System.out.println("File '" + currentFileName + "' opened successfully.");
                    }
                    break;
                case "close":
                    if (isFileOpened) {
                        System.out.println("Closing file: " + currentFileName);
                        isFileOpened = false;
                        currentFileName = "";
                    } else {
                        System.out.println("No file is currently open.");
                    }
                    break;
                case "save":
                    if (isFileOpened) {
                        System.out.println("Changes saved to " + currentFileName);
                    } else {
                        System.out.println("Please open a file first!");
                    }
                    break;
                case "help":
                    printHelp();
                    break;
                case "exit":
                    System.out.println("Exiting... See you later!");
                    isRunning = false;
                    break;
                case "list":
                    System.out.println("Listing all TMs in the current file...");
                    break;
                case "print":
                    if (parts.length < 2) System.out.println("Usage: print <id>");
                    else System.out.println("Printing details for machine ID: " + parts[1]);
                    break;
                case "newtm":
                    if (parts.length < 2) System.out.println("Usage: newtm <id>");
                    else {
                        loadedMachines.put(parts[1], new TuringMachine(parts[1]));
                        System.out.println("Created new empty machine: " + parts[1]);
                    }
                    break;
                case "addstate":
                    if (parts.length < 3) System.out.println("Usage: addstate <id> <state>");
                    else {
                        TuringMachine tm = loadedMachines.get(parts[1]);
                        if (tm != null) {
                            tm.addState(parts[2]);
                            System.out.println("Added state '" + parts[2] + "' to machine " + parts[1]);
                        } else System.out.println("Machine not found.");
                    }
                    break;
                case "addtrans":
                    if (parts.length < 7) {
                        System.out.println("Usage: addtrans <id> <q1> <char> <q2> <char_new> <dir>");
                    } else {
                        TuringMachine tm = loadedMachines.get(parts[1]);
                        if (tm != null) {
                            Transition trans = new Transition(parts[4], parts[5].charAt(0), parts[6].charAt(0));
                            tm.addTransition(parts[2], parts[3].charAt(0), trans);
                            System.out.println("Transition added.");
                        } else System.out.println("Machine not found.");
                    }
                    break;
                case "setstart":
                    if (parts.length < 3) System.out.println("Usage: setstart <id> <state>");
                    else System.out.println("Start state for " + parts[1] + " set to: " + parts[2]);
                    break;
                case "run":
                    if (parts.length < 2) System.out.println("Usage: run <id>");
                    else System.out.println("Starting machine " + parts[1] + "...");
                    break;
                case "step":
                    if (parts.length < 2) System.out.println("Usage: step <id>");
                    else System.out.println("Executing one step for machine " + parts[1]);
                    break;
                case "status":
                    if (parts.length < 2) System.out.println("Usage: status <id>");
                    else System.out.println("Current status of " + parts[1] + ": Awaiting command.");
                    break;
                case "tape":
                    if (parts.length < 2) System.out.println("Usage: tape <id>");
                    else System.out.println("Tape of " + parts[1] + ": [ _ _ a b a _ _ ]");
                    break;
                case "init":
                    if (parts.length < 3) System.out.println("Usage: init <id> <word>");
                    else System.out.println("Machine " + parts[1] + " initialized with word: " + parts[2]);
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for assistance.");
                    break;
            }
        }
        scanner.close();
    }

    private void printHelp() {
        System.out.println("\n--- AVAILABLE COMMANDS ---");
        System.out.println("Files:    open <f>, close, save, save as <f>, exit");
        System.out.println("Machines: list, print <id>, newtm <id>, addstate <id> <s>");
        System.out.println("Trans:    addtrans <id> <q> <c> <q2> <c2> <d>, removetrans <id> <q> <c>");
        System.out.println("Control:  setstart, addaccept, addreject, init <id> <word>");
        System.out.println("Action:   run <id>, step <id>, status <id>, tape <id>, reset <id>");
        System.out.println("Analyze:  checkdet <id>, accepts <id> <word>, trace, report\n");
    }
}