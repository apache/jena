/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;


/**
 * A query iterator that joins two or more iterators into a single iterator.
 * 
 * @author Andy Seaborne
 */ 

public class QueryIterConcat extends QueryIter
{
    boolean initialized = false ;
    List<QueryIterator> iteratorList = new ArrayList<QueryIterator>() ; 
    Iterator<QueryIterator> iterator ;
    QueryIterator currentQIter = null ;

    Binding binding ;
    boolean doneFirst = false ;

    public QueryIterConcat(ExecutionContext context)
    {
        super(context) ;
    }

    private void init()
    {
        if ( ! initialized )
        {
            currentQIter = null ;
            if ( iterator == null )
                iterator = iteratorList.listIterator() ;
            if ( iterator.hasNext() )
                currentQIter = iterator.next() ;
            initialized = true ;
        }
    }
    
    public void add(QueryIterator qIter)
    {
        if ( qIter != null )
            iteratorList.add(qIter) ; 
    }
    
    
    @Override
    protected boolean hasNextBinding()
    {
        if ( isFinished() )
            return false ;

        init() ;
        if ( currentQIter == null )
            return false ;
        
        while ( ! currentQIter.hasNext() )
        {
            // End sub iterator
            //currentQIter.close() ;
            currentQIter = null ;
            if ( iterator.hasNext() )
                currentQIter = iterator.next() ;
            if ( currentQIter == null )
            {
                // No more.
                //close() ;
                return false ;
            }
        }
        
        return true ;
    }

    @Override
    protected Binding moveToNextBinding()
    {
        if ( ! hasNextBinding() )
            throw new NoSuchElementException(Utils.className(this)) ; 
        if ( currentQIter == null )
            throw new NoSuchElementException(Utils.className(this)) ; 
        
        Binding binding = currentQIter.nextBinding() ;
        return binding ;
    }

    
    @Override
    protected void closeIterator()
    {
        for ( Iterator<QueryIterator> iter = iteratorList.iterator() ; iter.hasNext() ; )
        {
            QueryIterator qIter = iter.next() ;
            if ( qIter != null )
                qIter.close() ;
        }
    }
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    { 
        out.println(Utils.className(this)) ;
        out.incIndent() ;
        for ( Iterator<QueryIterator> iter = iteratorList.iterator() ; iter.hasNext() ; )
        {
            QueryIterator qIter = iter.next() ;
            qIter.output(out, sCxt) ;
        }
        out.decIndent() ;
        out.ensureStartOfLine() ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
