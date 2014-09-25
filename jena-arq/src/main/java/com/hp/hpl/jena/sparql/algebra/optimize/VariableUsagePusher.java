package com.hp.hpl.jena.sparql.algebra.optimize;

import java.util.Collection;

import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * A before visitor for tracking variable usage
 * 
 */
public class VariableUsagePusher extends VariableUsageVisitor {

    public VariableUsagePusher(VariableUsageTracker tracker) {
        super(tracker);
    }

    @Override
    protected void action(Collection<Var> vars) {
        this.tracker.increment(vars);
    }

    @Override
    protected void action(Var var) {
        this.tracker.increment(var);
    }

    @Override
    protected void action(String var) {
        this.tracker.increment(var);
    }

    @Override
    public void visit(OpProject opProject) {
        super.visit(opProject);
        this.tracker.push();
        super.visit(opProject);
    }

    
}