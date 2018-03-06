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

package org.apache.jena.sparql.transaction;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Creator ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphSink;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.junit.Assert ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

/** "supports" for various DatasetGraph implementations */
@RunWith(Parameterized.class)
public class TestTransactionSupport {
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        x.add(new Object[] {"createTxnMem", 
            (Creator<DatasetGraph>)()->DatasetGraphFactory.createTxnMem(),
            true, true}) ;
        x.add(new Object[] {"createGeneral",
            (Creator<DatasetGraph>)()->DatasetGraphFactory.createGeneral(),
            true, false}) ;
        x.add(new Object[] {"create",
            (Creator<DatasetGraph>)()->DatasetGraphFactory.create(),
            true, false}) ;
        x.add(new Object[] {"wrap(Graph)" ,
            (Creator<DatasetGraph>)()->DatasetGraphFactory.wrap(GraphFactory.createDefaultGraph()),
            true, false}) ;
        x.add(new Object[] {"zero" ,
            (Creator<DatasetGraph>)()->new DatasetGraphZero(),
            true, true}) ;
        x.add(new Object[] {"sink" ,
            (Creator<DatasetGraph>)()->new DatasetGraphSink(),
            true, true}) ;
        x.add(new Object[] {"create(Graph)",
            (Creator<DatasetGraph>)()->DatasetGraphFactory.create(GraphFactory.createDefaultGraph()),
            true, false}) ;
        return x ;
    }

    private final Creator<DatasetGraph> maker;
    private final boolean supportsTxn;
    private final boolean supportsAbort;
    
    public TestTransactionSupport(String name, Creator<DatasetGraph> maker, boolean supportsTxn, boolean supportsAbort) {
        this.maker = maker ;
        this.supportsTxn = supportsTxn ;
        this.supportsAbort = supportsAbort ;
    }
    
    @Test public void txn_support() {
        DatasetGraph dsg = maker.create() ;
        test(dsg, supportsTxn, supportsAbort) ;
    }

    private static void test(DatasetGraph dsg, boolean supportsTxn, boolean supportsAbort) {
        Assert.assertEquals("supports",         supportsTxn,    dsg.supportsTransactions()) ;
        Assert.assertEquals("supportsAbort",    supportsAbort,  dsg.supportsTransactionAbort()) ;
    }
}
