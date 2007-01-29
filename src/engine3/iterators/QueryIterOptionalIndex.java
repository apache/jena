/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3.iterators;

import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterDefaulting;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpFilter;
import com.hp.hpl.jena.query.expr.Expr;

import engine3.QC;


public class QueryIterOptionalIndex extends QueryIterStream
{
    private Op op ;

    public QueryIterOptionalIndex(QueryIterator input, Op op, Expr expr, ExecutionContext context)
    {
        super(input, context) ;
        // In an indexed left join, the LHS bindings are visible to the
        // RHS execution so the expression is evaluted by moving it to be 
        // a filter over the RHS pattern. 
        if ( expr != null )
            op = OpFilter.filter(expr, op) ;
        this.op = op ;
    }

    protected QueryIterator nextStage(Binding binding)
    {
        // Can lead to repeated substitutions, all the way down.
        // But depth is uncommon (except in artifical queries designed
        // to test the algebra or engine!)
        
        Op op2 = QC.substitute(op, binding) ;
        
        System.out.println("===="+super.getIteratorNumber()) ;
        System.out.println(binding) ;
        System.out.println(op) ;
        
        // Seems shame to create an iterator just so we can wrap the input binding
        // but we had to unwrap the thing to do the substitute.
        // Maybe another way to pass the parent binding into QC.compile?
        QueryIterator thisStep = new QueryIterSingleton(binding, getExecContext()) ;
        
        QueryIterator cIter = QC.compile(op2, thisStep, super.getExecContext()) ;
        cIter = new QueryIterDefaulting(cIter, binding, getExecContext()) ;
        return cIter ;
    }
    
    protected void closeIterator() 
    { super.closeIterator() ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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