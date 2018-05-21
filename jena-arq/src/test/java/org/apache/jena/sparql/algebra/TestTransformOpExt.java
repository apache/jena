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

package org.apache.jena.sparql.algebra;

import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.algebra.op.OpExt ;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.writers.WriterOp ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;
import org.junit.Test ;

/** Tests for OpExt */ 
public class TestTransformOpExt extends BaseTest
{
    // An OpExt
    static class OpExtTest extends OpExt {
        private Op op ;

        OpExtTest(Op op) {
            super("test") ;
            this.op = op ;
        }

        @Override
        public Op apply(Transform transform, OpVisitor before, OpVisitor after) {
            Op opx = Transformer.transformSkipService(transform, null, op, before, after) ;
            return new OpExtTest(opx) ;
        }

        @Override public Op effectiveOp() { return op ; }

        @Override public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
        { return null ; }

        @Override
        public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
            out.println();
            WriterOp.output(out, op, sCxt) ;
        }

        @Override
        public int hashCode() { return 0 ; }

        @Override
        public boolean equalTo(Op other, NodeIsomorphismMap labelMap) { 
            return other instanceof OpExtTest ; 
        }
    }
    
    static class OpExtTransform extends TransformCopy {
        
        @Override
        public Op transform(OpFilter opFilter, Op subOp) {
            return new OpExtTest(opFilter);
        }
    }
    
    @Test public void textOpExtQuads() {
        String x = StrUtils.strjoinNL
            ("(graph <g>"
            ,"    (join"
            , "     (bgp (?s ?p ?o))"
            ,"      (graph <g2> (bgp (?s ?p ?o)))"
            ,"))"
            ) ;
        
        String y = StrUtils.strjoinNL
            ("(join"
            ,"   (quadpattern (quad <g> ?s ?p ?o))"
            ,"   (quadpattern (quad <g2> ?s ?p ?o)))"
            ) ;

        // Build 
        Op op = SSE.parseOp(x) ;
        OpGraph opg = (OpGraph)op ;
        
        // Insert OpExtTest
        Op op1 = opg.getSubOp() ;
        op1 = new OpExtTest(op1) ;
        
        op = new OpGraph(opg.getNode(), op1) ;
        Op op2 = AlgebraQuad.quadize(op) ;
        
        assertTrue(op2 instanceof OpExt) ;
        Op opSub = ((OpExt)op2).effectiveOp() ;
        Op expectedSub = SSE.parseOp(y) ;
        assertEquals(expectedSub, opSub) ;
    }
    
    @Test
    public void testOpExtMinus() {
        String sse = StrUtils.strjoinNL
                ("(filter (= ?o 1)"
                ,"   (bgp (?s ?p ?o))"
                ,")"
                ) ;
        
        // Build 
        Op op = SSE.parseOp(sse) ;
        
        // Transform
        op = Transformer.transformSkipService(new OpExtTransform(), op) ;
        
        // Check WalkerVisitorVisible
        Set<Var> vars = OpVars.visibleVars(op);
        assertEquals(3, vars.size());
    }
}

