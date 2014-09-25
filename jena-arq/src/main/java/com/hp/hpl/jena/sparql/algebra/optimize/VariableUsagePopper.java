package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.Collection;

import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * An after visitor for tracking variable usage
 * 
 */
public class VariableUsagePopper extends VariableUsageVisitor {

    public VariableUsagePopper(VariableUsageTracker tracker) {
        super(tracker);
    }

    @Override
    protected void action(Collection<Var> vars) {
        this.tracker.decrement(vars);
    }

    @Override
    protected void action(Var var) {
        this.tracker.decrement(var);
    }

    @Override
    protected void action(String var) {
        this.tracker.decrement(var);
    }

    @Override
    public void visit(OpProject opProject) {
        super.visit(opProject);
        this.tracker.pop();
        super.visit(opProject);
    }
}