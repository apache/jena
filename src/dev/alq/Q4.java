/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Element;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.QueryEngine;
import com.hp.hpl.jena.query.engine2.AlgebraCompilerQuad;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.Store;

/** Highly experimental quad engine */

public class Q4 extends QueryEngine
{
    private static Log log = LogFactory.getLog(Q4.class) ; 
    Store store ;
    
    public Q4(Store store, Query q)
    {
        super(q) ;
        this.store = store ;
    }
    
    @Override
    protected PlanElement makePlanForQueryPattern(Context context, Element queryPatternElement)
    {
        Op op =  SDBCompiler.compile(context, queryPatternElement) ;
        
        // Quad tree. Now what?
        return null ;
    }
    
    public SqlNode kick()
    {
        Element queryPatternElement = query.getQueryPattern() ;
        Op op = SDBCompiler.compile(context, queryPatternElement) ;
        CompileContext cxt = new CompileContext(store, query) ;
        return new QuadToSDB(cxt).compile(op) ;
    }
}

// Temporary - access to a protected while we think about the correct overall
// design of the algebra compiler (which is just for SDB).   
class SDBCompiler extends AlgebraCompilerQuad
{
    static Op compile(Context context, Element queryPatternElement) 
    { return new SDBCompiler(context).compileFixedElement(queryPatternElement) ; }
    
    public SDBCompiler(Context context)
    {
        super(context) ;
    }
    public Op dwim(Element queryPatternElement)
    {
        return super.compileFixedElement(queryPatternElement) ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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