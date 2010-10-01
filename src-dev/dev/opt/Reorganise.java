/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.opt;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternElements ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternTriple ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderProc ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformationBase ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.sse.Item ;

// Work in progress
public class Reorganise
{
    
    
    // At the moment, we can do all reorganisation as a bottom-up walk. 
    //    BGP gets reordered,
    //    Then filters flowed into position.
    // If we want to take a more holistic view, need to do the rewriting as a top down analysis
    
    
    // Use OpWalker as a before visitor walker?  
    //  Need to intercept/modify OpWalker.WalkerVisitor
    // Can use OpWalker.WalkerVisitor for that.

    public static Op reorganise(Op op, Map<Op, Set<Var>> x)
    {
        return Transformer.transform(new ReorganiseTransform(x), op) ;
    }
    
    /** Transform to use a map of scopes (defined variables) to drive the reorganisation. */
    private static final class ReorganiseTransform extends TransformCopy
    {
        Map<Op, Set<Var>> scopeMap ;
        
        public ReorganiseTransform(Map<Op, Set<Var>> scopeMap)
        {
            super() ;
            this.scopeMap = scopeMap ;
        }

        // Places we want to intercept the walk.
        @Override
        public Op transform(OpFilter opFilter, Op sub)
        {
            if ( OpBGP.isBGP(sub) )
                // Reorganise and place filters
                return reorganise((OpBGP)sub, opFilter.getExprs(), scopeMap.get(opFilter)) ;
//            if ( sub instanceof OpSequence )
//            //if ( OpSequence.isSeq(sub) )
//            { }
            
            return super.transform(opFilter, sub) ;
        }

        @Override
        public Op transform(OpBGP opBGP)
        {
            return reorganise(opBGP, null, scopeMap.get(opBGP)) ;
        }
        
        // At this point we need to be walk-down.
        @Override
        public Op transform(OpSequence opSequence, List<Op> elts)
        {
            List<Op> x = opSequence.getElements() ;   // Old elements, not transformed.
            Set<Var> defined = new HashSet<Var>() ;
            
            for ( Op op : x )
            {
                //****
                // Do op.
                // Extend scope
                defined.addAll(scopeMap.get(op)) ;
            }
            
            return super.transform(opSequence, elts);
            // for every element in the sequence, accumulate the known bound variables. 
        }
        
        private static Op reorganise(OpBGP opBGP, ExprList exprs, Set<Var> set)
        {
            // Redo the BGP pattern WRT the scope set.
            BasicPattern pattern = opBGP.getPattern() ;
            ReorderTransformation transform = new ReorderTransformationReorg(set) ;
            
            // This can miss very restrictive filters (partial conditional scans) 
            // but those still require large amount of index access even if they
            // emit very few results.   
            
            
//            ReorderTransformation transform = new ReorderTransformationBase(){
//                @Override
//                protected double weight(PatternTriple pt)
//                {
//                    return 0 ;
//                }} ;

            ReorderProc proc = transform.reorderIndexes(pattern) ;
            pattern = proc.reorder(pattern) ; 

            Op op = null ;
            if ( exprs != null )
                op = TransformFilterPlacement.transform(exprs, pattern) ;
            else
                op = new OpBGP(pattern) ;
            
            return op ;
        }
    }
    
    /** ReorderTransformation that uses a set of defined variables (defined before this BGP is reached) */
    static class ReorderTransformationReorg extends ReorderTransformationBase  
    {
        
        Set<Var> definedVars ;
        
        public ReorderTransformationReorg(Set<Var> definedVars)
        {
            this.definedVars = definedVars ;
        }
    
        /* Modify with TERM for any defined variables */
        @Override
        protected List<PatternTriple> modifyComponents(List<PatternTriple> components)
        {
            if ( definedVars == null || definedVars.size() == 0 )
                return components ;
            
            List<PatternTriple> components2 = new ArrayList<PatternTriple>() ;
            for ( PatternTriple pt : components)
            {
                pt = update(pt) ;
                components2.add(pt) ;
            }
            return components2 ;
        }
        
        private PatternTriple update(PatternTriple pt)
        {
            return new PatternTriple(update(pt.subject),
                                     update(pt.predicate),
                                     update(pt.object)) ;
        }

        private Item update(Item item)
        {
            if ( !item.isNode() ) return item ;

            Node n = item.getNode() ;
            if ( ! Var.isAllocVar(n) ) return item ;

            Var v = Var.alloc(n) ;
            if ( definedVars.contains(v) )
                return PatternElements.TERM ;
            return item ;
        }

        @Override
        protected double weight(PatternTriple pt)
        {
            return 0 ;
        }
        
    }
    
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