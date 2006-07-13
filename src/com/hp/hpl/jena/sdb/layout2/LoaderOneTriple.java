/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import static com.hp.hpl.jena.sdb.util.StrUtils.strjoinNL;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.*;
import com.hp.hpl.jena.sdb.store.StoreLoader;

public class LoaderOneTriple
    extends SDBConnectionHolder 
    implements StoreLoader
{
    // Not used - a loader that does one triple directly.
    private static Log log = LogFactory.getLog(LoaderOneTriple.class);
    private static final String classShortName = Utils.classShortName(LoaderOneTriple.class)  ;
    
    public LoaderOneTriple(SDBConnection connection) { super(connection) ; }
    
    public void close() {}
    
    public void startBulkLoad() {}

    public void addTriple(Triple triple) { addTriple(connection(), triple) ; }
    
    public static void addTriple(SDBConnection connection, Triple triple)
    { 
        try {
            int sId = getIndex(connection, triple.getSubject(),    true) ;
            int pId = getIndex(connection, triple.getPredicate(),  true) ;
            int oId = getIndex(connection, triple.getObject(),     true) ;
            
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
                ResultSet rs = connection.execQuery(sqlStmtCheck) ;
                rs.next() ;
                int count = rs.getInt(1) ;
                RS.close(rs) ;
                
                if ( count > 1 )
                    log.warn("Duplicate triple deteched: count="+count+" :: "+triple) ;
                
                if ( count > 0 )
                    return ;
                String sqlStmtIns = "INSERT INTO "+TableTriples.tableName+" VALUES("+sId+", "+pId+", "+oId+")" ;
                connection.execUpdate(sqlStmtIns) ;
            }
            else
            {
                // MySQL :: IGNORE
                String sqlStmtIns = "INSERT IGNORE INTO "+TableTriples.tableName+" VALUES("+sId+", "+pId+", "+oId+")" ;
                connection.execUpdate(sqlStmtIns) ;
            }
                
        }
        catch (SQLException ex) { throw new SDBExceptionSQL("SQLException: "+ex.getMessage(), ex) ; }
    }

    public void deleteTriple(Triple triple) { deleteTriple(connection(), triple) ; }
    
    public static void deleteTriple(SDBConnection connection, Triple triple)
    { 
        try {
            String sqlStmtIns = null ;
            try { 
                int sId = getIndex(connection, triple.getSubject(),    false) ;
                int pId = getIndex(connection, triple.getPredicate(),  false) ;
                int oId = getIndex(connection, triple.getObject(),     false) ;
                sqlStmtIns = "DELETE FROM "+TableTriples.tableName+" WHERE "+
                             TableTriples.tableName+".s="+sId+" AND "+
                             TableTriples.tableName+".p="+pId+" AND "+
                             TableTriples.tableName+".o="+oId ;
            } catch (SDBException ex)
            { return ; }
            connection.execUpdate(sqlStmtIns) ;
        }
        catch (SQLException ex) { throw new SDBExceptionSQL("SQLException: "+ex.getMessage(), ex) ; }
    }


    public void flushTriples() {}

    public void finishBulkLoad() {}

    public void setChunkSize(int chunks) {}

    public int getChunkSize() { return 1 ; }

    public void setUseThreading(boolean useThreading)
    { throw new UnsupportedOperationException("LoaderOneTriple.setUseThreading") ; }    {}

    public boolean getUseThreading()
    { return false ; }
    
    // ----
    
    // ---- Used by deleteTriple
    // To share with LoaderTriplesNodes
    
    private static int getIndex(SDBConnection conn, Node node, boolean create) throws SQLException
    {
        try {
            long hash = NodeLayout2.hash(node) ;
            String lex  = NodeLayout2.nodeToLex(node) ;
            String hashStr = Long.toString(hash) ;
            
            String sqlStmt = strjoinNL(
                "SELECT id FROM Nodes WHERE hash = "+hashStr
                ) ;
            ResultSet rs = conn.execQuery(sqlStmt) ;
            try {
                if ( ! rs.next() )
                {
                    if ( ! create )
                        throw new SDBException("No such node in table: "+node) ;
                    insertNode(conn, lex, node) ;
                    // And get it again to find the auto-allocate ID.
                    return getIndex(conn, node, false) ;
                }
        
                int id = rs.getInt("id") ;
                if ( rs.next() )
                    log.warn("More than one hit for : "+sqlStmt+" (ignored)") ;
                return id ;    
            } finally { RS.close(rs) ; }
        } catch (SQLException ex)
        {
            log.warn("SQLException: "+ex.getMessage()) ;
            throw ex ;
        }
    }
    
    private static void insertNode(SDBConnection conn, String lex,  Node node) throws SQLException
    {
        ValueType vType = ValueType.lookup(node) ;
        int typeId  = NodeLayout2.nodeToType(node) ;
        
        String lang = "" ;
        String datatype = "" ;
        
        if ( node.isLiteral() )
        {
            lang = node.getLiteralLanguage() ;
            datatype = node.getLiteralDatatypeURI() ;
            if ( datatype == null )
                datatype = "" ;
        }
        // Value of the node
        int valInt = 0 ;
        if ( vType == ValueType.INTEGER )
            valInt = Integer.parseInt(lex) ;
        
        double valDouble = 0 ;
        if ( vType == ValueType.DOUBLE )
            valDouble = Double.parseDouble(lex) ;

        String valDateTime = "0000-01-01 00:00:00" ;
        if ( vType == ValueType.DATETIME )
            valDateTime = SQLUtils.toSQLdatetimeString(lex) ;
        
        valDateTime = SQLUtils.quote(valDateTime) ;
        
        long hash = NodeLayout2.hash(lex,lang,datatype,typeId);
        
        String sqlStmt = strjoinNL(
                "INSERT INTO "+TableNodes.tableName+"(hash,lex,lang,datatype,type,vInt,vDouble,vDateTime) VALUES",
                "  ("+hash+", ",
                "   "+SQLUtils.quote(lex)+", ",
                "   "+SQLUtils.quote(lang)+", ",
                "   "+SQLUtils.quote(datatype)+", ",
                "   "+typeId+", ",
                "   "+valInt+", "+valDouble+", "+valDateTime+")") ;
        conn.execUpdate(sqlStmt) ;
    }
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