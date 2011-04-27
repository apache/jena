/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import java.util.HashSet ;
import java.util.Set ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpNull ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion ;
import com.hp.hpl.jena.sparql.algebra.table.Table1 ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/**
 * Transform to restrict a query to subset of the named graphs in a datasets,
 * both in the named graphs visible, and the default graph as a composition of
 * graphs in the datasets.
 */

// Problems with paths - that code accesses the active graph.

public class TransformDynamicDataset_Imperfect extends TransformCopy
{
    // The graphs making up the usual default graph of the query.
    private Set<Node> defaultGraphs ;
    // The graphs making up the default graph seen as <urn:x-arq:DefaultGraph>
    // Often the same as above; not if union-of-named-graphs visible 
    private Set<Node> defaultGraphsReal ;
    private Set<Node> namedGraphs ;

    /** Apply the dynamic dataset transformation: rewrite the algebra into a form that uses
     *  the dataset description for a dynamic choice of graphs.  
     */
    public static Op transform(Query query, Op op)
    {
        if ( ! query.hasDatasetDescription() )
            return op ;
        Set<Node> defaultGraphs = NodeUtils2.convertToNodes(query.getGraphURIs()) ; 
        Set<Node> namedGraphs = NodeUtils2.convertToNodes(query.getNamedGraphURIs()) ;
        Transform t = new TransformDynamicDataset_Imperfect(defaultGraphs, namedGraphs, false) ; // false??
        Op op2 = Transformer.transform(t, op) ;
        return op2 ;
    }
    
    public TransformDynamicDataset_Imperfect(Set<Node> defaultGraphs, Set<Node> namedGraphs, boolean defaultGraphIncludesNamedGraphUnion)
    {
        this.defaultGraphs = defaultGraphs ;
        this.defaultGraphsReal = defaultGraphs ;
        this.namedGraphs = namedGraphs ;
        if ( defaultGraphIncludesNamedGraphUnion )
        {
            // Named graph union. 
            this.defaultGraphs = new HashSet<Node>() ;
            this.defaultGraphs.addAll(defaultGraphs) ;
            this.defaultGraphs.addAll(namedGraphs) ;
        }
    }
    
    @Override
    public Op transform(OpBGP op)
    {
        // Bad - assume we work on the quad form.
        // Otherwise need to know the active graph at this point
        // toQuadForm transformation.
        throw new ARQException("Unexpected use of BGP in for a dynamic dataset") ;
        //return super.transform(op) ;
    }
    
    @Override
    public Op transform(OpPath op)
    {
        Log.warn(this, "Paths in dynamic datasets queries not supported yet") ;
        //throw new ARQException("Unexpected use of Path in for a dynamic dataset") ;
        return super.transform(op) ;
    }
    
//    @Override
//    public Op transform(OpTriple opTriple)
//    {
//        return super.transform(opTriple) ;
//    }
    
    @Override
    public Op transform(OpQuadPattern opQuadPattern)
    {
        Node gn = opQuadPattern.getGraphNode() ;

        if ( Quad.isDefaultGraphGenerated(gn) )  
            // Quad pattern directed at the default graph (not by explicit name). 
            return patternOver(defaultGraphs, opQuadPattern.getBasicPattern()) ;

        if ( gn.equals(Quad.defaultGraphIRI) )
            // <urn:x-arq:DefaultGraph>
            return patternOver(defaultGraphsReal, opQuadPattern.getBasicPattern()) ;
        
        if ( Quad.isUnionGraph(gn) )  
            // <urn:x-arq:UnionGraph>
            // Quad pattern directed at the union of (visible) named graphs 
            return patternOver(namedGraphs, opQuadPattern.getBasicPattern()) ;

        if ( gn.isVariable() )
        {
            // GRAPH ?g but no named graphs.
            if (namedGraphs.size() == 0 )
                return OpNull.create() ; 
            
            Var v = Var.alloc(gn) ;
            Op union = null ;
            for ( Node n : namedGraphs )
            {
                Binding b = BindingFactory.binding(v, n) ;
                Op x2 = QC.substitute(opQuadPattern, b) ;
                Op op = OpAssign.assign(x2, v, NodeValue.makeNode(n)) ;
                union = OpUnion.create(union, op) ;
            }
            return union ;
        }

        // Not a variable.
        if ( ! namedGraphs.contains(gn) )
            // No match. 
            return OpNull.create() ;
        // Nothing to do.
        return super.transform(opQuadPattern) ;
    }

