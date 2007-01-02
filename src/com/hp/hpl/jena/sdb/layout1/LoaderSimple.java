/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.store.StoreLoader;


public class LoaderSimple 
    extends SDBConnectionHolder
    implements StoreLoader
{
    private EncoderDecoder codec ;
    public LoaderSimple(SDBConnection connection, EncoderDecoder codec)
    {
        super(connection) ;
        this.codec = codec ;
    }

    public void startBulkUpdate() {}
    public void finishBulkUpdate() {}
    public void close() {}
    
    public void addTriple(Triple t )
    {
        // not transactional
        String sStr = "'"+codec.encode(t.getSubject())+"'" ;
        String pStr = "'"+codec.encode(t.getPredicate())+"'" ;
        String oStr = "'"+codec.encode(t.getObject())+"'" ;
        
        String sqlStmt = 
            "SELECT count(*) FROM Triples WHERE "+
            "  Triples.s="+sStr+" AND Triples.p="+pStr+" AND Triples.o="+oStr ;  
        String sqlStmtIns = "INSERT INTO Triples VALUES("+sStr+", "+pStr+", "+oStr+")" ;
        try {
            ResultSet rs = connection().execQuery(sqlStmt) ;
            rs.next() ;
            int count = rs.getInt(1) ;
            RS.close(rs) ;
            if ( count != 0  )
                return ;
            connection().execUpdate(sqlStmtIns) ;
        } catch (SQLException ex) { throw new SDBException("SQLException: "+ex.getMessage(), ex) ; }
        return ;
    }
 

    public void deleteTriple(Triple t ) 
    {
        String sStr = "'"+codec.encode(t.getSubject())+"'" ;
        String pStr = "'"+codec.encode(t.getPredicate())+"'" ;
        String oStr = "'"+codec.encode(t.getObject())+"'" ;
        
        String sqlStmt =  
           "DELETE FROM Triples WHERE "+
           "  Triples.s="+sStr+" AND Triples.p="+pStr+" AND  Triples.o="+oStr ;
        
        try {
            connection().execUpdate(sqlStmt) ;
        } catch (Exception ex)
        {
            System.err.println(ex.getMessage()) ;
            ex.printStackTrace(System.err) ;
            return ;
        }
    }

    public void setUseThreading(boolean useThreading)
    {}

    public boolean getUseThreading()
    {
        return false ;
    }

    public void setChunkSize(int chunks)
    {}

    public int getChunkSize()
    {
        return 1 ;
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