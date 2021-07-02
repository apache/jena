/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.syntax.ElementData;

public class ValuesHandler implements Handler {

    private Map<Var, List<Node>> valuesTable;

    /*
     * the map used during build to substitute vars modified by the setVars() method
     */
    private Map<Var, Node> valueMap;

    // the query to modify
    private final Query query;

    /**
     * Constructor.
     */
    public ValuesHandler() {
        this(null);
    }

    public ValuesHandler(Query query) {
        this.query = query;
        this.valuesTable = new LinkedHashMap<Var, List<Node>>();
        this.valueMap = Collections.emptyMap();
    }

    public ElementData asElement() {
        ElementData ed = new ElementData();
        // remove all the vars that have been set
        List<Var> vars = new ArrayList<Var>(valuesTable.keySet());
        vars.removeAll(valueMap.keySet());
        if (vars.isEmpty()) {
            return ed;
        }
        vars.forEach(var -> ed.add(var));

        // List<Binding> bindings = new ArrayList<Binding>();
        int count = valuesTable.get(vars.get(0)).size();
        BindingBuilder builder = Binding.builder();
        for (int i = 0; i < count; i++) {
            builder.reset();
            for (Var var : vars) {
                List<Node> lst = valuesTable.get(var);
                // must be square
                if (lst.size() != count) {
                    throw new QueryBuildException(
                            String.format("The number of data items for %s (%s) is not the same as for %s (%s)", var,
                                    lst.size(), vars.get(0), count));
                }
                Node n = lst.get(i);
                if (n != null) {
                    if (valueMap.containsKey(n)) {
                        n = valueMap.get(n);
                    }
                    builder.add(var, n);
                }
            }

            if (builder.isEmpty())
                continue;
            ed.add(builder.build());

        }
        return ed;
    }

    public boolean isEmpty() {
        return valuesTable.isEmpty();
    }

    @Override
    public void setVars(Map<Var, Node> values) {
        valueMap = values;
    }

    @Override
    public void build() {
        if (query != null) {
            ElementData ed = asElement();
            if (!ed.getRows().isEmpty()) {
                query.setValuesDataBlock(ed.getVars(), ed.getRows());
            }
        }
    }

    private List<Node> getNodesList(Var var) {
        List<Node> values = valuesTable.get(var);
        if (values == null) {
            values = new ArrayList<Node>();
            valuesTable.put(var, values);
        }
        return values;
    }

    /**
     * Add a variable to the value statement.
     *
     * A variable may only be added once. Attempting to add the same variable
     * multiple times will be silently ignored.
     *
     * @param var The variable to add.
     */
    public void addValueVar(Var var, Collection<Node> nodes) {
        List<Node> values = getNodesList(var);
        if (nodes != null) {
            values.addAll(nodes);
        }
    }

    /**
     * Add the values for the variables. There must be one value for each value var.
     *
     * @param values the collection of values to add.
     */
    public void addValueRow(Collection<Node> values) {
        if (values.size() != valuesTable.size()) {
            throw new IllegalArgumentException(String.format("Number of values (%s) must match number of columns %s",
                    values.size(), valuesTable.size()));
        }
        Iterator<Node> iter = values.iterator();
        for (Var v : valuesTable.keySet()) {
            List<Node> lst = getNodesList(v);
            lst.add(iter.next());
        }
    }

    /**
     * Add the ValuesHandler values to this values Handler.
     *
     * @param handler the handler that has the values to add.
     */
    public void addAll(ValuesHandler handler) {
        if (handler.valuesTable.size() == 0) {
            return;
        }
        // assume our table is square.
        int count = 0;
        if (valuesTable.size() > 0) {
            count = valuesTable.values().iterator().next().size();
        }

        for (Var var : handler.valuesTable.keySet()) {
            List<Node> lst = valuesTable.get(var);
            if (lst == null) {
                lst = new ArrayList<Node>();
                lst.addAll(Arrays.asList(new Node[count]));
                valuesTable.put(var, lst);
            }
            lst.addAll(handler.valuesTable.get(var));
        }

        // keep table square by adding nulls to the vars that are not in the
        // other table.
        List<Var> lst = new ArrayList<Var>(valuesTable.keySet());
        lst.removeAll(handler.valuesTable.keySet());
        if (!lst.isEmpty()) {
            count = handler.valuesTable.values().iterator().next().size();
            for (Var var : lst) {
                List<Node> lst2 = valuesTable.get(var);
                lst2.addAll(Arrays.asList(new Node[count]));
            }
        }

    }

    public void clear() {
        valuesTable.clear();
    }

    public List<Var> getValuesVars() {
        return Collections.unmodifiableList(new ArrayList<Var>(valuesTable.keySet()));
    }

    public Map<Var, List<Node>> getValuesMap() {
        Map<Var, List<Node>> m = new LinkedHashMap<Var, List<Node>>();
        for (Var key : valuesTable.keySet()) {
            m.put(key, Collections.unmodifiableList(valuesTable.get(key)));
        }
        return Collections.unmodifiableMap(m);
    }
}
