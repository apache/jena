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

package com.hp.hpl.jena.sparql.algebra.optimize ;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;

/** Merge BGPs 
 * 
 * <li>(join BGP1 BGP2) => BGP
 * <li>(sequence ...BGP1 BGP2 ...) => (sequence ...BGP ...)
 */
public class TransformMergeBGPs extends TransformCopy {
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        BasicPattern p1 = asBGP(left) ;
        BasicPattern p2 = asBGP(right) ;
        if ( p1 != null && p2 != null ) {
            BasicPattern p = merge(p1, p2) ;
            return new OpBGP(p) ;
        }

        return super.transform(opJoin, left, right) ;
    }

    @Override
    public Op transform(OpSequence opSequence, List<Op> elts) {
        // First check whether we need to do anything at all.
        // Check for two BGPs.
        boolean xform = false ;
        boolean previousBGP = false ;

        for ( Op op1 : elts ) {
            BasicPattern p1 = asBGP(op1) ;
            if ( previousBGP && p1 != null ) {
                xform = true ;
                break ;
            }
            previousBGP = (p1 != null) ;
        }

        if ( !xform )
            // Nothing to do here.
            return super.transform(opSequence, elts) ;

        OpSequence seq2 = OpSequence.create() ;

        for ( int i = 0 ; i < elts.size() ; i++ ) {
            Op op = elts.get(i) ;

            BasicPattern p1 = asBGP(op) ;
            if ( p1 == null ) {
                // Do nothing
                seq2.add(op) ;
                continue ; // Outer loop.
            }

            // This is the op after the merge, if any.
            BasicPattern pMerge = new BasicPattern() ;
            seq2.add(new OpBGP(pMerge)) ;
            // Merge any BGPs from here on ...
            // Re-gets the BGP that trigegrs this all.
            for ( ; i < elts.size() ; i++ ) {
                // Look at next element.
                Op opNext = elts.get(i) ;

                BasicPattern p2 = asBGP(opNext) ;
                if ( p2 == null ) {
                    seq2.add(opNext) ;
                    break ;
                }

                // Merge.
                pMerge.addAll(p2) ;
            }
        }
        if ( seq2.size() == 1 )
            return seq2.get(0) ;
        return seq2 ;
    }

    private static BasicPattern asBGP(Op op) {
        if ( op instanceof OpBGP )
            return ((OpBGP)op).getPattern() ;
        return null ;
    }

    private static BasicPattern merge(BasicPattern p1, BasicPattern p2) {
        if ( p1 == null || p2 == null )
            return null ;
        BasicPattern p = new BasicPattern() ;
        p.addAll(p1) ;
        p.addAll(p2) ;
        return p ;
    }

}
