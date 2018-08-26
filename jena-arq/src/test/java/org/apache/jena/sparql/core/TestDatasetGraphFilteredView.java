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

package org.apache.jena.sparql.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for API access via DatasetGraphFiltered
 */
@RunWith(Parameterized.class)
public class TestDatasetGraphFilteredView {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        Creator<DatasetGraph> c1 = DatasetGraphFactory::create;
        Object[] obj1 = { "General", c1 };
        Creator<DatasetGraph> c2 = DatasetGraphFactory::createTxnMem;
        Object[] obj2 = { "TIM", c2 };
        return Arrays.asList(obj1, obj2);
    }

    final DatasetGraph basedsg;
    
    private static String dataStr = StrUtils.strjoinNL
        ("PREFIX : <http://test/>"
            ,""
            ,":s0 :p 0 ."
            ,":g1 { :s1 :p 1 }"
            ,":g2 { :s2 :p 2 , '02' }"
            ,":g3 { :s3 :p 3 , '03' , '003' }"
            ,":g4 { :s4 :p 4 , '04' , '004', '0004' }"
            );

    public static Node s0 = SSE.parseNode("<http://test/s0>"); 
    public static Node s1 = SSE.parseNode("<http://test/s1>"); 
    public static Node s2 = SSE.parseNode("<http://test/s2>"); 
    public static Node s3 = SSE.parseNode("<http://test/s3>"); 
    public static Node s4 = SSE.parseNode("<http://test/s4>"); 

    public static Node g1 = SSE.parseNode("<http://test/g1>"); 
    public static Node g2 = SSE.parseNode("<http://test/g2>"); 
    public static Node g3 = SSE.parseNode("<http://test/g3>"); 
    public static Node g4 = SSE.parseNode("<http://test/g4>"); 

    public static void addTestData(DatasetGraph dsg) {
        Txn.executeWrite(dsg, ()->{
            RDFParser.create().fromString(dataStr).lang(Lang.TRIG).parse(dsg);
        });
    }
    
    public TestDatasetGraphFilteredView(String name, Creator<DatasetGraph> source) {
        basedsg = source.create();
        addTestData(basedsg);
    }
    
    @Test public void filtered1() {
        Predicate<Quad> filter = x->true;
        Txn.executeRead(basedsg, ()->{
            DatasetGraph dsg = new DatasetGraphFilteredView(basedsg, filter, Iter.toList(basedsg.listGraphNodes()));
            assertSame(basedsg, dsg);
        });
    }

    @Test public void filtered2() {
        Predicate<Quad> filter = x->x.getGraph().equals(g2);
        Txn.executeRead(basedsg, ()->{
            DatasetGraph dsg = new DatasetGraphFilteredView(basedsg, filter, Collections.singleton(g1));
            long x0 = Iter.count(dsg.find(null, null, null, null));
            assertEquals(2,x0);
            long x1 = Iter.count(dsg.find(g2, null, null, null));
            assertEquals(2,x1);
            long x2 = Iter.count(dsg.find(null, s2, null, null));
            assertEquals(2,x2);
            long x3 = Iter.count(dsg.find(g1, null, null, null));
            assertEquals(0,x3);
            assertEquals(1, dsg.size());
        });
    }

    @Test public void filtered3() {
        Predicate<Quad> filter = x->x.getSubject().equals(s2);
        Txn.executeRead(basedsg, ()->{
            DatasetGraph dsg = new DatasetGraphFilteredView(basedsg, filter, Collections.singleton(g1));
            long x0 = Iter.count(dsg.find(null, null, null, null));
            assertEquals(2,x0);
            long x1 = Iter.count(dsg.find(g2, null, null, null));
            assertEquals(2,x1);
            long x2 = Iter.count(dsg.find(null, s2, null, null));
            assertEquals(2,x2);
            long x3 = Iter.count(dsg.find(g1, s2, null, null));
            assertEquals(0,x3);
            assertEquals(1, dsg.size());
        });
    }
    
    @Test public void filtered4() {
        Predicate<Quad> filter = x->x.getSubject().equals(s2);
        Txn.executeRead(basedsg, ()->{
            DatasetGraph dsg = new DatasetGraphFilteredView(basedsg, filter, Arrays.asList(g1, g2));
            long x0 = Iter.count(dsg.find(null, null, null, null));
            assertEquals(2,x0);
            long x1 = Iter.count(dsg.find(g2, null, null, null));
            assertEquals(2,x1);
            long x2 = Iter.count(dsg.find(null, s2, null, null));
            assertEquals(2,x2);
            long x3 = Iter.count(dsg.find(g1, s2, null, null));
            assertEquals(0,x3);
            assertEquals(2, dsg.size());
        });
    }
    
    @Test public void filtered5() {
        Predicate<Quad> filter = x-> x.getSubject().equals(s2) || x.getSubject().equals(s1);  
        Txn.executeRead(basedsg, ()->{
            DatasetGraph dsg = new DatasetGraphFilteredView(basedsg, filter, Arrays.asList(g1, g2));
            long x0 = Iter.count(dsg.find(null, null, null, null));
            assertEquals(3,x0);
            long x1 = Iter.count(dsg.find(g2, null, null, null));
            assertEquals(2,x1);
        });
    }

    private void assertSame(DatasetGraph dsg1, DatasetGraph dsg2) {
        Set<Quad> quads1 = Iter.toSet(dsg1.find());
        Set<Quad> quads2 = Iter.toSet(dsg2.find());
        assertEquals(quads1, quads2);
    }
    
}
