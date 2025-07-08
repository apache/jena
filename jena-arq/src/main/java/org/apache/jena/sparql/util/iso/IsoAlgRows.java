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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.EqualityTest;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.iso.IsoLib.Mappable;

public class IsoAlgRows {

    private static final boolean        DEBUG = false;
    private static final IndentedWriter out   = debugIndentedWriter();
    private static final IndentedWriter debugIndentedWriter() {
        IndentedWriter iout = null;
        if ( DEBUG ) {
            iout = new IndentedWriter(System.out);
            iout.setFlushOnNewline(true);
        }
        return iout;
    }

    public static boolean isomorphic(RowSet rowset1, RowSet rowset2, BNodeIso.Match matching) {
        Objects.requireNonNull(rowset1);
        Objects.requireNonNull(rowset2);
        if ( rowset1 == rowset2 )
            return true;
        if ( ! IsoLib.compareHeader(rowset1, rowset2) )
            return false;
        List<Binding> rows1 = toList(rowset1);
        List<Binding> rows2 = toList(rowset2);
        if ( rows1.size() != rows2.size() )
            return false;

        // Could divide into rows with exact matches (no blank nodes) and those with
        // blank nodes.

        return matcher(rows1, rows2, matching);
    }

    public static boolean isomorphic(List<Binding> list1, List<Binding> list2, BNodeIso.Match matching) {
        if ( list1.size() != list2.size() )
            return false;
        return matcher(list1, list2, matching);
    }

    private static boolean matcher(List<Binding> list1, List<Binding> list2, BNodeIso.Match matching) {
        Mappable mapping;
        EqualityTest equalityTest;
        switch(matching) {
            case BNODES_TERM, EXACT_TERM    -> equalityTest = NodeUtils.sameRdfTerm;
            case BNODES_VALUE, EXACT_VALUE  -> equalityTest = NodeUtils.sameValue;
            default -> { throw new InternalErrorException("Unknown equality policy"); }
        }
        switch(matching) {
            case EXACT_VALUE, EXACT_TERM    -> mapping = IsoLib.mappableNoMap;
            case BNODES_TERM, BNODES_VALUE  -> mapping = IsoLib.mappableBlankNodes;
            default -> { throw new InternalErrorException("Unknown matching policy"); }
        }

        return matcherWorker(list1, 0, list2, IsoMapping.rootMapping, mapping, equalityTest);
    }

    // Match rows in rows1, starting at idx1, against rows in rows2, starting at idx2
    private static boolean matcherWorker(List<Binding> rows1, int idx1, List<Binding> rows2,
                                         IsoMapping mapping, IsoLib.Mappable mappable, EqualityTest equalityTest) {
        if ( rows1.size() == 0 )
            return true;

        for ( int i = idx1 ; i < rows1.size() ; i++ ) {
            Binding row1 = rows1.get(i);
            if ( row1 == null )
                continue;
            if ( DEBUG )
                out.println("-- Row1 = "+row1);
            List<Possibility> possibilities = matcherOneRow(row1, rows2, mapping, mappable, equalityTest);
            if ( DEBUG )
                out.println("Possibilities: "+possibilities);

            if ( possibilities.isEmpty() )
                return false;

            for ( Possibility poss : possibilities ) {
                if ( DEBUG )
                    out.println("Try: " + poss);
                // Without this row1, and the possibility row can we map the rest?
                int iRest = i+1;
                if ( iRest >= rows1.size() ) {
                    // Last item
                    if ( DEBUG )
                        out.println("Yes (zero left)");
                    return true;
                }
                // One element of row2 removed. Not by position.
                List<Binding> rows2x = new ArrayList<>(rows2);
                rows2x.remove(poss.row);

                if ( DEBUG )
                    out.incIndent();

                if ( matcherWorker(rows1, iRest, rows2x, poss.mapping, mappable, equalityTest) ) {
                    if ( DEBUG )
                        out.decIndent();

                    if ( DEBUG )
                        out.println("Yes");
                    return true;
                }
                if ( DEBUG )
                    out.decIndent();
                if ( DEBUG )
                    out.println("No");
            }
            // No possibility
            return false;
        }
        // Maybe a bug in this code
        return false;
    }

