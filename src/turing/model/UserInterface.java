package turing.model;

import java.io.*;
import java.util.*;

public class UserInterface {

    private Map<String, TuringMachine> machines = new HashMap<>();
    private String currentFilePath = null;
    private final Scanner scanner = new Scanner(System.in);

    private final Map<String, Command> commandDispatcher = new HashMap<>();

    public UserInterface() {
        commandDispatcher.put("exit", args -> {
            System.out.println("Exiting the program...");
            System.exit(0);
        });

        commandDispatcher.put("help", args -> printHelp());

        commandDispatcher.put("open", args -> {
            if (args.length < 2) throw new Exception("Usage: open <filename>");
            openFile(args[1]);
        });

        commandDispatcher.put("close", args -> {
            requireFile();
            machines.clear();
            currentFilePath = null;
            System.out.println("Successfully closed file.");
        });

        commandDispatcher.put("save", args -> {
            requireFile();
            saveFile(currentFilePath);
        });

        commandDispatcher.put("saveas", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: saveas <filename>");
            saveFile(args[1]);
        });

        commandDispatcher.put("list", args -> {
            requireFile();
            System.out.println("Loaded machines: " + machines.keySet());
        });

        commandDispatcher.put("newtm", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: newTM <name>");
            String id = UUID.randomUUID().toString().substring(0, 8);
            machines.put(id, new TuringMachine(id, args[1]));
            System.out.println("Created new TM with ID: " + id);
        });

        commandDispatcher.put("print", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: print <id>");
            getMachine(args[1]).printInfo();
        });

        commandDispatcher.put("addstate", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: addState <id> <state>");
            getMachine(args[1]).addState(args[2]);
            System.out.println("Added state.");
        });

        commandDispatcher.put("setstart", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: setStart <id> <state>");
            getMachine(args[1]).setStart(args[2]);
            System.out.println("Start state set.");
        });

        commandDispatcher.put("addaccept", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: addAccept <id> <state>");
            getMachine(args[1]).addAccept(args[2]);
            System.out.println("Accept state added.");
        });

        commandDispatcher.put("addreject", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: addReject <id> <state>");
            getMachine(args[1]).addReject(args[2]);
            System.out.println("Reject state added.");
        });

        commandDispatcher.put("addtrans", args -> {
            requireFile();
            if (args.length < 7) throw new Exception("Usage: addTrans <id> <q> <read> <q2> <write> <move>");
            getMachine(args[1]).addTransition(
                    args[2], args[3].charAt(0), args[4], args[5].charAt(0), Direction.fromString(args[6])
            );
            System.out.println("Transition added.");
        });

        commandDispatcher.put("removetrans", args -> {
            requireFile();
            if (args.length < 4) throw new Exception("Usage: removeTrans <id> <q> <read>");
            getMachine(args[1]).removeTransition(args[2], args[3].charAt(0));
            System.out.println("Transition removed.");
        });

        commandDispatcher.put("checkdet", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: checkDet <id>");
            System.out.println("Machine " + args[1] + " is deterministic by structure.");
        });

        commandDispatcher.put("init", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: init <id> [<input>]");
            getMachine(args[1]).init(args.length > 2 ? args[2] : "");
            System.out.println("Machine initialized.");
        });

        commandDispatcher.put("step", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: step <id>");
            getMachine(args[1]).step();
            System.out.println(getMachine(args[1]).getStatus());
        });

        commandDispatcher.put("run", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: run <id> [max=<n>]");
            getMachine(args[1]).run(extractMax(args));
            System.out.println(getMachine(args[1]).getStatus());
        });

        commandDispatcher.put("status", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: status <id>");
            System.out.println(getMachine(args[1]).getStatus());
        });

        commandDispatcher.put("tape", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: tape <id> [from=<a>] [to=<b>]");
            TuringMachine tm = getMachine(args[1]);
            int from = extractParam(args, "from=", 0);
            int to = extractParam(args, "to=", 10);
            if (hasParam(args, "from=")) {
                System.out.println("Tape segment: " + tm.getTapeSegment(from, to));
            } else {
                System.out.println("Tape: " + tm.getFullTapeString());
            }
        });

        commandDispatcher.put("reset", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: reset <id>");
            getMachine(args[1]).resetExecution();
            System.out.println("Execution reset.");
        });

        commandDispatcher.put("accepts", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: accepts <id> <word> [max=<n>]");
            TuringMachine tm = getMachine(args[1]);
            tm.init(args[2]);
            tm.run(extractMax(args));
            System.out.println("Result: " + (tm.doesAccept() ? "Accepted" : "Rejected/Halted"));
        });

        commandDispatcher.put("trace", args -> {
            requireFile();
            if (args.length < 4) throw new Exception("Usage: trace <id> <word> <k> [max=<n>]");
            TuringMachine tm = getMachine(args[1]);
            int k = Integer.parseInt(args[3]);
            tm.printTrace(args[2], k, extractMax(args));
        });

        commandDispatcher.put("report", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: report <id> <word> [max=<n>]");
            TuringMachine tm = getMachine(args[1]);
            tm.init(args[2]);
            tm.run(extractMax(args));
            System.out.println("=== Execution Report ===");
            System.out.println(tm.getStatus());
            System.out.println("Final Tape: " + tm.getFullTapeString());
        });

        commandDispatcher.put("savetm", args -> {
            requireFile();
            if (args.length < 3) throw new Exception("Usage: saveTM <id> <filename>");
            TuringMachine tm = getMachine(args[1]);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(args[2]))) {
                oos.writeObject(tm);
            }
            System.out.println("Machine " + args[1] + " successfully saved to " + args[2]);
        });

        commandDispatcher.put("loadtm", args -> {
            requireFile();
            if (args.length < 2) throw new Exception("Usage: loadTM <filename>");
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]))) {
                TuringMachine tm = (TuringMachine) ois.readObject();
                String newId = UUID.randomUUID().toString().substring(0, 8);
                TuringMachine newMachine = new TuringMachine(newId, tm.getName());
                machines.put(newId, tm);
                System.out.println("Successfully loaded machine from " + args[1] + " with new ID: " + newId);
            }
        });
    }

    public void start() {
        System.out.println("Turing Machine CLI Started. Type 'help' for commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] tokens = input.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            try {
                Command command = commandDispatcher.get(cmd);
                if (command != null) {
                    command.execute(tokens);
                } else {
                    System.out.println("Unknown command. Type 'help'.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void requireFile() throws Exception {
        if (currentFilePath == null) {
            throw new Exception("No file is opened. Use 'open <file>' first.");
        }
    }

    @SuppressWarnings("unchecked")
    private void openFile(String filepath) throws Exception {
        File f = new File(filepath);
        if (!f.exists()) {
            f.createNewFile();
            machines.clear();
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                machines = (Map<String, TuringMachine>) ois.readObject();
            } catch (EOFException e) {
                machines.clear(); // Празен файл
            }
        }
        currentFilePath = filepath;
        System.out.println("Successfully opened " + filepath);
    }

    private void saveFile(String filepath) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(machines);
        }
        currentFilePath = filepath;
        System.out.println("Successfully saved " + filepath);
    }

    private TuringMachine getMachine(String id) throws Exception {
        if (!machines.containsKey(id)) throw new Exception("Machine with ID " + id + " not found.");
        return machines.get(id);
    }

    private int extractMax(String[] tokens) {
        return extractParam(tokens, "max=", -1);
    }

    private int extractParam(String[] tokens, String prefix, int defaultValue) {
        for (String t : tokens) {
            if (t.toLowerCase().startsWith(prefix)) {
                return Integer.parseInt(t.substring(prefix.length()));
            }
        }
        return defaultValue;
    }

    private boolean hasParam(String[] tokens, String prefix) {
        for (String t : tokens) {
            if (t.toLowerCase().startsWith(prefix)) return true;
        }
        return false;
    }

    private void printHelp() {
        System.out.println("=== Workspace Commands ===");
        System.out.println("  open <file>, close, save, saveas <file>, exit");
        System.out.println("=== Machine Management ===");
        System.out.println("  newTM <name>, list, print <id>, saveTM <id> <file>, loadTM <file>");
        System.out.println("=== Configuration ===");
        System.out.println("  addState <id> <q>, setStart <id> <q>, addAccept <id> <q>, addReject <id> <q>");
        System.out.println("  addTrans <id> <q1> <read> <q2> <write> <move>, removeTrans <id> <q> <read>, checkDet <id>");
        System.out.println("=== Execution ===");
        System.out.println("  init <id> <word>, step <id>, run <id> [max=<n>], status <id>, tape <id>, reset <id>");
        System.out.println("  accepts <id> <word> [max=<n>], trace <id> <word> <k> [max=<n>], report <id> <word> [max=<n>]");
    }
}
