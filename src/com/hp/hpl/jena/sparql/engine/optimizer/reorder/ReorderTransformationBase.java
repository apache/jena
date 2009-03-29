/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.reorder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.lib.iterator.AccString;
import com.hp.hpl.jena.sparql.lib.iterator.Iter;
import com.hp.hpl.jena.sparql.lib.iterator.Transform;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.util.StrUtils;

import static com.hp.hpl.jena.sparql.util.StringUtils.printAbbrev;

/** Machinary */
public abstract class ReorderTransformationBase implements ReorderTransformation
{
    protected static final boolean DEBUG = false ;  
    static public final Logger log = LoggerFactory.getLogger(ReorderTransformationBase.class) ;
    
    //@Override // Java5-ism
    public BasicPattern reorder(BasicPattern pattern)
    {
        return reorderIndexes(pattern).reorder(pattern) ;
    }

    //@Override
    public final ReorderProc reorderIndexes(BasicPattern pattern)
    {
        if (pattern.size() <= 1 )
            return ReorderLib.identityProc() ;
        
        List<Triple> triples = pattern.getList() ;

        // Could merge into the conversion step to do the rewrite WRT a Binding.
        // Or done here as a second pass mutate of PatternTriples

        // Convert to a mutable form (that allows things like "TERM")
        List<PatternTriple> components = Iter.toList(Iter.map(triples, convert)) ;

        // Allow subclasses to get in (e.g. static reordering).
        components = modifyComponents(components) ;
        ReorderProc proc = reorder(triples, components) ;
        return proc ;
    }

    protected List<PatternTriple> modifyComponents(List<PatternTriple> components)
    {
        return components ;
    }

    protected ReorderProc reorder(List<Triple> triples, List<PatternTriple> components)
    {
        int N = components.size() ;
        int numReorder = N ;        // Maybe choose 4, say, and copy over the rest.
        int indexes[] = new int[N] ;

        if ( DEBUG )
            log.info("Reorder: "+Iter.asString(components, formatter)) ;
        
        int idx = 0 ;
        for ( ; idx < numReorder ; idx++ )
        {
            int j = chooseNext(components) ;
            if ( j < 0 )
                break ;
            Triple triple = triples.get(j) ;
            indexes[idx] = j ;
            update(triple, components) ;
            components.set(j, null) ;
        }
        
        // Copy over the remainder (if any) 
        for ( int i = 0 ; i < components.size() ; i++ )
        {
            if ( components.get(i) != null )
                indexes[idx++] = i ;
        }
        if ( triples.size() != idx )
            throw new ARQException(String.format("Inconsistency: number of triples (%d) does not equal to number of indexes processed (%d)", triples.size(), idx)) ;
        
        ReorderProc proc = new ReorderProcIndexes(indexes) ;
        
        return proc ;
    }
    
    
//    private int findFirst(List<PatternTriple> pTriples)
//    {
//        for ( int i = 0 ; i < pTriples.size() ; i++ )
//            if ( pTriples.get(i) != null )
//                return i ;
//        return -1 ;
//    }

    /** Return index of next pattern triple */
    protected int chooseNext(List<PatternTriple> pTriples)
    {
        if ( DEBUG )
        {
            int i = -1 ;
            StringBuilder buff = new StringBuilder() ; 
            for ( PatternTriple pt : pTriples )
            {
                i++ ;
                if ( pt == null )
                {
                    buff.append(String.format("    %d          : null\n", i)) ;
                    continue ;
                }
                double w2 = weight(pt) ;
                buff.append(String.format("    %d %8.0f : %s\n", i, w2, printAbbrev(pt))) ;
            }
            String x = StrUtils.noNewlineEnding(buff.toString());
            log.info(">> Input\n"+x) ;
        }
        
        int idx = processPTriples(pTriples, null) ; 
        
        if ( DEBUG )
        {
            String x = printAbbrev(pTriples.get(idx)) ;
            x = StrUtils.noNewlineEnding(x) ;
            log.info("<< Output\n    "+x) ;
        }
        return idx ;
    }

    /** Return all the indexes of pattern triples of the least weight. */
    protected List<Integer> chooseAll(List<PatternTriple> pTriples)
    {
        List<Integer> results = new ArrayList<Integer>(pTriples.size()) ;
        processPTriples(pTriples, results) ;
        return results ;
    }
    
