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

package org.apache.jena.tdb2.store;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Transactions and store connections - extended tests assuming the
 * basics work. Hence these tests use memory databases.
 *
 * For tests of StoreConnection basics:
 * @see AbstractTestStoreConnectionBasics
 * @see TestStoreConnectionDirect
 * @see TestStoreConnectionMapped
 * @see TestStoreConnectionMem
 */
public class TestTransactions
{
    // Per-test unique-ish.
    static int count = 0;
    long x = System.currentTimeMillis()+(count++);

    String ns = "http://example/TestTransactions#";
    String data1 = StrUtils.strjoinNL
        ("prefix : <"+ns+">"
        ,":s :p '000-"+x+"' ."
        ,":s :p '111-"+x+"' ."
        ," :g {"
        ,"    :s :p '222-"+x+"' ."
        ,"    :s :p '333-"+x+"' ."
        ,"    :s :p '444-"+x+"' ."
        ,"}"
        );
    String data2 = StrUtils.strjoinNL
        ("prefix : <"+ns+">"
        ,":s :p 'AAA-"+x+"' ."
        ,":s :p 'BBB-"+x+"' ."
        ,":s :p 'CCC-"+x+"' ."
        ,":s :p 'DDD-"+x+"' ."
        );

    Dataset dataset;
    Location location;

    @Before public void before() {
        location = Location.mem();
        dataset = TDB2Factory.connectDataset(location);
    }

    @After public void after() {
        dataset.close();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    // Models across transactions
    @Test public void trans_01() {
        Model named = dataset.getNamedModel(ns+"g");
        Txn.executeWrite(dataset, ()->{
            RDFDataMgr.read(dataset, new StringReader(data1), null, Lang.TRIG);
        });

        Txn.executeRead(dataset, ()->{
            long x1 = Iter.count(dataset.getDefaultModel().listStatements());
            assertEquals(2, x1);
            long x2 = Iter.count(named.listStatements());
            assertEquals(3, x2);
        });

    }

    @Test public void trans_02() {
        Model model = dataset.getDefaultModel();
        Txn.executeWrite(dataset, ()->{
            RDFDataMgr.read(model, new StringReader(data2), null, Lang.TURTLE);
        });
        Txn.executeRead(dataset, ()->{
            assertEquals(4, model.size());
        });
    }

    // Iterators and trasnaxction scope.

    private void load(String data) {
        Txn.executeWrite(dataset, ()->{
            RDFDataMgr.read(dataset, new StringReader(data), null, Lang.TURTLE);
        });
    }

    public void iterator_01() {
        load(data2);

        dataset.begin(TxnType.READ);
        Iterator<Quad> iter = TDBInternal.getDatasetGraphTDB(dataset).find();
        Iter.consume(iter);
        dataset.end();
    }

    @Test(expected=TransactionException.class)
    public void iterator_02() {
        load(data2);

        dataset.begin(TxnType.READ);
        Iterator<Quad> iter = dataset.asDatasetGraph().find();
        dataset.end();
        Quad q = iter.next();
        System.err.println("iterator_02: Unexpected: "+q);
    }

    @Test(expected=TransactionException.class)
    public void iterator_03() {
        load(data2);

        dataset.begin(TxnType.READ);
        Iterator<Quad> iter = TDBInternal.getDatasetGraphTDB(dataset).find();
        dataset.end();
        Quad q = iter.next();
        System.err.println("iterator_03: Unexpected: "+q);
    }

    @Test(expected=TransactionException.class)
    public void iterator_04() {
        load(data2);
        Iterator<Statement> iter = Txn.calculateRead(dataset, ()->dataset.getDefaultModel().listStatements());
        Statement q = iter.next();
        System.err.println("iterator_04: Unexpected: "+q);
    }

    @Test(expected=TransactionException.class)
    public void iterator_05() {
        load(data2);
        Iterator<Statement> iter = Txn.calculateWrite(dataset, ()->dataset.getDefaultModel().listStatements());
        iter.next();
    }

    @Test(expected=TransactionException.class)
    public void iterator_06() {
        load(data2);

        Iterator<Quad> iter = Txn.calculateRead(dataset, ()->dataset.asDatasetGraph().find());

        dataset.begin(TxnType.READ);
        iter.next();
        dataset.end();
    }
}


