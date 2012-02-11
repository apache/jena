/**
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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple ;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarAlloc ;
import com.hp.hpl.jena.sparql.path.* ;

/** The path transformation step exactly as per teh SPARQL 1.1 spec.
 *  It does not produce very nice execution structyres so ARQ uses
 *  a functional equivalent, bit different, transformation.
 */
public class TransformPathFlatternStd extends TransformCopy
{
    public TransformPathFlatternStd() { }
    
    @Override
    public Op transform(OpPath opPath)
    {
        TriplePath tp = opPath.getTriplePath() ;
        Op op = transformPath(opPath, tp.getSubject(), tp.getPath(), tp.getObject() ) ;
        // And combine adjacent triple patterns.
        return op ;
    }
    
    static Op transformPath(OpPath op, Node subject, Path path, Node object)
    {
        PathTransform transform = new PathTransform(subject, object) ;
        path.visit(transform) ;
        Op r = transform.getResult() ;
        if ( r == null )
        {
            if ( op == null )
            {
                TriplePath tp = new TriplePath(subject, path, object) ;
                op = new OpPath(tp) ;
            }
            return op ;
        }
            
        return r ;
    }
    
    static VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker+"Q") ;
    
    static Op join(Op op1, Op op2)
    {
        if ( op1 == null )
            return op2 ;
        if ( op2 == null )
            return op1 ;
        return OpJoin.create(op1, op2) ;
    }
    
    static class PathTransform implements PathVisitor
    {
        private final Node subject ;
        private final Node object ;
        private Op result ;
        Op getResult()
        {
            return result ;
        }
        
        public PathTransform(Node subject, Node object)
        {
            this.subject = subject ;
            this.object = object ;
        }
        
        @Override
        public void visit(P_Link pathNode)
        {
            Op op = new OpTriple(new Triple(subject, pathNode.getNode(), object)) ;
            result = op  ;
        }
        
        @Override
        public void visit(P_ReverseLink pathNode)
        {
            Op op = new OpTriple(new Triple(object, pathNode.getNode(), subject)) ;
            result = op  ;
        }
        
        @Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            // No change.
        }
        
        @Override
        public void visit(P_Inverse inversePath)
        {
            PathTransform pt = new PathTransform(object, subject) ;
            inversePath.getSubPath().visit(pt) ;
            result = pt.getResult() ;
        }
        
        @Override
        public void visit(P_Mod pathMod)
        {
            if ( pathMod.getMin() > pathMod.getMax() )
                throw new ARQException("Bad path: "+pathMod) ;
            
            Op op = null ;
            for ( long i = pathMod.getMin() ; i <= pathMod.getMax() ; i++ )
            {
                Path p = PathFactory.pathFixedLength(pathMod.getSubPath(), i) ;
                Op sub = transformPath(null, subject, p, object) ;
                op = OpUnion.create(op, sub) ;
            }
            result = op ;
        }
        
        @Override
        public void visit(P_FixedLength pFixedLength)
        {
            Op op = null ;
            Var v1 = null ;
            for ( int i = 0 ; i < pFixedLength.getCount() ; i++ )
            {
                Var v2 = varAlloc.allocVar() ;
                Node s = (v1 == null) ? subject : v1 ;
                Node o = (i == pFixedLength.getCount()-1) ? object : v2 ;
                Op op1 = transformPath(null, s, pFixedLength.getSubPath() , o) ;
                op = join(op,  op1) ;
                v1 = v2 ;
            }
            result = op ;
        }
        
        @Override
        public void visit(P_Distinct pathDistinct)
        {
            // No change.
            result = null ;
        }
        
        @Override
        public void visit(P_ZeroOrOne path)
        {
            // No change.
            result = null ;
        }
        
        @Override
        public void visit(P_ZeroOrMore path)
        {
            // No change.
            result = null ;
        }
        
        @Override
        public void visit(P_OneOrMore path)
        {
            // No change.
            result = null ;
        }
        
        @Override
        public void visit(P_Alt pathAlt)
        {
            Op op1 = transformPath(null, subject, pathAlt.getLeft() , object) ;
            Op op2 = transformPath(null, subject, pathAlt.getRight() , object) ;
            result = OpUnion.create(op1, op2) ;
        }
        
        @Override
        public void visit(P_Seq pathSeq)
        {
            Var v = varAlloc.allocVar() ;
            Op op1 = transformPath(null, subject, pathSeq.getLeft() , v) ;
            Op op2 = transformPath(null, v, pathSeq.getRight() , object) ;
            result = join(op1, op2) ;
        }
    }
}
