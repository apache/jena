/*
 * Copyright (C) Martin HEIN (m#)/March 2008
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;

import com.hp.hpl.jena.util.FileManager;

public class StoreUtils
{
    
    public static boolean isHSQL(Store store)
    { 
        return store.getDatabaseType().equals(DatabaseType.HSQLDB) ;
    }
    
    public static boolean isH2(Store store)
    { 
        return store.getDatabaseType().equals(DatabaseType.H2) ;
    }
    
    public static boolean isDerby(Store store)
    { 
        return store.getDatabaseType().equals(DatabaseType.Derby) ;
    }
    
    
    public static boolean isPostgreSQL(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.PostgreSQL) ;
    }

    public static boolean isMySQL(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.MySQL) ;
    }
    
    public static boolean isSQLServer(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.SQLServer) ;
    }
    
    public static boolean isOracle(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.Oracle) ;
    }
    
    public static boolean isDB2(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.DB2) ;
    }

    public static void load(Store store, String filename)
    {
        Model model = SDBFactory.connectDefaultModel(store) ;
        FileManager.get().readModel(model, filename) ;
    }

    public static void load(Store store, String graphIRI, String filename)
    {
        Model model = SDBFactory.connectNamedModel(store, graphIRI) ;
        FileManager.get().readModel(model, filename) ;
    }
    
    public static Iterator<Node> storeGraphNames(Store store)
    {
        List<Node> x = new ArrayList<Node>() ;
        String qs = "SELECT ?g { GRAPH ?g { }}" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, SDBFactory.connectDataset(store)) ;
        ResultSet rs = qExec.execSelect() ;
        Var var_g = Var.alloc("g") ;
        while(rs.hasNext())
        {
            Node n = rs.nextBinding().get(var_g) ;
            x.add(n) ;
        }
        return x.iterator() ;
    }
    
    public static boolean containsGraph(Store store, Node graphNode)
    {
        String qs = "SELECT * { GRAPH "+FmtUtils.stringForNode(graphNode)+" { ?s ?p ?o }} LIMIT 1" ;
        Dataset ds = SDBFactory.connectDataset(store) ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        boolean b = rs.hasNext() ;
        qExec.close();
        return b ;
    }

    /**
     * Best effort utility to check whether store is formatted
     * (currently: tables and columns exist)
     * @param store The store to check
     */
    public static boolean isFormatted(Store store) throws SQLException {
        return checkNodes(store) && checkTuples(store);
    }

    private static boolean checkNodes(Store store) throws SQLException {
        Connection conn = store.getConnection().getSqlConnection();
        TableDescNodes nodeDesc = store.getNodeTableDesc();
        if (nodeDesc == null) {
            return true; // vacuously
        }
        return hasTableAndColumns(conn,
                nodeDesc.getTableName(),
                nodeDesc.getIdColName(),
                nodeDesc.getHashColName(),
                nodeDesc.getLexColName(),
                nodeDesc.getLangColName(),
                nodeDesc.getTypeColName());
    }

    private static boolean checkTuples(Store store) throws SQLException {
        Connection conn = store.getConnection().getSqlConnection();
        return isTupleTableFormatted(conn, store.getTripleTableDesc()) &&
                isTupleTableFormatted(conn, store.getQuadTableDesc());
    }

    private static boolean isTupleTableFormatted(Connection conn, TableDesc desc) throws SQLException {
        if (desc == null) {
            return true; // vacuously
        }
        return hasTableAndColumns(conn,
                desc.getTableName(),
                desc.getColNames().toArray(new String[]{}));
    }

    private static boolean hasTableAndColumns(Connection conn, String tableName, String... colNames) throws SQLException {
        Collection<String> cols = new HashSet<String>();
        for (String c : colNames) {
            if (c != null) {
                cols.add(c.toLowerCase());
            }
        }
        return (hasColumns(conn, tableName, cols) ||
                hasColumns(conn, tableName.toLowerCase(), cols) ||
                hasColumns(conn, tableName.toUpperCase(), cols));
    }

    private static boolean hasColumns(Connection conn, String tableName, Collection<String> colNames) throws SQLException {
        java.sql.ResultSet res = null;
        try {
            res = conn.getMetaData().getColumns(null, null, tableName, null);
            while (res.next()) {
                String colName = res.getString("COLUMN_NAME");
                colNames.remove(colName.toLowerCase());
            }
            return colNames.isEmpty();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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