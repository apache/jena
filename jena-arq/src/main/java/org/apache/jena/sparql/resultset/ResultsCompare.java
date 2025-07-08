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

package org.apache.jena.sparql.resultset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.EqualityTest;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.iso.BNodeIso;
import org.apache.jena.sparql.util.iso.IsoAlgRows;

/** Comparison of ResultSets.
 *  Note that reading ResultSets is destructive so consider using {@link ResultSetRewindable}
 *  from {@link ResultSetFactory#makeRewindable}
 */

public class ResultsCompare {

    /**
     * Compare two result sets for equivalence where nodes are compared by value. A
     * row rs1 has one matching row in rs2, and vice versa. A row is only matched
     * once. Rows match if they have the same variables with the same values. bNodes
     * must map to a consistent other bNodes. Value comparisons of nodes.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByValue(ResultSet rs1, ResultSet rs2) {
        return equalsByValue(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /** See {@link #equalsByValue(ResultSet, ResultSet)} */
    public static boolean equalsByValue(RowSet rs1, RowSet rs2) {
        return IsoAlgRows.isomorphic(rs1, rs2, BNodeIso.Match.BNODES_VALUE);
    }

    /**
     * Compare two result sets for equivalence where nodes are compared by beign the
     * same term.. A row rs1 has one matching row in rs2, and vice versa. A row is
     * only matched once. Rows match if they have the same variables with the same
     * values, bNodes must map to a consistent other bNodes.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTerm(ResultSet rs1, ResultSet rs2) {
        return equalsByTerm(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /**
     * Compare two row sets for equivalence.  Equivalence means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values,
     * blank node must map to a consistent blank node in the other row set.
     * Term comparisons of nodes.
     *
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTerm(RowSet rs1, RowSet rs2) {
        return IsoAlgRows.isomorphic(rs1, rs2, BNodeIso.Match.BNODES_TERM);
    }

    /** Compare two list - unordered */
    public static boolean equalsByTerm(List<Binding> list1, List<Binding> list2) {
        return IsoAlgRows.isomorphic(list1, list2, BNodeIso.Match.BNODES_TERM);
    }

    /**
     * Compare two result sets for equivalence. Equivalence means: Each row in rs1
     * matches the same index row in rs2. Rows match if they have the same variables
     * with the same values, bNodes must map to a consistent other bNodes.
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByValueAndOrder(ResultSet rs1, ResultSet rs2) {
        return equalsByValueAndOrder(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /**
     * Compare two result sets for equivalence. Equivalence means: Each row in rs1
     * matches the same index row in rs2. Rows match if they have the same variables
     * with the same values, bNodes must map to a consistent other bNodes.
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByValueAndOrder(RowSet rs1, RowSet rs2) {
        if ( ! compareHeader(rs1, rs2) )
            return false;
        return equivalentByOrder(convert(rs1) , convert(rs2), BNodeIso.bnodeIsoByValue());
    }

    /**
     * Compare two result sets for equivalence.
     * Equivalence means: Each row in rs1
     * matches the same index row in rs2. Rows match if they have the same variables
     * with the same values, blank nodes must map to a consistent other blank node.
     * RDF term comparisons of nodes.
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTermAndOrder(ResultSet rs1, ResultSet rs2) {
        return equalsByTermAndOrder(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /**
     * Compare two row sets for equivalence.
     * Equivalence means: Each row in rs1
     * matches the same index row in rs2. Rows match if they have the same variables
     * with the same values, blank nodes must map to a consistent other blank node.
     * RDF term comparisons of nodes.
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTermAndOrder(RowSet rs1, RowSet rs2) {
        if ( ! compareHeader(rs1, rs2) )
            return false;
        return equivalentByOrder(convert(rs1) , convert(rs2), BNodeIso.bnodeIsoByTerm());
    }

    /**
     * Compare two result sets for exact equality equivalence.
     * Exact equality means:
     * Each row in rs1 matches the same index row in rs2.
     * Rows match if they have the same variables with the same values,
     * Blank nodes must have same labels
     *
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsExact(ResultSet rs1, ResultSet rs2) {
        return equalsExact(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /**
     * Compare two row sets for equivalence where blank nodes must have same labels
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsExact(RowSet rs1, RowSet rs2) {
        if ( !compareHeader(rs1, rs2) )
            return false;
        return equalsExact(convert(rs1), convert(rs2));
    }

    /**
     * Compare two row sets for exact node equality equivalence.
     * Each row in rs1 matches some other row in rs2.
     * Rows match if they have the same variables with the same terms.
     * Blank nodes must have same labels
     *
     * @param list1
     * @param list2
     * @return true if they are equivalent
     */
    public static boolean equalsExact(List<Binding> list1, List<Binding> list2) {
        // Mutable copy used to track matches from list1.
        // This would also work.
        //   return IsoAlgRows.isIsomorphic(list1, list2, BNodeIso.Match.EXACT);
        // The code here is simpel and direct.

        list2 = new ArrayList<>(list2);
        if ( list1.size() != list2.size() )
            return false;
        for ( Binding binding : list1 ) {
            boolean wasPresent = list2.remove(binding);
            if ( ! wasPresent )
                return false;
        }
        // list2 must be empty.
        return true;
    }

    /**
     * Compare two row sets for exact node equality equivalence.
     * Each row in rs1 matches the same row in rs2.
     * Rows match if they have the same variables with the same values,
     * Blank nodes must have same labels
     *
     * @param list1
     * @param list2
     * @return true if they are equivalent
     */
    public static boolean equalsExactAndOrder(List<Binding> list1, List<Binding> list2) {
        return equivalentByOrder(list1, list2, NodeUtils.sameRdfTerm);
    }

    /** Compare two bindings, use the node equality test provided */
    static public boolean equal(Binding bind1, Binding bind2, EqualityTest test) {
        if ( bind1 == bind2 )
            return true;
        if ( bind1.size() != bind2.size() )
            return false;
        // They are the same size so containment is enough.
        if ( ! containedIn(bind1, bind2, test) )
            return false;
        return true;
    }

    static private boolean compareHeader(RowSet rs1, RowSet rs2) {
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

    static private List<Binding> convert(RowSet rs) {
        return Iter.iter(rs).toList();
    }

    static private boolean equivalentByOrder(List<Binding> rows1, List<Binding> rows2, EqualityTest match) {
        if ( rows1.size() != rows2.size() )
             return false;

        Iterator<Binding> iter1 = rows1.iterator();
        Iterator<Binding> iter2 = rows2.iterator();

        while (iter1.hasNext()) {
            // Does not need backtracking because rows must
            // align and so must variables in a row.
            Binding row1 = iter1.next();
            Binding row2 = iter2.next();
            if ( !equal(row1, row2, match) )
                return false;
        }
        return true;
    }

    // Is bind1 contained in bind2?  For every (var,value) in bind1, is it in bind2?
    // Maybe more in bind2.
    private static boolean containedIn(Binding bind1, Binding bind2, EqualityTest test) {
        // There are about 100 ways to do this!
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
