package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.hp.hpl.jena.sparql.core.Var;

/**
 * Tracker for variable usage
 * 
 */
public class VariableUsageTracker {

    private Stack<Map<String, Integer>> stack = new Stack<>();
    private Map<String, Integer> variables = new HashMap<>();

    public void push() {
        this.stack.push(this.variables);
        this.variables = new HashMap<>();
    }

    public void pop() {
        if (this.stack.size() == 0)
            throw new IllegalStateException("Stack is empty");
        this.variables = this.stack.pop();
    }

    public void increment(Collection<Var> vars) {
        for (Var var : vars) {
            increment(var);
        }
    }

    public void increment(String var) {
        if (!variables.containsKey(var)) {
            variables.put(var, 1);
        } else {
            variables.put(var, variables.get(var) + 1);
        }
    }

    public void increment(Var var) {
        increment(var.getName());
    }

    public void decrement(Collection<Var> vars) {
        for (Var var : vars) {
            decrement(var);
        }
    }

    public void decrement(String var) {
        if (variables.containsKey(var)) {
            variables.put(var, variables.get(var) - 1);
            if (variables.get(var) <= 0)
                variables.remove(var);
        }
    }

    public void decrement(Var var) {
        decrement(var.getName());
    }

    public int getUsageCount(String var) {
        Integer i = variables.get(var);
        return i != null ? i.intValue() : 0;
    }

    public int getUsageCount(Var var) {
        return getUsageCount(var.getName());
    }
}