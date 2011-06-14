/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

/** A transaction handle */
public class Transaction
{
    // TODO split internal and external features.
    private final long id ;
    private final TransactionManager txnMgr ;
    private final List<Iterator<?>> iterators ; 
    private final List<Journal> journals = new ArrayList<Journal>() ;
    private final List<ObjectFileTrans> objJournals = new ArrayList<ObjectFileTrans>() ;

    public Transaction(long id, TransactionManager txnMgr)
    {
        this.id = id ;
        this.txnMgr = txnMgr ;
        this.iterators = new ArrayList<Iterator<?>>() ;
    }

    public void commit()
    {
        // Write commit entry.
        if ( journals.size() > 0 )
        {
            Journal jrnl = journals.get(0) ;
            JournalEntry entry = new JournalEntry(JournalEntryType.Commit, null) ;
            jrnl.writeJournal(entry) ;
        }
        
        
        for ( ObjectFileTrans jrnl : objJournals )
            jrnl.sync() ;
    
        for ( Journal jrnl : journals )
            jrnl.sync() ;
    }
    
    public void abort()                             { txnMgr.abort(this) ; }
    public long getTxnId()                          { return id ; }
    public TransactionManager getTxnMgr()           { return txnMgr ; }
    
    public void addIterator(Iterator<?> iter)       { iterators.add(iter) ; }
    public void removeIterator(Iterator<?> iter)    { iterators.remove(iter) ; }
    public List<Iterator<?>> iterators()            { return Collections.unmodifiableList(iterators) ; }
    
    public void add(Journal journal)
    {
        journals.add(journal) ;
    }

    public void add(ObjectFileTrans objFileTrans)
    {
        objJournals.add(objFileTrans) ;
    }
}

/*
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