    // Generate quad algebra that accesses the set of graphs as a single graph (including duplicate surpression). 
    private Op patternOver(Set<Node> graphs, BasicPattern basicPattern)
    {
        if ( basicPattern.size() == 0 )
            return OpTable.unit() ;
        
        if ( graphs.size() == 0 )
        {
            // No graphs => no results.
            return OpNull.create() ;
        }
        
        // WRONG - must be per triple pattern. 
        // distinct needed each step?
        
//        Op union = null ;
//         
//        for ( Node n : graphs )
//        {
//            Op pattern = new OpQuadPattern(n, basicPattern) ;
//            union = OpUnion.create(union, pattern) ;
//        }
        if ( graphs.size() == 1 )
        {
            // Simple rewrite.
            Node n = graphs.iterator().next() ;
            return new OpQuadPattern(n, basicPattern) ;
        }
            
        OpSequence opSeq = OpSequence.create() ;
        
        for ( Triple t : basicPattern )
        {
            // One expansion for each triple pattern.
            Op union = null ;
            for ( Node n : graphs )
            {
                BasicPattern bp = new BasicPattern() ;
                bp.add(t) ;
                Op pattern = new OpQuadPattern(n, bp) ;
                union = OpUnion.create(union, pattern) ;
            }
            opSeq.add(union) ;
        }
        
        // More than one graph - make distinct
        return new OpDistinct(opSeq) ;
    }

    @Override
    public Op transform(OpDatasetNames opDatasetNames)
    {
        Node gn = opDatasetNames.getGraphNode() ;
        if ( gn.isVariable() )
        {
            // Answer is a table.
            Table t = TableFactory.create() ;
            Var v = Var.alloc(gn) ;
            for ( Node n : namedGraphs )
            {
                Binding b = BindingFactory.binding(v, n) ;
                t.addBinding(b) ;
            }
            return OpTable.create(t) ; 
        }
        // Not a variable.
        if ( ! namedGraphs.contains(gn) )
            // No match. 
            return OpNull.create() ;
        // Nothing to do.
        return super.transform(opDatasetNames) ;
    }

    @Override
    public Op transform(OpGraph opGraph, Op x)
    {
        // We work on quad forms so this does not occur.  
        // But do it anyway, for completeness and for any later chnages.
        
        // What we need to do is a sequence whereby we loop over the namedGraphs
        // and try each possibility.
        
        Node gn = opGraph.getNode() ;
        if ( Quad.isDefaultGraph(gn) )
        {
            System.err.println("<Cough/>") ;
        }
        
        
        if ( namedGraphs.size() == 0 )
            return OpNull.create() ;
        
        if ( gn.isVariable() )
        {
            Op union = null ;
            Var v = Var.alloc(gn) ;
            
            for ( Node n : namedGraphs )
            {
                /* Graph evaluation is defined as:
                 * foreach IRI i in D
                 *    R := Union(R, Join( eval(D(D[i]), P) , Ω(?var->i) )
                 */
                // Do before join classification and optimization.
                Op op = OpTable.create(new Table1(v, n)) ;
                op = OpJoin.create(op, x) ;
                op = new OpGraph(n, op) ;
                // Don't need an assign.  The table did that.
                // op = OpAssign.assign(op, v, NodeValue.makeNode(n)) ;
                union = OpUnion.create(union, op) ;
            }
            return union ;
        }

        // Not a variable.
        if ( ! namedGraphs.contains(gn) )
            // No match. 
            return OpNull.create() ;
        // Nothing to do.
        return super.transform(opGraph, x) ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
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