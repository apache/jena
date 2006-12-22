/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.StoreFormatter;


public abstract class FmtLayout2
    extends SDBConnectionHolder 
    implements StoreFormatter
{
    public FmtLayout2(SDBConnection connection) { super(connection) ; }
    
    public void create() { format() ; }
    
    public void format()
    { 
        formatTablePrefixes() ;
        formatTableNodes() ;
        formatTableTriples() ;
    }
    
    public void truncate()
    {
        truncateTablePrefixes() ;
        truncateTableNodes() ;
        truncateTableTriples() ;
    }
    
    abstract protected void formatTableTriples() ;
    abstract protected void formatTableNodes() ;
    abstract protected void formatTablePrefixes() ;
    
    protected void truncateTableTriples()  { truncateTable(TableTriples.tableName) ; } 
    protected void truncateTableNodes()    { truncateTable(TableNodes.tableName) ; }
    protected void truncateTablePrefixes() { truncateTable(TablePrefixes.tableName) ; }
    
    protected void truncateTable(String tableName)
    {
        try { 
            connection().exec("DELETE FROM "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException truncating table: "+tableName,ex) ; }
    }
    
    abstract protected void dropTable(String tableName) ;

//    /** Drop whether it exists or not -- better to override*/
//    protected void dropTable(String tableName)
//    {
//        try { 
//            connection.execAny("DROP TABLE "+tableName) ;
//        } catch (SQLException ex) {}
//    }
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