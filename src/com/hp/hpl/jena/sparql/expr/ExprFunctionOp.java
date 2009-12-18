/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCheck;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.syntax.Element;


/** A "function" that executes over a pattern */
 
public abstract class ExprFunctionOp extends ExprFunction
{
    private Op op ;
    private Element element ;
    
    protected ExprFunctionOp(String fName, Element el, Op op)
    {
        super(fName) ;
        this.op = op ;
        this.element = el ;
    }

    @Override
    public Expr getArg(int i)
    {
        return null ;
    }
    
    public Op getOp()       { return op ; }
    public Element getElement()       { return element ; }
    
    @Override
    public int numArgs() { return 0 ; }
    
    // ---- Evaluation
    
    @Override
    public final NodeValue eval(Binding binding, FunctionEnv env)
    {
        ExecutionContext execCxt = new ExecutionContext(env.getContext(),
                                                        env.getActiveGraph(),
                                                        env.getDataset(),
                                                        QC.getFactory(env.getContext())
                                                        ) ;
        QueryIterator qIter1 = QueryIterSingleton.create(binding, execCxt) ;
        QueryIterator qIter = QC.execute(op, qIter1, execCxt) ;
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt) ;
        // Call the per-operation functionality.
        NodeValue v = eval(binding, qIter, env) ;
        qIter.close() ;
        return v ;
    }
    
    protected abstract NodeValue eval(Binding binding, QueryIterator iter, FunctionEnv env) ;
    
    @Override
    public void visit(ExprVisitor visitor) 
    { visitor.visit(this) ; }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
