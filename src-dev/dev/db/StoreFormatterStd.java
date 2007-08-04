/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.StoreFormatterBase;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class StoreFormatterStd extends StoreFormatterBase
{
    public StoreFormatterStd(SDBConnection conn)
    {
        super(conn) ;
    }
    
//    public void create()
//    {
//        format() ;
//        addIndexes() ;
//    }

    public void format()
    { 
        formatTablePrefixes() ;
        formatTableNodes() ;
        formatTableTriples() ;
        formatTableQuads() ;
    }
    
    public void truncate()
    {
        truncateTablePrefixes() ;
        truncateTableNodes() ;
        truncateTableTriples() ;
        truncateTableQuads() ;
    }
    
    public void addIndexes()
    {
        addIndexesTableTriples() ;
        addIndexesTableQuads() ;
    }
    
    public void dropIndexes()
    {
        dropIndexesTableTriples() ;
        dropIndexesTableQuads() ;
    }

    public static TableDescQuads descQ = new TableDescQuads() ;
    public static TableDescTriples descT = new TableDescTriples() ;
    

    
    protected void formatTableTriples() { formatTupleTable(descT) ; }
    protected void formatTableQuads()   { formatTupleTable(descQ) ; }
    
    abstract protected void formatTupleTable(TableDesc tableDesc) ;
    
    abstract protected void formatTableNodes() ;
    abstract protected void formatTablePrefixes() ;
    
    abstract protected void addIndexesTableTriples() ;
    abstract protected void addIndexesTableQuads() ;
    
    abstract protected void dropIndexesTableTriples() ;
    abstract protected void dropIndexesTableQuads() ;
    
    abstract protected void truncateTableTriples() ;
    abstract protected void truncateTableQuads() ;
    abstract protected void truncateTableNodes() ;
    abstract protected void truncateTablePrefixes() ;
    
    protected void dropTable(String tableName)
    {
        TableUtils.dropTable(connection(), tableName) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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