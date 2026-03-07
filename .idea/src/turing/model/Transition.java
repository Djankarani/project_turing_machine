package turing.model;

public class Transition {
    private String nextState;

    private char writeSymbol;

    private char moveDirection;

    public Transition(String nextState, char writeSymbol, char moveDirection) {
        this.nextState = nextState;
        this.writeSymbol = writeSymbol;
        this.moveDirection = moveDirection;
    }

    public String getNextState() {
        return nextState;
    }

    public char getWriteSymbol() {
        return writeSymbol;
    }

    public char getMoveDirection() {
        return moveDirection;
    }

    @Override
    public String toString() {
        return String.format("(%s, %c, %c)", nextState, writeSymbol, moveDirection);
    }
}