package turing.model;

public interface Computable {
    void init(String input);
    boolean step();
    void run(int maxSteps);
    String getStatus();
    void resetExecution();
}