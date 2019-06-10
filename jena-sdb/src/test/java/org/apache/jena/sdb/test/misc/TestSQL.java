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

package org.apache.jena.sdb.test.misc;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sdb.SDB;
import org.apache.jena.sdb.Store;
import org.apache.jena.sdb.StoreDesc;
import org.apache.jena.sdb.compiler.OpSQL;
import org.apache.jena.sdb.compiler.SDB_QC;
import org.apache.jena.sdb.core.SDBRequest;
import org.apache.jena.sdb.engine.QueryEngineSDB;
import org.apache.jena.sdb.store.DatabaseType;
import org.apache.jena.sdb.store.DatasetGraphSDB;
import org.apache.jena.sdb.store.LayoutType;
import org.apache.jena.sdb.store.StoreFactory;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;

public class TestSQL {

    @Test
    public void testRewriteDistinctAsGroup() {
        Query  query = QueryFactory.create(
                "PREFIX  :     <http://example/>\n" +
                "PREFIX  arq:  <urn:x-arq:>\n" +
                "\n" +
                "SELECT  *\n" +
                "WHERE\n" +
                "  { GRAPH arq:UnionGraph\n" +
                "      { ?s  ?p  ?o }\n" +
                "  }\n");

        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
        Store store = StoreFactory.create(storeDesc);
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;
        String sql = SDB_QC.toSqlString((OpSQL)qe.getOp(), new SDBRequest(store, query));

        // Ensure the SQL contains an GROUP BY clause
        Assert.assertTrue(sql.contains("GROUP BY"));
    }

    @Test
    public void testRewriteOrder() {
        Query  query = QueryFactory.create(
                "PREFIX  :     <http://example/>\n" +
                        "PREFIX  arq:  <urn:x-arq:>\n" +
                        "\n" +
                        "SELECT  *\n" +
                        "WHERE\n" +
                        "  { GRAPH arq:UnionGraph\n" +
                        "      { ?s  ?p  ?o }\n" +
                        "  }\n" +
                        "ORDER BY ?s\n" +
                        "LIMIT 10\n"
                );

        StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
        Store store = StoreFactory.create(storeDesc);
        Context context = SDB.getContext().copy();

        // Turn on ORDER BY rewriting
        context.set(SDB.optimizeOrderClause, true);

        QueryEngineSDB qe = new QueryEngineSDB(new DatasetGraphSDB(store, context), query, BindingRoot.create(), context);

        String sql = SDB_QC.toSqlString((OpSQL)qe.getOp(), new SDBRequest(store, query));

        // Ensure the SQL contains an ORDER BY clause
        Assert.assertTrue(sql.contains("ORDER BY"));
    }
}
