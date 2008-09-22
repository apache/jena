/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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
