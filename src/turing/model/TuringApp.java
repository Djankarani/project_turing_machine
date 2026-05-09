package turing.model;

import java.io.*;
import java.util.*;

public class TuringApp {
    private static Map<String, TuringMachine> machines = new HashMap<>();
    private static String currentFilePath = null;
    private static boolean hasUnsavedChanges = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Turing Machine CLI Started. Type 'help' for commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] tokens = input.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            try {
                if (cmd.equals("exit")) {
                    System.out.println("Exiting the program...");
                    break;
                } else if (cmd.equals("help")) {
                    printHelp();
                    continue;
                } else if (cmd.equals("open") && tokens.length > 1) {
                    openFile(tokens[1]);
                    continue;
                }

                if (currentFilePath == null && !cmd.equals("open")) {
                    System.out.println("Error: No file is opened. Use 'open <file>' first.");
                    continue;
                }

                processMachineCommand(cmd, tokens);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void processMachineCommand(String cmd, String[] tokens) throws Exception {
        switch (cmd) {
            case "close":
                machines.clear();
                currentFilePath = null;
                hasUnsavedChanges = false;
                System.out.println("Successfully closed file.");
                break;
            case "save":
                saveFile(currentFilePath);
                break;
            case "saveas":
                if (tokens.length > 1) saveFile(tokens[1]);
                else System.out.println("Error: Specify a file path.");
                break;
            case "list":
                System.out.println("Loaded machines: " + machines.keySet());
                break;
            case "newtm":
                if (tokens.length < 2) throw new Exception("Usage: newTM <name>");
                String id = UUID.randomUUID().toString().substring(0, 8);
                machines.put(id, new TuringMachine(id, tokens[1]));
                hasUnsavedChanges = true;
                System.out.println("Created new TM with ID: " + id);
                break;
            case "print":
                getMachine(tokens[1]).printInfo();
                break;
            case "addstate":
                getMachine(tokens[1]).addState(tokens[2]);
                hasUnsavedChanges = true;
                System.out.println("Added state.");
                break;
            case "setstart":
                getMachine(tokens[1]).setStart(tokens[2]);
                hasUnsavedChanges = true;
                System.out.println("Start state set.");
                break;
            case "addaccept":
                getMachine(tokens[1]).addAccept(tokens[2]);
                hasUnsavedChanges = true;
                System.out.println("Accept state added.");
                break;
            case "addreject":
                getMachine(tokens[1]).addReject(tokens[2]);
                hasUnsavedChanges = true;
                System.out.println("Reject state added.");
                break;
            case "addtrans":
                if (tokens.length < 7) throw new Exception("Usage: addTrans <id> <q> <read> <q2> <write> <move>");
                getMachine(tokens[1]).addTransition(
                        tokens[2], tokens[3].charAt(0), tokens[4], tokens[5].charAt(0), Direction.fromString(tokens[6])
                );
                hasUnsavedChanges = true;
                System.out.println("Transition added.");
                break;
            case "removetrans":
                getMachine(tokens[1]).removeTransition(tokens[2], tokens[3].charAt(0));
                hasUnsavedChanges = true;
                System.out.println("Transition removed.");
                break;
            case "checkdet":
                System.out.println("Machine " + tokens[1] + " is deterministic by architecture.");
                break;
            case "init":
                getMachine(tokens[1]).init(tokens.length > 2 ? tokens[2] : "");
                System.out.println("Machine initialized.");
                break;
            case "step":
                getMachine(tokens[1]).step();
                System.out.println(getMachine(tokens[1]).getStatus());
                break;
            case "run":
                getMachine(tokens[1]).run(extractMax(tokens));
                System.out.println(getMachine(tokens[1]).getStatus());
                break;
            case "status":
                System.out.println(getMachine(tokens[1]).getStatus());
                break;
            case "tape":
                System.out.println("Tape: " + getMachine(tokens[1]).getFullTapeString());
                break;
            case "reset":
                getMachine(tokens[1]).resetExecution();
                System.out.println("Execution reset.");
                break;
            case "accepts":
                TuringMachine tm = getMachine(tokens[1]);
                tm.init(tokens[2]);
                tm.run(extractMax(tokens));
                System.out.println("Accepts: " + tm.doesAccept());
                break;
            default:
                System.out.println("Unknown command. Type 'help'.");
        }
    }


    @SuppressWarnings("unchecked")
    private static void openFile(String filepath) throws Exception {
        File f = new File(filepath);
        if (!f.exists()) {
            f.createNewFile();
            machines.clear();
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                machines = (Map<String, TuringMachine>) ois.readObject();
            }
        }
        currentFilePath = filepath;
        hasUnsavedChanges = false;
        System.out.println("Successfully opened " + filepath);
    }

    private static void saveFile(String filepath) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(machines);
        }
        currentFilePath = filepath;
        hasUnsavedChanges = false;
        System.out.println("Successfully saved " + filepath);
    }

    private static TuringMachine getMachine(String id) throws Exception {
        if (!machines.containsKey(id)) throw new Exception("Machine with ID " + id + " not found.");
        return machines.get(id);
    }

    private static int extractMax(String[] tokens) {
        for (String t : tokens) {
            if (t.startsWith("max=")) {
                return Integer.parseInt(t.substring(4));
            }
        }
        return -1;
    }

    private static void printHelp() {
        System.out.println("=== Global Commands ===");
        System.out.println("open <file>  - opens a workspace file");
        System.out.println("close        - closes currently opened file");
        System.out.println("save         - saves the currently open file");
        System.out.println("save as <f>  - saves the file in <f>");
        System.out.println("exit         - exits program");
        System.out.println("=== TM Commands (require open file) ===");
        System.out.println("newTM <name>");
        System.out.println("list, print <id>");
        System.out.println("addState <id> <state>, setStart <id> <state>");
        System.out.println("addAccept/addReject <id> <state>");
        System.out.println("addTrans <id> <q> <read> <q2> <write> <move>");
        System.out.println("init <id> <input>, step <id>, run <id> [max=<n>]");
        System.out.println("status <id>, tape <id>, reset <id>, accepts <id> <word> [max=<n>]");
    }
}
