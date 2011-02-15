/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/**
 * This class makrs a QueryIter that takes one QueryIterator as input. */
public abstract class QueryIter2 extends QueryIter
{
    private QueryIterator leftInput ; 
    private QueryIterator rightInput ;
    
    public QueryIter2(QueryIterator left, QueryIterator right, ExecutionContext execCxt)
    { 
        super(execCxt) ;
        this.leftInput = left ;
        this.rightInput = right ;
    }
    
    protected QueryIterator getLeft()   { return leftInput ; } 
    protected QueryIterator getRight()  { return rightInput ; } 
    
    @Override
    protected final
    void closeIterator()
    {
        closeSubIterator() ;
        performClose(leftInput) ;
        performClose(rightInput) ;
        leftInput = null ;
        rightInput = null ;
    }
    
    @Override
    protected final
    void requestCancel()
    {
        performRequestCancel(leftInput) ;
        performRequestCancel(rightInput) ;
    }
    
    /** Cancellation of the query execution is happening */
    protected abstract void requestSubCancel() ;
    
    /** Pass on the close method - no need to close the left or right QueryIterators passed to the QueryIter1 constructor */
    protected abstract void closeSubIterator() ;
    
    // Do better
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        
        out.print(Plan.startMarker) ;
        out.incIndent() ;
        getLeft().output(out, sCxt) ;
        out.decIndent() ;
        //out.ensureStartOfLine() ;
        out.println(Plan.finishMarker) ;
        
        out.print(Plan.startMarker) ;
        out.incIndent() ;
        getRight().output(out, sCxt) ;
        out.decIndent() ;
        //out.ensureStartOfLine() ;
        out.println(Plan.finishMarker) ;
        
        out.decIndent() ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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