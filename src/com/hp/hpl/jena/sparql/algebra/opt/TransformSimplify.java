/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;

public class TransformSimplify extends TransformCopy
{
    @Override
    public Op transform(OpSequence opSequence, List<Op> elts)
    {
        List<Op> x = new ArrayList<Op>(elts) ;
        for ( Iterator<Op> iter = x.iterator() ; iter.hasNext() ; )
        {
            Op sub = iter.next() ;
            if ( OpJoin.isJoinIdentify(sub) )
                iter.remove();
        }
        return super.transform(opSequence, x) ;
    }
    
    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    {
        if ( OpJoin.isJoinIdentify(left) )
            return right ;
        if ( OpJoin.isJoinIdentify(right) )
            return left ;
        // Merge adjacent BGPs
        // Also works on nested subqueries that turned out to be simple BGPs.
        
//        if ( OpBGP.isBGP(left) && OpBGP.isBGP(right) )
//        {
//            BasicPattern pattern = new BasicPattern() ;
//            pattern.addAll( ((OpBGP)left).getPattern() ) ;
//            pattern.addAll( ((OpBGP)right).getPattern() ) ;
//            return new OpBGP(pattern) ;
//        }
        
        return super.transform(opJoin, left, right) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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