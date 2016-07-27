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

package org.apache.jena.sdb.store;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sdb.SDB ;
import org.apache.jena.sdb.engine.QueryEngineSDB ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpQuadPattern ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.Plan ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingRoot ;

public class LibSDB {
    
    /** Match a quad pattern (not triples in the default graph) */
    public static Iterator<Quad> findInQuads(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        // If null, create and remember a variable, else use the node.
        final Node vg = varOrConst(g,"g") ;
        final Node vs = varOrConst(s,"s") ;
        final Node vp = varOrConst(p,"p") ;
        final Node vo = varOrConst(o,"o") ;

        Triple triple = new Triple(vs, vp ,vo) ;
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple);
        Op op = new OpQuadPattern(vg, pattern) ;
        
        Plan plan = QueryEngineSDB.getFactory().create(op, dsg, BindingRoot.create(), null) ;
        QueryIterator qIter = plan.iterator() ;
        Iterator<Binding> iter ;
        if ( SDB.getContext().isTrue(SDB.streamGraphAPI) ) {
            iter = qIter ;
        } else {
            // ---- Safe version: 
            List<Binding> x = Iter.toList(qIter) ;
            Iterator<Binding> qIter2 = x.iterator() ;
            qIter.close();
            iter = qIter2 ;
        } 
        return Iter.map(iter, (b) -> bindingToQuad(vg, vs, vp, vo, b)) ;
    }

    /** Find triples, in the default graph or a named graph. */
    public static Iterator<Triple> findTriplesInDftGraph(DatasetGraph dsg, Node s, Node p, Node o) {
        return findTriples(dsg, null, s, p, o) ; 
    }

        /** Find triples, in the default graph or a named graph. */
    public static Iterator<Triple> findTriples(DatasetGraph dsg, Node g, Node s, Node p, Node o) {
        if ( Var.isVar(g) )
            throw new InternalErrorException("Graph node is a variable : "+g) ;
        
        final Node vs = varOrConst(s,"s") ;
        final Node vp = varOrConst(p,"p") ;
        final Node vo = varOrConst(o,"o") ;

        // Evaluate as an algebra expression
        Triple triple = new Triple(vs, vp ,vo) ;
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple);
        Op op = ( g != null ) ? new OpQuadPattern(g, pattern) : new OpBGP(pattern) ;
        
        Plan plan = QueryEngineSDB.getFactory().create(op, dsg, BindingRoot.create(), null) ;
        QueryIterator qIter = plan.iterator() ;
        Iterator<Binding> iter ;
        if ( SDB.getContext().isTrue(SDB.streamGraphAPI) ) {
            // Assumes iterator closed properly.
            iter = qIter ;
        } else {
            // ---- Safe version: 
            List<Binding> x = Iter.toList(qIter) ;
            Iterator<Binding> qIter2 = x.iterator() ;
            qIter.close();
            iter = qIter2 ;
        } 
        return Iter.map(iter, (b) -> bindingToTriple(vs, vp, vo, b)) ;
    }

    private static Node varOrConst(Node n, String varName) {
        if ( n != null && n != Node.ANY )
            return n;
        return Var.alloc(varName) ;
    }         

    private static Quad bindingToQuad(Node vg, Node vs, Node vp, Node vo, Binding binding)
    {
        Node gResult = vg ;
        Node sResult = vs ;
        Node pResult = vp ;
        Node oResult = vo ;

        if ( Var.isVar(vg) )
            gResult = binding.get(Var.alloc(vg)) ;
        if ( Var.isVar(vs) )
            sResult = binding.get(Var.alloc(vs)) ;
        if ( Var.isVar(vp) )
            pResult = binding.get(Var.alloc(vp)) ;
        if ( Var.isVar(vo) )
            oResult = binding.get(Var.alloc(vo)) ;
        
        return Quad.create(gResult, sResult, pResult, oResult) ;
    }
    
    private static Triple bindingToTriple(Node vs, Node vp, Node vo, Binding binding)
    {
        Node sResult = vs ;
        Node pResult = vp ;
        Node oResult = vo ;

        if ( Var.isVar(vs) )
            sResult = binding.get(Var.alloc(vs)) ;
        if ( Var.isVar(vp) )
            pResult = binding.get(Var.alloc(vp)) ;
        if ( Var.isVar(vo) )
            oResult = binding.get(Var.alloc(vo)) ;
        
        return Triple.create(sResult, pResult, oResult) ;
    }

}