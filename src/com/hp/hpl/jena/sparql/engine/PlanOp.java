/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

public class PlanOp extends PlanBase
{
    private QueryIterator qIter ;
    private String label = null ;
    
    public PlanOp(Op op, QueryIterator qIter)
    { 
        super(op) ;
        this.qIter = qIter ;
    }

    public PlanOp(String label, Op op, QueryIterator qIter)
    {
        this(op, qIter) ;
        this.label = label ;
    }
    
    protected QueryIterator iteratorOnce()
    { return qIter ; }

    //@Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        if ( getOp() == null )
        {
            out.println(Utils.className(this)) ;
            return ;
        }
        if ( label != null )
        {
            out.print(Plan.startMarker) ;
            out.println(label) ;
            out.incIndent() ;
        }
        
        //getOp().output(out, sCxt) ;
        iterator().output(out, sCxt) ;
        
        if ( label != null )
        {
            out.print(Plan.finishMarker) ;
            out.decIndent() ;
        }
        out.ensureStartOfLine() ;
    }
}
/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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