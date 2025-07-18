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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.util.EqualityTest;
import org.apache.jena.sparql.util.Iso;
import org.apache.jena.sparql.util.NodeUtils;

/**
 * Simple isomorphism testing for collections of tuples of nodes. This can be used
 * for graphs, datasets and results sets The Graph isomorphism code in Jena is much
 * better (better tested, better performance) for graph isomorphism. This code is
 * simple, easier to understand, and works on collections of tuples, not just graphs.
 */
public class IsoAlgTuple {

    private record Possibility(Tuple<Node> tuple, IsoMapping mapping) {
        @Override
        public String toString() {
            return String.format("Poss|%s %s|", tuple, mapping);
        }
    }

    /**
     * Blank node isomorphism test.
     * Are the two collections of tuples of nodes isomorphic?
     * Two nodes considered "equal" by an equality test such as
     * {@link NodeUtils#sameValue} (SPARQL value testing),
     * {@link NodeUtils#sameNode} (Node.equals),
     * or {@link NodeUtils#sameRdfTerm} (Node.equals, with lang tag insensitive testing).
     */
    public static boolean isIsomorphic(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2, EqualityTest nodeTest) {
        return isIsomorphic(x1, x2, IsoLib.mappableBlankNodes, nodeTest);
    }

    /**
     * Isomorphism test based on a class of IsoLib.Mappable elements (e.g. blank nodes {@linkplain Iso#mappableBlankNodes},
     * or blank nodes and variables {@linkplain Iso#mappableBlankNodesVariables}).
     * Two nodes considered "equal" by an equality test such as
     * {@link NodeUtils#sameValue} (SPARQL value testing),
     * {@link NodeUtils#sameNode} (Node.equals),
     * or {@link NodeUtils#sameRdfTerm} (Node.equals, with lang tag insensitive testing).
     */
    /*package*/ static boolean isIsomorphic(Collection<Tuple<Node>> x1, Collection<Tuple<Node>> x2, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        if ( x1.size() != x2.size() )
            return false;
        return matcher(x1, x2, IsoMapping.rootMapping, mappable, nodeTest);
    }

    /**
     * Isomorphism test based on a class of IsoLib.Mappable elements (e.g. blank nodes {@linkplain Iso#mappableBlankNodes},
     * or blank nodes and variables {@linkplain Iso#mappableBlankNodesVariables}).
     * Two nodes considered "equal" by an equality test such as
     * {@link NodeUtils#sameValue} (SPARQL value testing),
     * {@link NodeUtils#sameNode} (Node.equals),
     * or {@link NodeUtils#sameRdfTerm} (Node.equals, with lang tag insensitive testing).
     */
    private static IsoMapping isIsomorphic(Tuple<Node> tuple1, Tuple<Node> tuple2, IsoMapping mapping, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        return matchTuples(tuple1, tuple2, mapping, mappable, nodeTest);
    }

    // Debug.
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

    private static boolean matcher(Collection<Tuple<Node>> tuples1, Collection<Tuple<Node>> tuples2, IsoMapping mapping,
                                   IsoLib.Mappable mappable, EqualityTest nodeTest) {
        if ( DEBUG ) {
            out.println("match: ");
            out.println("  1: " + tuples1);
            out.println("  2: " + tuples2);
            out.println("  M: " + mapping);
        }
        // Process the inputs : tuples of all non-mappable terms can be processed here (Node.equals)
        // Loop on tuples1:
        //   if not mappable, remove from tuples2.
        //   if mappable, copy into tuples1.
        //
        tuples2 = new HashSet<>(tuples2);

        // Fast path.
        if ( true ) {
            List<Tuple<Node>> tuples1$ = new ArrayList<>();
            // Step one - copy non-mappable tuples or exit now.
            for ( Tuple<Node> t1 : tuples1 ) {
                if ( ! containsMappable(t1) ) {
                    boolean wasPresent = tuples2.remove(t1);
                    if ( ! wasPresent )
                        return false;
                    continue;
                }
                tuples1$.add(t1);
            }
            // Tuples for IsoLib.Mappable processing.
            tuples1 = tuples1$;
        } else
            // No pre-process concrete.
            tuples1 = new ArrayList<>(tuples1);

        // ----

        if ( tuples1.size() != tuples2.size() )
            return false;

        for ( Tuple<Node> t1 : tuples1 ) {
            if ( DEBUG )
                out.println("  Process t1 = " + t1);
            tuples1.remove(t1);

            List<Possibility> causes = matcher(t1, tuples2, mapping, mappable, nodeTest);

            if ( DEBUG ) {
                out.println("    Possibilities: Tuple" + t1 + " :: " + causes);
                out.incIndent();
            }
            try {
                // Try each possible tuple-tuple matching until one succeeds all the
                // way.
                for ( Possibility c : causes ) {
                    if ( DEBUG )
                        out.println("  Try: " + c);
                    // Try t1 -> t2
                    Tuple<Node> t2 = c.tuple;
                    tuples2.remove(t2);
                    // Try without t1 and t2, using the IsoMapping of this cause.
                    if ( tuples1.isEmpty() && tuples2.isEmpty() )
                        // They are the same size.
                        return true;
                    // Recurse
                    if ( matcher(tuples1, tuples2, c.mapping, mappable, nodeTest) ) {
                        if ( DEBUG )
                            out.println("Yes");
                        return true;
                    }
                    if ( DEBUG )
                        out.println("No");
                    tuples2.add(t2);
                }
                return false;
            }
            finally {
                if ( DEBUG ) out.decIndent();
            }
        }
        return true;
    }

    private static boolean containsMappable(Tuple<Node> tuple) {
        for ( Node n : tuple ) {
            if ( n.isBlank() )
                return true;
            if ( n.isVariable() )
                return true;
            if ( n.isTripleTerm() ) {
                Tuple<Node> tuple1 = tripleToTuple.apply(n.getTriple());
                boolean b = containsMappable(tuple1);
                if ( b )
                    return true;
            }
        }
        return false;
    }

    /** Return all possible tuple-tuple matches from tuple t1 to tuples in x2 */
    private static List<Possibility> matcher(Tuple<Node> t1, Collection<Tuple<Node>> g2, IsoMapping mapping, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        List<Possibility> matches = new ArrayList<>();
        for ( Tuple<Node> t2 : g2 ) {
            IsoMapping step = matchTuples(t1, t2, mapping, mappable, nodeTest);
            if ( step != null ) {
                Possibility c = new Possibility(t2, step);
                matches.add(c);
            }
        }
        return matches;
    }

    /**
     * Find a IsoMapping between the tuples, given a start mapping.
     * Return a IsoMapping or null for "no match".
     */
    private static IsoMapping matchTuples(Tuple<Node> t1, Tuple<Node> t2, IsoMapping _mapping, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        if ( t1.len() != t2.len() )
            return null;

        IsoMapping mapping = _mapping;
        for ( int i = 0 ; i < t1.len() ; i++ ) {
            Node n1 = t1.get(i);
            Node n2 = t2.get(i);

            IsoMapping mapping2 = matchTerms(n1, n2, mapping, mappable, nodeTest);
            if ( mapping2 == null )
                return null;
            mapping = mapping2;
        }
        return mapping;
    }

    private static Function<Triple, Tuple<Node>> tripleToTuple = (triple) ->
                    TupleFactory.tuple(triple.getSubject(),
                                       triple.getPredicate(),
                                       triple.getObject());

    static IsoMapping matchTerms(Node n1, Node n2, IsoMapping _mapping, IsoLib.Mappable mappable, EqualityTest nodeTest) {
        IsoMapping mapping = _mapping;
        Node n1m = mapping.map(n1);

        if ( n1m != null ) {
            // Already mapped
            if ( n1m.equals(n2) )
                // Exact equals after IsoMapping n1.
                return mapping;
            // No match.
            return null;
        }

        if ( n1.isTripleTerm() ) {
            if ( n2.isTripleTerm() ) {
                Triple t1 = n1.getTriple();
                Triple t2 = n2.getTriple();
                Tuple<Node> tuple1 = tripleToTuple.apply(t1);
                Tuple<Node> tuple2 = tripleToTuple.apply(t2);
                return matchTuples(tuple1, tuple2, mapping, mappable, nodeTest);
            }
        } else if ( n2.isTripleTerm() ) {
            return null;
        }

        // Map an atomic term (not triple term)
        if ( mappable.mappable(n1, n2) ) {
            if ( mapping.reverseMapped(n2) ) {
                // Already a target.
                // but not the same (else n1m != null)
                return null;
            }
            // **** If n2 not already mapped.

            mapping = new IsoMapping(mapping, n1, n2);
            return mapping;
        }

        if ( !nodeTest.equal(n1, n2) )
            // No isomorphism.
            return null;

        return mapping;
    }
}