    /** Return the index of the first, least triple; optionally accumulate all indexes of the same least weight */ 
    private int processPTriples(List<PatternTriple> pTriples, List<Integer> results)
    {
        double min = Double.MAX_VALUE ;     // Current minimum
        int N = pTriples.size() ;
        int idx = -1 ;
        
        for ( int i = 0 ; i < N ; i++ )
        {
            PatternTriple pt = pTriples.get(i) ;
            if ( pt == null )
                continue ;
            double x = weight(pt) ;
            if ( x < 0 )
            {
                // ****
                DefaultChoice choice = defaultChoice(pt) ;
                if ( choice != null )
                {
                    switch (choice)
                    {
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
                
                // Not found.  No default action.
                // Make sure something is returned but otherwise ignore this pattern (goes last). 
                if ( idx == -1 )
                {
                    idx = i ;
                    if ( results != null ) results.add(i) ;
                }
                // Do nothing.  Does not update min so will be not be in results. 
                continue ;
            }
            
            if ( x == min )
            {
                if ( results != null ) results.add(i) ;
                continue ;
            }
            
            if ( x < min )
            {
                min = x ;
                idx = i ;
                if ( results != null )
                {
                    results.clear() ;
                    results.add(i) ;
                }
            }
        }
        return idx ;
    }
    
    /** Return the weight of the pattern, or -1 if no knowdleg for it */
    protected abstract double weight(PatternTriple pt) ;
    
    protected enum DefaultChoice { ZERO, LAST, FIRST , NUMERIC ; }
    /** What to do if the {@link weight} comes back as "not found".
     * Choices are:
     *    ZERO      Assume the weight is zero (the rules were complete over the data so this is a pattern that will not match the data. 
     *    LAST      Place after all explciitly weighted triple patterns
     *    FIRST     Place before all explciitly weighted triple patterns
     *    NUMERIC   Use value returned by {@link defaultWeight}
     * The default, default choice is LAST.   
     */
    protected /*abstract*/ DefaultChoice defaultChoice(PatternTriple pt) { return null ; } // return DefaultChoice.LAST ; }
    
    protected /*abstract*/ double defaultWeight(PatternTriple pt) { return -1 ; }

    /** Update components to note any variables from triple */
    protected static void update(Triple triple, List<PatternTriple> components)
    {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(triple, elt) ;
    }

    private static void update(Triple triple, PatternTriple tuple)
    {
        update(triple.getSubject(), tuple) ;
        update(triple.getPredicate(), tuple) ;
        update(triple.getObject(), tuple) ;
    }

    private static void update(Node node, PatternTriple elt)
    {
        if ( Var.isVar(node) )
        {
            if ( node.equals(elt.subject.getNode()) )
                elt.subject = PatternElements.TERM ;
            if ( node.equals(elt.predicate.getNode()) )
                elt.predicate = PatternElements.TERM ;
            if ( node.equals(elt.object.getNode()) )
                elt.object = PatternElements.TERM ;
        }
    }
    
    /** Update based on a variable/value (c.f. Substitute.substitute) */
    protected static void update(Var var, Node value, List<PatternTriple> components)
    {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(var, value, elt) ;
    }
    
    private static void update(Var var, Node value, PatternTriple elt)
    {
        if ( var.equals(elt.subject.getNode()) )
            elt.subject = Item.createNode(value) ;
        if ( var.equals(elt.predicate.getNode()) )
            elt.predicate = Item.createNode(value) ;
        if ( var.equals(elt.object.getNode()) )
            elt.object = Item.createNode(value) ;
    }
    
    private static AccString<PatternTriple> formatter = 
    new AccString<PatternTriple>() 
    {   @Override 
        protected String toString(PatternTriple pt) 
          { return "("+printAbbrev(pt.toString())+")" ; }
    } ;
    
    // Triples to TriplePatterns.
    private static Transform<Triple, PatternTriple> convert = new Transform<Triple, PatternTriple>(){
        //@Override
        public PatternTriple convert(Triple triple)
        {
            return new PatternTriple(triple) ;
        }} ;
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */