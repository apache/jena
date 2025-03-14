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

package org.apache.jena.sparql.util.iso;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetRewindable;
import org.apache.jena.sparql.util.EqualityTest;

/**
 * Shared stuff. Where does this go?
 */
class IsoLib {
    static final boolean DEBUG = false;

    /**
     * Interface for choosing the pairs of node that can be mapped for isomorphism.
     */
    @FunctionalInterface
    interface Mappable {
        boolean mappable(Node n1, Node n2);
    }

    /** Blank nodes are mappable in {@link IsoAlgTuple} */
    static Mappable mappableBlankNodes = (Node n1, Node n2) -> n1.isBlank() && n2.isBlank();

    static Mappable mappableVariables = (Node n1, Node n2) -> n1.isVariable() && n2.isVariable();

    static Mappable mappableBlankNodesVariables = (Node n1, Node n2) ->  {
            if ( n1.isBlank() && n2.isBlank() ) return true;
            if ( n1.isVariable() && n2.isVariable() ) return true;
            return false;
        };

    /** Exact match only (a separate test), same term, blank nodes have same label. */
    static Mappable mappableNoMap = (Node n1, Node n2) -> false;

    static boolean compareHeader(RowSet rs1, RowSet rs2) {
        if ( rs1 == null && rs2 == null )
            return true;
        if ( rs1 == null )
            return false;
        if ( rs2 == null )
            return false;
        Set<Var> names1 = Set.copyOf(rs1.getResultVars());
        Set<Var> names2 = Set.copyOf(rs2.getResultVars());
        return names1.equals(names2);
    }

    static RowSetRewindable print(RowSet rs) {
        RowSetRewindable rsw = rs.rewindable();
        rsw.forEach(binding->{
            String x = str(binding);
            System.out.print("  ");
            System.out.print(x);
            System.out.println();
        });
        rsw.reset();
        return rsw;
    }

    // String, with blank node ids, not "_:bN"
    static String str(Binding binding) {
        StringBuffer sbuff = new StringBuffer();
        binding.forEach((var, node)-> {
            String tmp = NodeFmtLib.strNT(node);
            sbuff.append("( ?" + var.getVarName() + " = " + tmp + " )");

        });
        return sbuff.toString();
    }


    /** Compare two bindings, use the node equality test provided */
    static boolean equal(Binding bind1, Binding bind2, EqualityTest test) {
        if ( bind1 == bind2 )
            return true;
        if ( bind1.size() != bind2.size() )
            return false;
        // They are the same size so containment is enough.
        if ( ! containedIn(bind1, bind2, test) )
            return false;
        return true;
    }

    // Is bind1 contained in bind2?  For every (var,value) in bind1, is it in bind2?
    // Maybe more in bind2.
    private static boolean containedIn(Binding bind1, Binding bind2, EqualityTest test) {
        Iterator<Var> iter1 =  bind1.vars();
        while(iter1.hasNext()) {
            Var v = iter1.next();
            Node n1 = bind1.get(v);
            Node n2 = bind2.get(v);
            if ( n2 == null )
                // v bound in bind1 and not in bind2.
                return false;
            if ( ! test.equal(n1, n2) )
                return false;
        }
        return true;
    }

}