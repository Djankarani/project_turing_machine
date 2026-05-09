package turing.model;

public enum Direction {
    L, R, S;

    public static Direction fromString(String s) {
        switch (s.toUpperCase()) {
            case "L": return L;
            case "R": return R;
            case "S": return S;
            default: throw new IllegalArgumentException("Invalid direction: " + s);
        }
    }
}
