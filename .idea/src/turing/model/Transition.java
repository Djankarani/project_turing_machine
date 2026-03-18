package turing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TuringMachine {
    private String id;
    private String startState;
    private Set<String> states;
    private Set<String> acceptStates;
    private Set<String> rejectStates;

    private Map<String, Map<Character, Transition>> transitions;

    public TuringMachine(String id) {
        this.id = id;
        this.states = new HashSet<>();
        this.acceptStates = new HashSet<>();
        this.rejectStates = new HashSet<>();
        this.transitions = new HashMap<>();
    }

    public void addState(String state) {
        states.add(state);
    }

    public void setStart(String state) {
        if (states.contains(state)) {
            this.startState = state;
        }
    }

    public void addAccept(String state) {
        if (states.contains(state)) acceptStates.add(state);
    }

    public void addReject(String state) {
        if (states.contains(state)) rejectStates.add(state);
    }

    public void addTransition(String fromState, char readSymbol, Transition trans) {
        if (!states.contains(fromState) || !states.contains(trans.getNextState())) {
            return;
        }
        if (!transitions.containsKey(fromState)) {
            transitions.put(fromState, new HashMap<>());
        }
        transitions.get(fromState).put(readSymbol, trans);
    }

    public String getId() { return id; }
    public String getStartState() { return startState; }
    public Set<String> getStates() { return states; }

    @Override
    public String toString() {
        return "TM ID: " + id + ", States: " + states.size() + ", Transitions: " + transitions.size();
    }
}