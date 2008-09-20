/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import static com.hp.hpl.jena.tdb.lib.Lib.printAbbrev;
import static iterator.Iter.map;
import static iterator.Iter.toList;
import iterator.Transform;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.Item;

import com.hp.hpl.jena.tdb.TDBException;


/** Machinary */
public abstract class ReorderTransformationBase implements ReorderTransformation
{
    protected static final boolean DEBUG = false ;
    
    @Override
    public BasicPattern reorder(BasicPattern pattern)
    {
        return reorderIndexes(pattern).reorder(pattern) ;
    }
    
    @Override
    public final ReorderProc reorderIndexes(BasicPattern pattern)
    {
        if (pattern.size() < 2 )
            return ReorderLib.identityProc() ;
        
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)pattern.getList() ;

        // Convert to a mutable form (that allows things like "TERM")
        List<PatternTriple> components = toList(map(triples, convert)) ;
        
        // This is for moving the substitution inside the reorder code.
        // Otherwise StageReorder needs to call Substitute.substitute  
//        Binding b = new BindingMap() ;
//        if ( b != null )
//        {
//            @SuppressWarnings("unchecked")
//            Iterator<Var> iter = (Iterator<Var>)b.vars() ;
//            for ( ; iter.hasNext() ; )
//            {
//                Var v = iter.next() ;
//                Node n = b.get(v) ;
//                update(v, n, components) ;
//            }
//        }

        ReorderProc proc = reorder(triples, components) ;
        return proc ;
    }

    private ReorderProc reorder(List<Triple> triples, List<PatternTriple> components)
    {
        int N = components.size() ;
        int numReorder = N ;        // Maybe choose 4, say, and copy over the rest.
        int indexes[] = new int[N] ;

        //Set<Var> varsInScope = new HashSet<Var>() ;
        int idx = 0 ;
        for ( ; idx < numReorder ; idx++ )
        {
            int j = chooseNext(components) ;
            if ( j < 0 )
                break ;
//            {
//                System.err.println("Reorder error") ;
//                System.err.println("---- Triples:\n"+printAbbrevList(triples)) ;
//                System.err.println("---- Components:\n"+printAbbrevList(components)) ;
//            }
            Triple triple = triples.get(j) ;
            indexes[idx] = j ;
            //VarUtils.addVarsFromTriple(varsInScope, triple) ;
            update(triple, components) ;
            components.set(j, null) ;
        }
        
        // Copy over the remainder (if any) 
        for ( int i = 0 ; i < components.size() ; i++ )
        {
            if ( components.get(i) != null )
                indexes[idx++] = i ;
            
            //VarUtils.addVarsFromTriple(varsInScope, triples.get(i)) ;
        }
        if ( triples.size() != idx )
            throw new TDBException(String.format("Inconsistency: number of triples (%d) not equal to number of indexes processed (%d)", triples.size(), idx)) ;
        
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
            System.out.println(">> Input") ;
            int i = -1 ;
            for ( PatternTriple pt : pTriples )
            {
                i++ ;
                if ( pt == null )
                {
                    System.out.printf("%d          : null\n", i) ;
                    continue ;
                }
                double w2 = weight(pt) ;
                System.out.printf("%d %8.0f : %s\n", i, w2, printAbbrev(pt)) ;
            }
        }
        
        int idx = -1 ;
        double min = Double.MAX_VALUE ;
        int N = pTriples.size() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            PatternTriple pt = pTriples.get(i) ;
            if ( pt == null )
                continue ;
            double x = weight(pt) ;
            if ( x < 0 )
            {
                // Not found.  Make sure idx is not left as -1 
                if ( idx == -1 )
                    idx = i ;
                continue ;
            }
            if ( x < min )
            {
                min = x ;
                idx = i ;
            }
        }
        
        if ( DEBUG )
        {
            System.out.println("<< Output: "+idx) ;
            String x = printAbbrev(pTriples.get(idx)) ;
            System.out.println(x) ;
        }
        return idx ;
    }

    protected abstract double weight(PatternTriple pt) ;

    /** Update components to note any variables from triple */
    protected final void update(Triple triple, List<PatternTriple> components)
    {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(triple, elt) ;
    }

    private void update(Triple triple, PatternTriple tuple)
    {
        update(triple.getSubject(), tuple) ;
        update(triple.getPredicate(), tuple) ;
        update(triple.getObject(), tuple) ;
    }

    private void update(Node node, PatternTriple elt)
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
    protected final void update(Var var, Node value, List<PatternTriple> components)
    {
        for ( PatternTriple elt : components )
            if ( elt != null )
                update(var, value, elt) ;
    }
    
    private void update(Var var, Node value, PatternTriple elt)
    {
        if ( var.equals(elt.subject.getNode()) )
            elt.subject = Item.createNode(value) ;
        if ( var.equals(elt.predicate.getNode()) )
            elt.predicate = Item.createNode(value) ;
        if ( var.equals(elt.object.getNode()) )
            elt.object = Item.createNode(value) ;
    }
    
    private static Transform<Triple, PatternTriple> convert = new Transform<Triple, PatternTriple>(){
        @Override
        public PatternTriple convert(Triple triple)
        {
            return new PatternTriple(triple) ;
        }} ;
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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