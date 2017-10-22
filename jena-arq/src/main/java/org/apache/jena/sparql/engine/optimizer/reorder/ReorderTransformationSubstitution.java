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

package org.apache.jena.sparql.engine.optimizer.reorder;

import static org.apache.jena.sparql.util.StringUtils.printAbbrev ;

import java.util.ArrayList ;
import java.util.List ;
import org.apache.jena.atlas.iterator.AccString ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.sse.Item ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Machinary.
 * This code implements the connectiveness assumed by execution based on substitution (index joins).
 * i.e. if <code>{ ?x :p ?v . ?x :q ?w }</code> then <code>?x</code> is <code>TERM</code>
 * at the second triple. 
 */
public abstract class ReorderTransformationSubstitution implements ReorderTransformation
{
    static public final Logger log = LoggerFactory.getLogger(ReorderTransformationSubstitution.class) ;
    static private final boolean DEBUG = false ;  
    
    public ReorderTransformationSubstitution() {}
    
    @Override
    public BasicPattern reorder(BasicPattern pattern) {
        return reorderIndexes(pattern).reorder(pattern) ;
    }

    @Override
    public final ReorderProc reorderIndexes(BasicPattern pattern) {
        if ( pattern.size() <= 1 )
            return ReorderLib.identityProc() ;

        List<Triple> triples = pattern.getList() ;

        // Could merge into the conversion step to do the rewrite WRT a Binding.
        // Or done here as a second pass mutate of PatternTriples

        // Convert to a mutable form (that allows things like "TERM")
        List<PatternTriple> components = Iter.toList(Iter.map(triples.iterator(), PatternTriple::new)) ;

        // Allow subclasses to get in (e.g. static reordering).
        components = modifyComponents(components) ;
        ReorderProc proc = reorder(triples, components) ;
        return proc ;
    }

    protected List<PatternTriple> modifyComponents(List<PatternTriple> components) {
        return components ;
    }

    private AccString<PatternTriple> formatter() { 
        return new AccString<PatternTriple>() { 
            @Override
            protected String toString(PatternTriple pt) {
                return "(" + printAbbrev(pt.toString()) + ")" ;
            }
        } ;
    }
    
    protected ReorderProc reorder(List<Triple> triples, List<PatternTriple> components) {
        int N = components.size() ;
        int numReorder = N ;        // Maybe choose 4, say, and copy over the rest.
        int indexes[] = new int[N] ;

        if ( DEBUG )
            log.debug("Reorder: " + Iter.asString(components, formatter())) ;

        int idx = 0 ;
        for ( ; idx < numReorder ; idx++ ) {
            int j = chooseNext(components) ;
            if ( j < 0 )
                break ;
            Triple triple = triples.get(j) ;
            indexes[idx] = j ;
            update(triple, components) ;
            components.set(j, null) ;
        }

        // Copy over the remainder (if any)
        for ( int i = 0 ; i < components.size() ; i++ ) {
            if ( components.get(i) != null )
                indexes[idx++] = i ;
        }
        if ( triples.size() != idx )
            throw new ARQException(String.format("Inconsistency: number of triples (%d) does not equal to number of indexes processed (%d)",
                                                 triples.size(), idx)) ;

        ReorderProc proc = new ReorderProcIndexes(indexes) ;

        return proc ;
    }    
    
    /** Return index of next pattern triple */
    protected int chooseNext(List<PatternTriple> pTriples) {
        if ( DEBUG ) {
            int i = -1 ;
            StringBuilder buff = new StringBuilder() ;
            for ( PatternTriple pt : pTriples ) {
                i++ ;
                if ( pt == null ) {
                    buff.append(String.format("    %d          : null\n", i)) ;
                    continue ;
                }
                double w2 = weight(pt) ;
                buff.append(String.format("    %d %8.0f : %s\n", i, w2, printAbbrev(pt))) ;
            }
            String x = StrUtils.noNewlineEnding(buff.toString()) ;
            log.debug(">> Input\n" + x) ;
        }

        int idx = processPTriples(pTriples, null) ;

        if ( DEBUG ) {
            String x = printAbbrev(pTriples.get(idx)) ;
            x = StrUtils.noNewlineEnding(x) ;
            log.debug("<< Output\n    " + x) ;
        }
        return idx ;
    }

    /** Return all the indexes of pattern triples of the least weight. */
    protected List<Integer> chooseAll(List<PatternTriple> pTriples) {
        List<Integer> results = new ArrayList<>(pTriples.size()) ;
        processPTriples(pTriples, results) ;
        return results ;
    }

    /**
     * Return the index of the first, least triple; optionally accumulate all indexes of
     * the same least weight
     */
    private int processPTriples(List<PatternTriple> pTriples, List<Integer> results) {
        double min = Double.MAX_VALUE ;     // Current minimum
        int N = pTriples.size() ;
        int idx = -1 ;

        for ( int i = 0 ; i < N ; i++ ) {
            PatternTriple pt = pTriples.get(i) ;
            if ( pt == null )
                continue ;
            double x = weight(pt) ;
            if ( x < 0 ) {
                // ****
                DefaultChoice choice = defaultChoice(pt) ;
                if ( choice != null ) {
                    switch (choice) {
                        case FIRST :
                            // Weight very low so it goes to front.
                            x = 0.01 ;
                            break ;
                        case LAST :
                            // Default action : 
                            break ;
                        case ZERO :
                            x = 0 ;
                            break ;
                        case NUMERIC :
                            x = defaultWeight(pt) ;
                            break ;
                    }
                }
                
                // Not found. No default action.
                // Make sure something is returned but otherwise ignore this pattern (goes
                // last).
                if ( idx == -1 ) {
                    idx = i ;
                    if ( results != null )
                        results.add(i) ;
                }
                // Do nothing. Does not update min so will be not be in results.
                continue ;
            }

            if ( x == min ) {
                if ( results != null )
                    results.add(i) ;
                continue ;
            }

            if ( x < min ) {
                min = x ;
                idx = i ;
                if ( results != null ) {
                    results.clear() ;
                    results.add(i) ;
                }
            }
        }
        return idx ;
    }

    /** Return the weight of the pattern, or -1 if no knowledge for it */
    protected abstract double weight(PatternTriple pt) ;
    
    protected enum DefaultChoice { ZERO, LAST, FIRST , NUMERIC ; }
    /** What to do if the {@link weight} comes back as "not found".
     * Choices are:
     *    ZERO      Assume the weight is zero (the rules were complete over the data so this is a pattern that will not match the data. 
     *    LAST      Place after all explicitly weighted triple patterns
     *    FIRST     Place before all explicitly weighted triple patterns
     *    NUMERIC   Use value returned by {@link defaultWeight}
     * The default, default choice is LAST.   
     */
    protected DefaultChoice defaultChoice(PatternTriple pt) { return null ; } // return DefaultChoice.LAST ; }
    
    protected double defaultWeight(PatternTriple pt) { return -1 ; }

    /** Update components to note any variables from triple */
    protected static void update(Triple triple, List<PatternTriple> components) {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(triple, elt) ;
    }

    private static void update(Triple triple, PatternTriple tuple) {
        update(triple.getSubject(), tuple) ;
        update(triple.getPredicate(), tuple) ;
        update(triple.getObject(), tuple) ;
    }

    private static void update(Node node, PatternTriple elt) {
        if ( Var.isVar(node) ) {
            if ( node.equals(elt.subject.getNode()) )
                elt.subject = PatternElements.TERM ;
            if ( node.equals(elt.predicate.getNode()) )
                elt.predicate = PatternElements.TERM ;
            if ( node.equals(elt.object.getNode()) )
                elt.object = PatternElements.TERM ;
        }
    }
    
    /** Update based on a variable/value (c.f. Substitute.substitute) */
    protected static void update(Var var, Node value, List<PatternTriple> components) {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(var, value, elt) ;
    }

    private static void update(Var var, Node value, PatternTriple elt) {
        if ( var.equals(elt.subject.getNode()) )
            elt.subject = Item.createNode(value) ;
        if ( var.equals(elt.predicate.getNode()) )
            elt.predicate = Item.createNode(value) ;
        if ( var.equals(elt.object.getNode()) )
            elt.object = Item.createNode(value) ;
    }
}
