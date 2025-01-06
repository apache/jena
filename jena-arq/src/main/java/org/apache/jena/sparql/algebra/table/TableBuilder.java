/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.algebra.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.ImmutableUniqueList;

/**
 * Builder for immutable instances of {@link Table}.
 * This builder is not thread safe.
 */
public class TableBuilder {
    private ImmutableUniqueList.Builder<Var> varsBuilder = ImmutableUniqueList.newUniqueListBuilder(Var.class);

    private List<Binding> rows = new ArrayList<>();
    private boolean copyRowsOnNextMutation = false;

    // Vars ----

    /** Returns an immutable snapshot of this builder's current variables. */
    public List<Var> snapshotVars() {
        return varsBuilder.build();
    }

    public int sizeVars() {
        return varsBuilder.size();
    }

    public TableBuilder addVar(Var var) {
        varsBuilder.add(var);
        return this;
    }

    public TableBuilder addVars(Collection<Var> vars) {
        varsBuilder.addAll(vars);
        return this;
    }

    public TableBuilder addVars(Iterator<Var> vars) {
        vars.forEachRemaining(varsBuilder::add);
        return this;
    }

    /** Adds the variables of a binding but not the binding itself. */
    public TableBuilder addVarsFromRow(Binding row) {
        row.vars().forEachRemaining(varsBuilder::add);
        return this;
    }

    // Rows -----

    private void copyRowsIfNeeded() {
        if (copyRowsOnNextMutation) {
            rows = new ArrayList<>(rows);
            copyRowsOnNextMutation = false;
        }
    }

    /** Returns an immutable snapshot of this builder's current rows. */
    public List<Binding> snapshotRows() {
        return List.copyOf(rows);
    }

    public int sizeRows() {
        return rows.size();
    }

    public TableBuilder addRow(Binding row) {
        copyRowsIfNeeded();
        rows.add(row);
        return this;
    }

    public TableBuilder addRows(Collection<Binding> newRows) {
        copyRowsIfNeeded();
        rows.addAll(newRows);
        return this;
    }

    public TableBuilder addRows(Iterator<Binding> newRows) {
        copyRowsIfNeeded();
        newRows.forEachRemaining(rows::add);
        return this;
    }

    // Rows and Vars -----

    /** This method assumes prior call to copyRowsIfNeeded(). */
    private void addRowAndVarsInternal(Binding row) {
        addVarsFromRow(row);
        rows.add(row);
    }

    public TableBuilder addRowAndVars(Binding row) {
        copyRowsIfNeeded();
        addRowAndVarsInternal(row);
        return this;
    }

    public TableBuilder addRowsAndVars(Collection<Binding> newRows) {
        copyRowsIfNeeded();
        newRows.forEach(this::addVarsFromRow);
        rows.addAll(newRows);
        return this;
    }

    public TableBuilder addRowsAndVars(Iterator<Binding> newRows) {
        copyRowsIfNeeded();
        newRows.forEachRemaining(this::addRowAndVarsInternal);
        return this;
    }

    /** Add the rows and variables of another table. */
    public TableBuilder addRowsAndVars(Table table) {
        addVars(table.getVars());
        addRows(table.rows());
        return this;
    }

    /**
     * Similar to {@link #addRowsAndVars(Iterator)} but
     * also closes the given QueryIterator when done.
     */
    public TableBuilder consumeRowsAndVars(QueryIterator qIter) {
        Objects.requireNonNull(qIter);
        try {
            addRowsAndVars(qIter);
        } finally {
            qIter.close();
        }
        return this;
    }

    // General -----

    public TableBuilder resetVars() {
        varsBuilder.clear();
        return this;
    }

    public TableBuilder resetRows() {
        if (copyRowsOnNextMutation) {
            rows = new ArrayList<>();
            copyRowsOnNextMutation = false;
        } else {
            rows.clear();
        }
        return this;
    }

    /** Reset variables and rows. */
    public TableBuilder reset() {
        resetVars();
        resetRows();
        return this;
    }

    public Table build() {
        List<Var> finalVars = snapshotVars();
        List<Binding> finalRows = Collections.unmodifiableList(rows);
        copyRowsOnNextMutation = true;
        return new TableData(finalVars, finalRows);
    }
}
