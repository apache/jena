/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpJoin extends Op2
{
    // This is the SPARQL simplification step done inline.
    // Which is wrong IMHO
//    private static boolean simplifyEarly = false ; 
    
    public static Op create(Op left, Op right)
    { 
//        // Inline simplification (too early - changes SPARQL for OPTIONAL {{ FILTER }} 
//        if ( simplifyEarly )
//        {
//            if ( isJoinIdentify(left) )
//                return right ;
//
//            if ( isJoinIdentify(right) )
//                return left ;
//        }
        
        return new OpJoin(left, right) ;
    }
    
    public static boolean isJoinIdentify(Op op)
    {
        if ( ! ( op instanceof OpTable ) )
            return false ;
        Table t = ((OpTable)op).getTable() ;
        // Safe answer.
        return (t instanceof TableUnit) ;
        
    }
    
    private OpJoin(Op left, Op right) { super(left, right) ; }
    
    public String getName() { return "join" ; }

    public Op apply(Transform transform, Op left, Op right)
    { return transform.transform(this, left, right) ; }
        
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    public Op copy(Op newLeft, Op newRight)
    { return new OpJoin(newLeft, newRight) ; }
    
    public boolean equalTo(Op op2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op2 instanceof OpJoin) ) return false ;
        return super.sameAs((Op2)op2, labelMap) ;
    }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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