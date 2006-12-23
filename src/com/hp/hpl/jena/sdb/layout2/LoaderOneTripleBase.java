/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.query.util.Utils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.StoreLoader;

/** A loader that works one triple at a time but is portable.
 * Two subvariants for hash and index based triple tables.
 * @author Andy Seaborne
 * @version $Id$
 */ 

public abstract class LoaderOneTripleBase
    extends SDBConnectionHolder 
    implements StoreLoader
{
    private static Log log = LogFactory.getLog(LoaderOneTripleBase.class);
    private static final String classShortName = Utils.classShortName(LoaderOneTripleBase.class)  ;
    
    public LoaderOneTripleBase(SDBConnection connection) { super(connection) ; }
    
    public void close() {}
    
    public void startBulkUpdate() {}

//    public void addTriple(Triple triple) { addTriple(connection(), triple) ; }
//    
//    public static void addTriple(SDBConnection connection, Triple triple)
//    {
    
    public void addTriple(Triple triple)
    {
        try {
            long sId = insertNode(triple.getSubject()) ;
            long pId = insertNode(triple.getPredicate()) ;
            long oId = insertNode(triple.getObject()) ;
            
            if ( true )
            {
                // Generic SQL
                String sqlStmtCheck =
                    "SELECT count(*) FROM Triples WHERE\n"+
                    "Triples.s = "+sId+"\n"+
                    "AND\n"+
                    "Triples.p = "+pId+"\n"+
                    "AND\n"+
                    "Triples.o = "+oId ;
                ResultSet rs = connection().execQuery(sqlStmtCheck) ;
                rs.next() ;
                int count = rs.getInt(1) ;
                RS.close(rs) ;
                
                if ( count > 1 )
                    log.warn("Duplicate triple deteched: count="+count+" :: "+triple) ;
                
                if ( count > 0 )
                    return ;
                String sqlStmtIns = "INSERT INTO "+TableTriples.tableName+" VALUES("+sId+", "+pId+", "+oId+")" ;
                connection().execUpdate(sqlStmtIns) ;
            }
            else
            {
                // MySQL :: IGNORE
                String sqlStmtIns = "INSERT IGNORE INTO "+TableTriples.tableName+" VALUES("+sId+", "+pId+", "+oId+")" ;
                connection().execUpdate(sqlStmtIns) ;
            }
                
        }
        catch (SQLException ex) { throw new SDBExceptionSQL("SQLException: "+ex.getMessage(), ex) ; }
    }

//    public void deleteTriple(Triple triple) { deleteTriple(connection(), triple) ; }
//    
//    public static void deleteTriple(SDBConnection connection, Triple triple)
    
    public void deleteTriple(Triple triple)
    { 
        try {
            String sqlStmtIns = null ;
            try { 
                long sId = getRefForNode(triple.getSubject()) ;
                long pId = getRefForNode(triple.getPredicate()) ;
                long oId = getRefForNode(triple.getObject()) ;
                
                sqlStmtIns = "DELETE FROM "+TableTriples.tableName+" WHERE "+
                             TableTriples.tableName+".s="+sId+" AND "+
                             TableTriples.tableName+".p="+pId+" AND "+
                             TableTriples.tableName+".o="+oId ;
            } catch (SDBException ex)
            { return ; }
            connection().execUpdate(sqlStmtIns) ;
        }
        catch (SQLException ex) { throw new SDBExceptionSQL("SQLException: "+ex.getMessage(), ex) ; }
    }

    public void finishBulkUpdate() {}

    public void setChunkSize(int chunks) {}

    public int getChunkSize() { return 1 ; }

    public void setUseThreading(boolean useThreading)
    { throw new UnsupportedOperationException("LoaderOneTriple.setUseThreading") ; }

    public boolean getUseThreading()
    { return false ; }
    
    // ----
    
//    public static int getIndex(SDBConnection conn, Node node) throws SQLException
//    {
//        return getIndex(conn, node, false) ;
//    }
    
    // Abstraction:
    //   long getRefForNode(SDBConnection conn, Node node)
    //   long insertNode(SDBConnection conn, Node node)
    
    abstract protected long getRefForNode(Node node) throws SQLException ;
    abstract protected long insertNode(Node node) throws SQLException ;
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