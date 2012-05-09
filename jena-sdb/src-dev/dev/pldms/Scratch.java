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

package dev.pldms;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.store.TableDesc;
import java.util.Collection;
import java.util.HashSet;

public class Scratch {

    /**
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        Store store = StoreFactory.create("Store/sdb-hsqldb-mem.ttl");
        System.err.println("Formatted? " + formatted(store));
        store.getTableFormatter().create();
        System.err.println("Formatted? " + formatted(store));
    }

    public static boolean formatted(Store store) throws SQLException {
        return checkNodes(store) && checkTuples(store);
    }

    private static boolean checkNodes(Store store) throws SQLException {
        Connection conn = store.getConnection().getSqlConnection();
        TableDescNodes nodeDesc = store.getNodeTableDesc();
        if (nodeDesc == null) {
            return true; // vacuous
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
            return true; // vacuous
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
        //System.err.println("Trying: " + tableName);
        ResultSet res = null;
        try {
            res = conn.getMetaData().getColumns(null, null, tableName, null);
            while (res.next()) {
                String colName = res.getString("COLUMN_NAME");
                //System.err.println("Looking at :" + colName + " " + colNames.contains(colName.toLowerCase()));
                colNames.remove(colName.toLowerCase());
            }
            //System.err.print("[");
            //for (String col: colNames) System.err.print(col + ",");
            //System.err.println("]");
            return colNames.isEmpty();
        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
}
