package turing.model;

import java.io.Serializable;
import java.util.*;

public class TuringMachine extends BaseMachine {
    private final Set<String> states = new HashSet<>();
    private String startState;
    private final Set<String> acceptStates = new HashSet<>();
    private final Set<String> rejectStates = new HashSet<>();
    private final Map<String, Map<Character, Transition>> transitions = new HashMap<>();

    private transient List<Character> tape;
    private transient int head;
    private transient String currentState;
    private transient boolean isHalted;
    private transient int stepsCount;

    public TuringMachine(String id, String name) {
        super(id, name);
        resetExecution();
    }


    public void addState(String state) { states.add(state); }
    public void setStart(String state) { this.startState = state; addState(state); }
    public void addAccept(String state) { acceptStates.add(state); addState(state); }
    public void addReject(String state) { rejectStates.add(state); addState(state); }

    public void addTransition(String fromState, char readSym, String toState, char writeSym, Direction move) {
        addState(fromState);
        addState(toState);
        transitions.computeIfAbsent(fromState, k -> new HashMap<>())
                .put(readSym, new Transition(toState, writeSym, move));
    }

    public void removeTransition(String state, char readSym) {
        if (transitions.containsKey(state)) {
            transitions.get(state).remove(readSym);
        }
    }

    @Override
    public void init(String input) {
        tape = new ArrayList<>();
        for (char c : input.toCharArray()) tape.add(c);
        if (tape.isEmpty()) tape.add('_');
        head = 0;
        currentState = startState;
        isHalted = false;
        stepsCount = 0;
    }

    @Override
    public void resetExecution() {
        init("");
    }

    private char readTape() {
        if (head < 0 || head >= tape.size()) return '_';
        return tape.get(head);
    }

    private void writeTape(char c) {
        while (head < 0) { tape.add(0, '_'); head++; }
        while (head >= tape.size()) { tape.add('_'); }
        tape.set(head, c);
    }

    @Override
    public boolean step() {
        if (isHalted || currentState == null) return false;

        if (acceptStates.contains(currentState) || rejectStates.contains(currentState)) {
            isHalted = true;
            return false;
        }

        char readSym = readTape();
        Map<Character, Transition> stateTrans = transitions.get(currentState);

        if (stateTrans == null || !stateTrans.containsKey(readSym)) {
            isHalted = true; // Няма дефиниран преход
            return false;
        }

        Transition t = stateTrans.get(readSym);
        writeTape(t.writeSymbol);
        currentState = t.nextState;

        if (t.move == Direction.R) head++;
        else if (t.move == Direction.L) head--;

        stepsCount++;
        return true;
    }

    @Override
    public void run(int maxSteps) {
        int steps = 0;
        while (!isHalted && (maxSteps == -1 || steps < maxSteps)) {
            if (!step()) break;
            steps++;
        }
    }

    @Override
    public String getStatus() {
        if (currentState == null) return "Not initialized.";
        String status = "Running";
        if (isHalted) {
            if (acceptStates.contains(currentState)) status = "Accepted";
            else if (rejectStates.contains(currentState)) status = "Rejected";
            else status = "Halted (No transition)";
        }
        return String.format("State: %s, Head: %d, Status: %s, Steps: %d", currentState, head, status, stepsCount);
    }

    @Override
    public void printInfo() {
        System.out.println("TM ID: " + id + ", Name: " + name);
        System.out.println("States: " + states);
        System.out.println("Start: " + startState);
        System.out.println("Accept: " + acceptStates);
        System.out.println("Reject: " + rejectStates);
        System.out.println("Transitions:");
        for (String q : transitions.keySet()) {
            for (Map.Entry<Character, Transition> e : transitions.get(q).entrySet()) {
                System.out.printf("  d(%s, %c) = %s\n", q, e.getKey(), e.getValue().toString());
            }
        }
    }

    public String getFullTapeString() {
        if (tape == null || tape.isEmpty()) return "_";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tape.size(); i++) {
            if (i == head) sb.append("[").append(tape.get(i)).append("]");
            else sb.append(tape.get(i));
        }
        return sb.toString();
    }


    public boolean doesAccept() {
        return isHalted && acceptStates.contains(currentState);
    }


    private static class Transition implements Serializable {
        String nextState;
        char writeSymbol;
        Direction move;

        public Transition(String nextState, char writeSymbol, Direction move) {
            this.nextState = nextState;
            this.writeSymbol = writeSymbol;
            this.move = move;
        }

        @Override
        public String toString() {
            return "(" + nextState + ", " + writeSymbol + ", " + move + ")";
        }
    }
}
