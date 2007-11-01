/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.JenaException;


public class TransactionHandlerSDB implements TransactionHandler
{
    static private Log log = LogFactory.getLog(TransactionHandlerSDB.class) ;

    GraphSDB graph ;
    boolean inTransaction ;
    
    public TransactionHandlerSDB(GraphSDB graph)
    { 
        this.graph = graph ;
        this.inTransaction = false ;
    }
    
    public boolean transactionsSupported() { return true ; }
    
    // Simplistic
    
    public synchronized void begin()
    {
        if ( inTransaction )
        {
            log.warn("beginTransaction: Already in a transaction") ;
            throw new SDBException("Already in transaction") ;
        }
        inTransaction = true;
        graph.startBulkUpdate();
    }
    
    public synchronized void commit()
    {
        if ( ! inTransaction )
        {
            log.warn("commit: Not in a transaction") ;
            return ;
        }
        graph.finishBulkUpdate();
        inTransaction = false;
    } 
    
    public synchronized void abort()
    {
        if ( ! inTransaction )
        {
            log.warn("abort: Not in a transaction") ;
            return ;
        }
        graph.getStore().getLoader().abortBulkUpdate();
        graph.finishBulkUpdate();
        inTransaction = false;
    }

    public synchronized void abortFinally()
    {
        // Abort if needed.
        if ( ! inTransaction )
            return ;
        abort() ;
    }

    public void abortSilent()
    { try { abortFinally() ; } catch (SDBExceptionSQL ex) {} } 

    
    public Object executeInTransaction(Command c)
    {
        try {
            begin() ;
            Object result = c.execute();
            commit();
            return result;
        } 
        catch (SDBExceptionSQL e) { abortFinally() ; throw e ; } 
        catch (JenaException e)   { abortFinally() ; throw e ; }
        catch (Throwable e) { abortFinally() ; throw new SDBException(e) ; } // Pass Graph tests.
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