package turing.model;

import java.io.Serializable;

public abstract class BaseMachine implements Computable, Serializable {
    protected String id;
    protected String name;

    public BaseMachine(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public abstract void printInfo();
}

