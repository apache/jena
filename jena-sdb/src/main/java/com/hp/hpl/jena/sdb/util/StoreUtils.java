/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
    public static boolean isSAP(Store store)
    {
        return store.getDatabaseType().equals(DatabaseType.SAP) ;
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
        try ( QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ) {
            ResultSet rs = qExec.execSelect() ;
            return rs.hasNext() ;
        }
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
        try ( java.sql.ResultSet res = conn.getMetaData().getColumns(null, null, tableName, null) ) {
            while (res.next()) {
                String colName = res.getString("COLUMN_NAME");
                colNames.remove(colName.toLowerCase());
            }
            return colNames.isEmpty();
        }
    }
}
