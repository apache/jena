/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Utils;

/** Execution Control Block : supports tracking iterators.
 */  

public class ExecCtl
{
    private static Log log = LogFactory.getLog(ExecCtl.class) ;
    
    private Context context       = null ;
    private DatasetGraph dataset  = null ;
    private Query query           = null ;
    
    // Iterator tracking
    private Collection openIterators      = null ;
    private Collection allIterators       = null ;

    /** For testing only */
    protected ExecCtl(Context context) 
    {
        this.context = context ;
    }

    /** Clone - shares tracking */
    protected ExecCtl(ExecCtl other) 
    {
        this.context = other.context ;
        this.dataset = other.dataset ;
        this.query = other.query ;
        this.openIterators = other.openIterators ;
        this.allIterators = other.allIterators ;
    }

    /** Clone - shares tracking */
    protected ExecCtl(ExecCtl other, DatasetGraph dataset) 
    {
        this.context = other.context ;
        this.dataset = dataset ;
        this.query = other.query ;
        this.openIterators = other.openIterators ;
        this.allIterators = other.allIterators ;
    }


    public ExecCtl(Context params, Query query, DatasetGraph dataset)
    {
        context = params ;
        this.dataset = dataset ;
        this.query = query ;
        openIterators = new ArrayList() ;
        allIterators  = new ArrayList() ;
    }
    
    public Context getContext()       { return context ; }
    public DatasetGraph getDataset()  { return dataset ; }
    public Query getQuery()           { return query ; }
    
    public void openIterator(QueryIterator qIter)
    {
        openIterators.add(qIter) ;
        allIterators.add(qIter) ;
    }

    public void closedIterator(QueryIterator qIter)
    {
        openIterators.remove(qIter) ;
    }

    public Iterator listOpenIterators()  { return openIterators.iterator() ; }
    public Iterator listAllIterators()   { return allIterators.iterator() ; }

    /** Debugging operation - dumps state of the context */
    public void dump() { dump(false) ; }

    /** Debugging operation - dumps state of the context */
    public void dump(boolean includeAll)
    {
        if ( includeAll )
        {
            Iterator iterAll = listAllIterators() ;
            
            while(iterAll.hasNext())
            {
                QueryIterator qIter = (QueryIterator)iterAll.next() ;
                warn(qIter, "Iterator: ") ;
            }
        }
        
        Iterator iterOpen = listOpenIterators() ;
        while(iterOpen.hasNext())
        {
            QueryIterator qIterOpen = (QueryIterator)iterOpen.next() ;
            warn(qIterOpen, "Open iterator: ") ;
        }
    }
    
    private void warn(QueryIterator qIter, String str)
    {
        str = str + Utils.className(this) ;
        
        if ( qIter instanceof QueryIteratorBase )
        {
            QueryIteratorBase qIterBase = (QueryIteratorBase)qIter ;
            {
                QueryIter qIterLN = (QueryIter)qIter ;
                str = str+"/"+qIterLN.getIteratorNumber() ;
            }
            String x = qIterBase.debug() ;
            if ( x.length() > 0 )
                str = str+" : "+x ;
        }
        log.warn(str) ;
    }
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