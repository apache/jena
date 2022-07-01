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

package org.apache.jena.sparql.engine.binding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;

/** Operations on Bindings */
public class BindingLib {

    /** test for equality - order independent (Bindings are map-like) */
    public static boolean equals(Binding bind1, Binding bind2) {
        if ( bind1 == bind2 )
            return true;
        if ( bind1.size() != bind2.size() )
            return false;
        for ( Iterator<Var> iter1 = bind1.vars() ; iter1.hasNext() ; ) {
            Var var = iter1.next();
            Node node1 = bind1.get(var);
            Node node2 = bind2.get(var);
            if ( !Objects.equals(node1, node2) )
                return false;
        }

        // No need to check the other way round as the sizes matched.
        return true;
    }

    /** Merge two bindings, assuming they are compatible. */
    public static Binding merge(Binding bind1, Binding bind2) {
        // Create binding from LHS
        BindingBuilder builder = Binding.builder(bind1);
        Iterator<Var> vIter = bind2.vars();
        // Add any variables from the RHS
        for ( ; vIter.hasNext() ; ) {
            Var v = vIter.next();
            if ( !builder.contains(v) )
                builder.add(v, bind2.get(v));
            else {
                // Checking!
                Node n1 = bind1.get(v);
                Node n2 = bind2.get(v);
                if ( !n1.equals(n2) )
                    Log.warn(BindingLib.class, "merge: Mismatch : " + n1 + " != " + n2);
            }
        }
        return builder.build();
    }

    /** Convert Binding to a Map */
    public static Map<Var, Node> bindingToMap(Binding binding) {
        Map<Var, Node> map = new HashMap<>();
        binding.forEach(map::put);
        return map;
    }

    /** Convert a query solution to a binding */
    public static Binding asBinding(QuerySolution qSolution) {
        if ( qSolution == null )
            return null;
        if ( qSolution instanceof ResultBinding )
            // Only named variables.
            return new BindingProjectNamed(((ResultBinding)qSolution).getBinding());
        Binding binding = toBinding(qSolution);
        return binding;
    }

    public static Binding toBinding(QuerySolution qSolution) {
        BindingBuilder builder = Binding.builder();
        for ( Iterator<String> iter = qSolution.varNames() ; iter.hasNext() ; ) {
            String n = iter.next();

            RDFNode x = qSolution.get(n);
            if ( Var.isBlankNodeVarName(n) )
                continue;
            try {
                builder.add(Var.alloc(n), x.asNode());
            } catch (ARQInternalErrorException ex) {
                // bad binding attempt.
                Log.warn(BindingLib.class, "Attempt to bind " + n + " when already bound");
            }
        }
        return builder.build();
    }
}
