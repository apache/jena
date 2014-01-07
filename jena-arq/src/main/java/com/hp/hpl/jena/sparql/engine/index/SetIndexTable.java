/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package com.hp.hpl.jena.sparql.engine.index;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * An {@link IndexTable} implementation optimized for the case where there is
 * only a single common variable
 * 
 */
public class SetIndexTable implements IndexTable {

    private Var var;
    private Set<Node> values = new HashSet<Node>();

    /**
     * Creates a new index table
     * 
     * @param commonVars
     *            Common Variables
     * @param data
     *            Data
     */
    public SetIndexTable(Set<Var> commonVars, QueryIterator data) {
        if (commonVars.size() != 1)
            throw new IllegalArgumentException("Common Variables must be of size 1");

        this.var = commonVars.iterator().next();
        while (data.hasNext()) {
            Binding binding = data.next();
            Node value = binding.get(this.var);

            if (value == null)
                continue;
            this.values.add(value);
        }
    }

    @Override
    public boolean containsCompatibleWithSharedDomain(Binding binding) {
        Node value = binding.get(this.var);
        if (value == null)
            return true;
        return this.values.contains(value);
    }

}