    private static List<Possibility> matcherOneRow(Binding row1, List<Binding> rows2, IsoMapping mapping, IsoLib.Mappable mappable, EqualityTest equalityTest) {
        // find in rows2.
        List<Possibility> possibilities = new ArrayList<>();
        for ( int j = 0 ; j < rows2.size() ; j++ ) {
            Binding row2 = rows2.get(j);
            if ( row2 == null )
                continue;
            if ( DEBUG )
                out.printf("%-2d Row2 = %s\n", j, row2);
            IsoMapping step = matchOne(row1, row2, mapping, mappable, equalityTest);
            if ( step == null )
                continue;
            Possibility poss = new Possibility(row2, step);
            if ( DEBUG )
                out.println(poss.toString());
            // if row1 all concrete, drop row1 and row2
            possibilities.add(poss);
        }
        return possibilities;
    }

    /** Can row1 be matched  by row2? */
    private static IsoMapping matchOne(Binding row1, Binding row2, IsoMapping mapping, IsoLib.Mappable mappable, EqualityTest equalityTest) {
//        Set<Var> vars1 = row1.varsMentioned();
//        Set<Var> vars2 = row2.varsMentioned();
//        if ( ! vars1.equals(vars2) )
//            return null;

        Iterator<Var> iter = row1.vars();
        IsoMapping mapping2 = mapping;
        while(iter.hasNext()) {
            Var v = iter.next();
            Node n1 = row1.get(v);
            Node n2 = row2.get(v);
            if ( n2 == null )
                return null;
            mapping2 = matchTerms(n1, n2, mapping2, mappable, equalityTest);
            if ( mapping2 == null )
                return null;
            if ( DEBUG )
                out.println("Mapping: "+mapping2);

        }
        return mapping2;
    }

    /*package*/ static IsoMapping matchTermsTest(Node n1, Node n2, EqualityTest nodeTest) {
        EqualityTest eqtest = BNodeIso.bnodeIsoByTerm();
        return matchTerms(n1, n2, IsoMapping.rootMapping, IsoLib.mappableBlankNodes, eqtest);
    }

    static IsoMapping matchTerms(Node n1, Node n2, IsoMapping _mapping, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        if ( _mapping == null )
            return null;
        if ( n1 == null || n2 == null )
            return null;
        IsoMapping mapping = _mapping;
        Node n1m = mapping.map(n1);

        if ( n1m != null ) {
            // Already mapped
            if ( DEBUG )
                out.printf("Existing: %s -> %s for %s\n", n1, n1m, n2);
            if ( n1m.equals(n2) )
                // Exact equals after mapping t1 slot.
                return mapping;
            // No match.
            return null;
        }

        if ( n1.isTripleTerm() ) {
            if ( n2.isTripleTerm() ) {
                Triple t1 = n1.getTriple();
                Triple t2 = n2.getTriple();
                mapping = matchTerms(t1.getSubject(), t2.getSubject(), mapping, mappable, nodeTest);
                if ( mapping == null )
                    return null;
                mapping = matchTerms(t1.getPredicate(), t2.getPredicate(), mapping, mappable, nodeTest);
                if ( mapping == null )
                    return null;
                mapping = matchTerms(t1.getObject(), t2.getObject(), mapping, mappable, nodeTest);
                if ( mapping == null )
                    return null;
                return mapping;
            }
            // n2 not a triple term.
            return null;
        } else if ( n2.isTripleTerm() ) {
            // n1 not a triple term.
            return null;
        }

        // Map an atomic term (not triple term)
        if ( mappable.mappable(n1, n2) ) {
            if ( mapping.reverseMapped(n2) ) {
                // Already a target.
                // but not the same (else n1m != null)
                return null;
            }
            // Add mapping.
            mapping = new IsoMapping(mapping, n1, n2);
            return mapping;
        }

        if ( !nodeTest.equal(n1, n2) )
            // No isomorphism.
            return null;

        return mapping;
    }


    static private List<Binding> toList(RowSet rs) {
        return Iter.iter(rs).toList();
    }

    private record Possibility(Binding row, IsoMapping mapping) {
        @Override
        public String toString() {
            return String.format("Poss|%s %s|", IsoLib.str(row), mapping);
        }
    }
}

