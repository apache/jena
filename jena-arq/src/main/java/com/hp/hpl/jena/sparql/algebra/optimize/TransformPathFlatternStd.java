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

import java.util.ArrayList ;
import java.util.List ;

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

/** The path transformation step exactly as per the SPARQL 1.1 spec.
 *  i.e. joins triples rather creatign BGPs. 
 *  It does not produce very nice execution structures so ARQ uses
 *  a functional equivalent, but different, transformation.
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
                op = make(subject, path, object) ;
            return op ;
        }
            
        return r ;
    }
    
    static OpPath make(Node subject, Path path, Node object)
    {
        TriplePath tp = new TriplePath(subject, path, object) ;
        return new OpPath(tp) ;
    }
    
    static VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarAnonMarker+"Q") ;
    
    private static Op join(Op op1, Op op2)
    {
        return OpJoin.create(op1, op2) ;
    }
    
    private static Op union(Op left, Op right)
    {
        return new OpUnion(left, right) ;
    }
    
    static class PathTransform extends PathVisitorBase
    {
        private final Node subject ;
        private final Node object ;
        private Op result = null ;
        Op getResult() { return result ; }
        
        public PathTransform(Node subject, Node object)
        {
            this.subject = subject ;
            this.object = object ;
            this.result = null ;
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
        
        /*
         * Reverse transformations.
         * X !(^:uri1|...|^:urin)Y                      ==>  ^(X !(:uri1|...|:urin) Y)
         * Split into forward and reverse.
         * X !(:uri1|...|:urii|^:urii+1|...|^:urim) Y   ==>  { X !(:uri1|...|:urii|)Y } UNION { X !(^:urii+1|...|^:urim) Y } 
         */
        @Override
        public void visit(P_NegPropSet pathNotOneOf)
        {
            Op opFwd = null ;
            Op opBwd = null ;
            List<P_Path0> forwards = new ArrayList<>() ;
            List<P_Path0> backwards = new ArrayList<>() ;
            
            for ( P_Path0 p :  pathNotOneOf.getNodes() )
            {
                if ( p.isForward() )
                    forwards.add(p) ;
                else
                    backwards.add(p) ;
            }
                
            if ( ! forwards.isEmpty() )
            {
                P_NegPropSet pFwd = new P_NegPropSet() ;
                for ( P_Path0 p : forwards )
                    pFwd.add(p) ;
                opFwd = make(subject, pFwd, object) ;
            }
            
            if ( ! backwards.isEmpty() )
            {
                // Could reverse here.
                P_NegPropSet pBwd = new P_NegPropSet() ;
                for ( P_Path0 p : backwards )
                    pBwd.add(p) ;
                opBwd = make(subject, pBwd, object) ;
            }
            
            if ( opFwd == null && opBwd == null )
            {
                result = make(subject, pathNotOneOf, object) ;
                return ; 
            }
            
            result = union(opFwd, opBwd) ;
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
                op = union(op, sub) ;
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
        public void visit(P_Alt pathAlt)
        {
            Op op1 = transformPath(null, subject, pathAlt.getLeft() , object) ;
            Op op2 = transformPath(null, subject, pathAlt.getRight() , object) ;
            result = union(op1, op2) ;
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
