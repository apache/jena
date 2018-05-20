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

package org.apache.jena.tdb2.loader ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.loader.DataLoader;
import org.apache.jena.tdb2.loader.LoaderFactory;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestLoader {
    static { JenaSystem.init(); }
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        BiFunction<DatasetGraph, Node, DataLoader> basic = (dsg, gn)->LoaderFactory.basicLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> sequential = (dsg, gn)->LoaderFactory.sequentialLoader(dsg, gn, output);
        BiFunction<DatasetGraph, Node, DataLoader> parallel = (dsg, gn)->LoaderFactory.parallelLoader(dsg, gn, output);
        x.add(new Object[]{"Basic loader", basic}) ;
        x.add(new Object[]{"Sequential loader", sequential}) ;
        x.add(new Object[]{"Parallel loader", parallel}) ;
        return x ; 
    }
    
    static MonitorOutput output = LoaderOps.nullOutput();
    private String name;
    private BiFunction<DatasetGraph, Node, DataLoader> maker;
    
    public TestLoader(String name, BiFunction<DatasetGraph, Node, DataLoader> maker) {
        this.name = name;
        this.maker = maker;
    }
    
    /** Load the contents of files or remote web data into a dataset. */
    static void load(DatasetGraph dataset, BiFunction<DatasetGraph, Node, DataLoader> maker, String ... dataURLs) {
        load(dataset, null, maker, dataURLs);
    }
    
    static void load(DatasetGraph dataset, Node graphName, BiFunction<DatasetGraph, Node, DataLoader> maker, String ... dataURLs) {
        DataLoader loader = maker.apply(dataset, graphName);
        loader.startBulk();
        try {
            loader.load(dataURLs);
            loader.finishBulk();
        }
        catch (RuntimeException ex) {
            loader.finishException(ex);
            throw ex;
        }
    }
    
    private static String DIR = "testing/Loader/";
    private static final Node g  = NodeFactory.createURI("g") ;
    private static final Node s  = NodeFactory.createURI("s") ;
    private static final Node p  = NodeFactory.createURI("p") ;
    private static final Node o  = NodeFactory.createURI("o") ;
    private static final Node o1 = NodeFactory.createURI("o1") ;
    private static final Node o2 = NodeFactory.createURI("o2") ;
    
    private static final Node gn  = NodeFactory.createURI("http://example/g") ;

    @BeforeClass
    static public void beforeClass() {
        LogCtl.disable(ARQ.logExecName) ;
        //LogCtl.disable(TDB2.logLoaderName) ;
    }

    @AfterClass
    static public void afterClass() {
        LogCtl.enable(ARQ.logExecName) ;
        LogCtl.enable(TDB2.logLoaderName) ;
    }

    static DatasetGraph fresh() {
        return DatabaseMgr.createDatasetGraph() ;
    }

    @Test
    public void load_dataset_01() {
        DatasetGraph dsg = fresh() ;
        load(dsg, maker, DIR + "data-1.nq") ;
        Txn.executeRead(dsg, ()->{
            assertTrue(dsg.getDefaultGraph().isEmpty()) ;
            assertEquals(1, dsg.getGraph(g).size()) ;
        });
    }

    @Test
    public void load_dataset_02() {
        DatasetGraph dsg = fresh() ;
        load(dsg, maker , DIR + "data-1.nq", DIR + "data-2.nt") ;
        Txn.executeRead(dsg, ()->{
            assertEquals(1, dsg.getGraph(g).size()) ;
            assertEquals(2, dsg.getDefaultGraph().size());
        });
    }

    @Test
    public void load_dataset_03() {
        DatasetGraph dsg = fresh();
        DataLoader loader = maker.apply(dsg, null);
        loader.startBulk();
        RDFDataMgr.parse(loader.stream(), DIR + "data-1.nq");
        loader.finishBulk();
        Txn.executeRead(dsg, ()->{
            assertTrue(dsg.getDefaultGraph().isEmpty()) ;
            assertEquals(1, dsg.getGraph(g).size()) ;
        });
    }

    @Test
    public void load_dataset_04() {
        DatasetGraph dsg = fresh() ;
        load(dsg, maker, DIR + "data-3.trig") ;
        Txn.executeRead(dsg, ()->{
            String uri = dsg.getDefaultGraph().getPrefixMapping().getNsPrefixURI("") ;
            assertEquals("http://example/", uri) ;
        });
    }
    
    @Test public void isomorphic_1() {
        DatasetGraph dsg = fresh() ;
        load(dsg, maker , DIR + "data-1.nq", DIR + "data-2.nt") ;
        Txn.executeRead(dsg, ()->{
            assertEquals(1, dsg.getGraph(g).size()) ;
            assertEquals(2, dsg.getDefaultGraph().size());
        });

        DatasetGraph dsg1 = RDFDataMgr.loadDatasetGraph(DIR + "data-1.nq");
        RDFDataMgr.read(dsg1, DIR + "data-2.nt");
        Txn.executeRead(dsg, ()->{
            boolean b = IsoMatcher.isomorphic(dsg1, dsg);
            assertTrue("Not isomorphic", b);
        });
    }

    @Test
    public void load_dataset_indexes() {
        DatasetGraph dsg = fresh() ;
        load(dsg, maker, DIR + "data-1.nq", DIR + "data-2.nt") ;
        Txn.executeRead(dsg, ()->{
            assertEquals(2, dsg.getDefaultGraph().size()) ;
            assertEquals(1, dsg.getGraph(g).size()) ;
            
            {
                // Check indexes.
                Graph g1 = dsg.getDefaultGraph();
                List<Triple> x = Iter.toList(g1.find(null, null, null)) ;
                assertEquals(2, x.size()) ;
                x = Iter.toList(g1.find(s, null, null)) ;
                assertEquals(2, x.size()) ;
                x = Iter.toList(g1.find(null, p, null)) ;
                assertEquals(2, x.size()) ;
                x = Iter.toList(g1.find(null, null, o1)) ;
                assertEquals(1, x.size()) ;
            }

            {
                Graph g2 = dsg.getGraph(g);
                List<Triple> x = Iter.toList(g2.find(null, null, null));
                assertEquals(1, x.size());
                x = Iter.toList(g2.find(s, null, null));
                assertEquals(1, x.size());
                x = Iter.toList(g2.find(null, p, null));
                assertEquals(1, x.size());
                x = Iter.toList(g2.find(null, null, o));
                assertEquals(1, x.size());
            }
            
            {
                List<Quad> z = Iter.toList(dsg.find(null, null, null, null)) ;
                assertEquals(3, z.size()) ;
                z = Iter.toList(dsg.find(Quad.defaultGraphIRI, null, null, null)) ;
                assertEquals(2, z.size()) ;
                z = Iter.toList(dsg.find(g, null, null, null)) ;
                assertEquals(1, z.size()) ;
                z = Iter.toList(dsg.find(null, s, null, null)) ;
                assertEquals(3, z.size()) ;
                z = Iter.toList(dsg.find(null, null, p, null)) ;
                assertEquals(3, z.size()) ;
                z = Iter.toList(dsg.find(g, null, p, null)) ;
                assertEquals(1, z.size()) ;
                z = Iter.toList(dsg.find(null, null, null, o)) ;
                assertEquals(1, z.size()) ;
            }
        });
    }
    
    @Test
    public void load_graph_1() {
        DatasetGraph dsg = fresh() ;
        load(dsg, gn, maker, DIR + "data-2.nt") ;
        Txn.executeRead(dsg, ()->{
            assertEquals(0, dsg.getDefaultGraph().size());
            assertEquals(2, dsg.getGraph(gn).size());
        });
    }
    
    // Try to load quads.
}
