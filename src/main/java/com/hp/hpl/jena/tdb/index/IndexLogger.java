/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import java.util.Iterator ;

import org.slf4j.Logger ;

import com.hp.hpl.jena.tdb.base.record.Record ;

public final class IndexLogger extends IndexWrapper
{
    private final Logger log ;

    public IndexLogger(RangeIndex rIdx, Logger log)
    {
        super(rIdx) ;
        this.log = log ;
    }

    @Override
    public boolean add(Record record)
    { 
        log.info("Add: "+record) ;
        return super.add(record) ; 
    }

    @Override
    public boolean delete(Record record)
    { 
        log.info("Delete: "+record) ;
        return super.delete(record) ; 
    }

    @Override
    public Record find(Record record)
    {
        log.info("Find: "+record) ;
        Record r2 = super.find(record) ;
        log.info("Find: "+record+" ==> "+r2) ;
        return r2 ;
    }

    @Override
    public Iterator<Record> iterator()
    {
        log.info("iterator()") ;
        return super.iterator() ;
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