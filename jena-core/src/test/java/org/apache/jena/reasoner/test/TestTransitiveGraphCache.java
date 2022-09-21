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

package org.apache.jena.reasoner.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.reasoner.TriplePattern ;
import org.apache.jena.reasoner.transitiveReasoner.TransitiveGraphCache ;

/**
 * A purely temporary test suite just used during development and kept
 * off the main unit test paths.
 */

public class TestTransitiveGraphCache extends TestCase {
    
    /** The cache under test */
    TransitiveGraphCache cache;
    
    // Dummy predicates and nodes for the graph
    String NS = "urn:x-hp-test:ex/";
    Node directP = NodeFactory.createURI(NS+"directSubProperty");
    Node closedP = NodeFactory.createURI(NS+"subProperty");
    
    Node a = NodeFactory.createURI(NS+"a");
    Node b = NodeFactory.createURI(NS+"b");
    Node c = NodeFactory.createURI(NS+"c");
    Node d = NodeFactory.createURI(NS+"d");
    Node e = NodeFactory.createURI(NS+"e");
    Node f = NodeFactory.createURI(NS+"f");
    Node g = NodeFactory.createURI(NS+"g");
     
    /**
     * Boilerplate for junit
     */ 
    public TestTransitiveGraphCache( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestTransitiveGraphCache.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest( new TestTransitiveGraphCache("testEquivalencesSimple"));
//        return suite;
    }  

    /**
     * Test the basic functioning a Transitive closure cache.
     * Caches the graph but not the final closure.
     */
    public void testBasicCache() {
        initCache();
        cache.setCaching(false);
        doBasicTest(cache);
    }
    
    /**
     * Test the basic functioning a Transitive closure cache.
     * Caches the graph and any requested closures
     */
    public void testCachingCache() {
        initCache();
        cache.setCaching(true);
        doBasicTest(cache);
    }
    
    /**
     * Test the clone operation
     */
    public void testCloning() {
        initCache();
        TransitiveGraphCache clone = cache.deepCopy();
        // Mess with the original to check cloning
        cache.addRelation(Triple.create(a, closedP, d));
        cache.addRelation(Triple.create(g, closedP, a));
        doBasicTest(clone);
    }
        
    /**
     * Initialize the cache with some test data
     */
    private void initCache() {
        // Create a graph with reflexive references, cycles, redundant links
        cache = new TransitiveGraphCache(directP, closedP);        
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(b, closedP, e));
        cache.addRelation(Triple.create(b, closedP, c));
        cache.addRelation(Triple.create(e, closedP, f));
        cache.addRelation(Triple.create(c, closedP, f));
        cache.addRelation(Triple.create(f, closedP, g));
        cache.addRelation(Triple.create(d, closedP, c));
        cache.addRelation(Triple.create(d, closedP, e));
        cache.addRelation(Triple.create(d, closedP, g));  // reduntant two ways
        cache.addRelation(Triple.create(a, closedP, e));  // redundant
        cache.addRelation(Triple.create(d, closedP, b));  // Makes both earlier d's redundant
        
        cache.addRelation(Triple.create(a, closedP, a));
        cache.addRelation(Triple.create(b, closedP, b));
        cache.addRelation(Triple.create(c, closedP, c));
        cache.addRelation(Triple.create(d, closedP, d));
        cache.addRelation(Triple.create(e, closedP, e));
        cache.addRelation(Triple.create(f, closedP, f));
        cache.addRelation(Triple.create(g, closedP, g));
    }
            
    public void doBasicTest(TransitiveGraphCache cache) {
         // Test forward property patterns
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, e),
                Triple.create(a, closedP, f),
                Triple.create(a, closedP, g)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, g)),
            new Object[] {
                Triple.create(a, closedP, g),
            });
            
        // Test backward patterns
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, directP, f)),
            new Object[] {
                Triple.create(e, closedP, f),
                Triple.create(f, closedP, f),
                Triple.create(c, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, closedP, f)),
            new Object[] {
                Triple.create(f, closedP, f),
                Triple.create(e, closedP, f),
                Triple.create(b, closedP, f),
                Triple.create(c, closedP, f),
                Triple.create(a, closedP, f),
                Triple.create(d, closedP, f)
            });
        
        // List all cases
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(d, closedP, d),
                Triple.create(d, closedP, b),
                Triple.create(b, closedP, b),
                Triple.create(b, closedP, e),
                Triple.create(b, closedP, c),
                Triple.create(e, closedP, e),
                Triple.create(e, closedP, f),
                Triple.create(c, closedP, c),
                Triple.create(c, closedP, f),
                Triple.create(f, closedP, f),
                Triple.create(f, closedP, g),
                Triple.create(g, closedP, g)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(null, closedP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, e),
                Triple.create(a, closedP, f),
                Triple.create(a, closedP, g),
                Triple.create(d, closedP, d),
                Triple.create(d, closedP, b),
                Triple.create(d, closedP, e),
                Triple.create(d, closedP, c),
                Triple.create(d, closedP, f),
                Triple.create(d, closedP, g),
                Triple.create(b, closedP, b),
                Triple.create(b, closedP, e),
                Triple.create(b, closedP, c),
                Triple.create(b, closedP, f),
                Triple.create(b, closedP, g),
                Triple.create(e, closedP, e),
                Triple.create(e, closedP, f),
                Triple.create(e, closedP, g),
                Triple.create(c, closedP, c),
                Triple.create(c, closedP, f),
                Triple.create(c, closedP, g),
                Triple.create(f, closedP, f),
                Triple.create(f, closedP, g),
                Triple.create(g, closedP, g)
             });
        
        // Add a look in the graph and check the loop from each starting position
        cache.addRelation(Triple.create(g, closedP, e));
        
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(e, directP, null)),
                new Object[] {
                    Triple.create(e, closedP, e),
                    Triple.create(e, closedP, f),
                    Triple.create(e, closedP, g)
                });
            TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(f, directP, null)),
                new Object[] {
                    Triple.create(f, closedP, f),
                    Triple.create(f, closedP, g),
                    Triple.create(f, closedP, e)
                });
            TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(g, directP, null)),
                new Object[] {
                    Triple.create(g, closedP, g),
                    Triple.create(g, closedP, e),
                    Triple.create(g, closedP, f)
                });
            TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, e)),
                    new Object[] {
                        Triple.create(e, closedP, e),
                        Triple.create(f, closedP, e),
                        Triple.create(b, closedP, e),
                        Triple.create(c, closedP, e),
                        Triple.create(g, closedP, e)
                    });
                TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, f)),
                    new Object[] {
                        Triple.create(f, closedP, f),
                        Triple.create(g, closedP, f),
                        Triple.create(b, closedP, f),
                        Triple.create(c, closedP, f),
                        Triple.create(e, closedP, f)
                    });
                TestUtil.assertIteratorValues(this, 
                    cache.find(new TriplePattern(null, directP, g)),
                    new Object[] {
                        Triple.create(g, closedP, g),
                        Triple.create(e, closedP, g),
                        Triple.create(b, closedP, g),
                        Triple.create(c, closedP, g),
                        Triple.create(f, closedP, g)
                    });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(g, closedP, null)),
            new Object[] {
                Triple.create(g, closedP, g),
                Triple.create(g, closedP, e),
                Triple.create(g, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(e, closedP, null)),
            new Object[] {
                Triple.create(e, closedP, g),
                Triple.create(e, closedP, e),
                Triple.create(e, closedP, f)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(f, closedP, null)),
            new Object[] {
                Triple.create(f, closedP, g),
                Triple.create(f, closedP, e),
                Triple.create(f, closedP, f)
            });
        /*        
        System.out.println("Add e-f-g-e loop");        
        cache.printAll();
        listFind(cache, e, directP, null);
        listFind(cache, e, closedP, null);
        listFind(cache, f, directP, null);
        listFind(cache, f, closedP, null);
        listFind(cache, g, directP, null);
        listFind(cache, g, closedP, null);        
        */
    }
    
    /**
     * Test a a case where an earlier version had a bug due to removing
     * a link which was required rather than redundant.
     */
    public void testBug1() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));  
        cache.addRelation(Triple.create(c, closedP, a));        
        cache.addRelation(Triple.create(c, closedP, b));
        cache.addRelation(Triple.create(a, closedP, c));     
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
            });
           
    }
    
    /**
     * Test a case where the transitive reduction appears to 
     * be incomplete. The links just
     * form a linear chain, with all closed links provided. But inserted
     * in a particular order.
     */
    public void testBug2() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(a, closedP, c));
        cache.addRelation(Triple.create(b, closedP, c));        
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b)
            });
           
    }
        
    /**
     * Test the removeRelation functionality.
     */
    public void testRemove() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(a, closedP, c));
        cache.addRelation(Triple.create(b, closedP, d));
        cache.addRelation(Triple.create(c, closedP, d));
        cache.addRelation(Triple.create(d, closedP, e));
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, d),
                Triple.create(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                Triple.create(b, closedP, b),
                Triple.create(b, closedP, d),
                Triple.create(b, closedP, e)
            });
        cache.removeRelation(Triple.create(b, closedP, d));
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, d),
                Triple.create(a, closedP, e)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                Triple.create(b, closedP, b),
            });
        cache.removeRelation(Triple.create(a, closedP, c));
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, closedP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b)
            });
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(b, closedP, null)),
            new Object[] {
                Triple.create(b, closedP, b),
            });
    }
    
    /**
     * Test direct link case with adverse ordering.
     */
    public void testDirect() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(c, closedP, d));
        cache.addRelation(Triple.create(a, closedP, d));
        cache.addRelation(Triple.create(b, closedP, c));
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
            });
    }
    
    /**
     * Test cycle detection.
     */
    public void testCycle() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(b, closedP, c));
        cache.addRelation(Triple.create(a, closedP, c));
        cache.addRelation(Triple.create(c, closedP, b));
        TestUtil.assertIteratorValues(this, 
            cache.find(new TriplePattern(a, directP, null)),
            new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
            });
    }
    
    /**
     * A ring of three cycle
     */
    public void testCycle2() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(a, closedP, c));
        cache.addRelation(Triple.create(f, closedP, b));
        cache.addRelation(Triple.create(b, closedP, g));
        cache.addRelation(Triple.create(b, closedP, d));
        cache.addRelation(Triple.create(d, closedP, c));
        cache.addRelation(Triple.create(d, closedP, e));
        cache.addRelation(Triple.create(c, closedP, e));
        cache.addRelation(Triple.create(c, closedP, b));
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(c, directP, null)),
                new Object[] {
                    Triple.create(c, closedP, e),
                    Triple.create(c, closedP, g),
                    Triple.create(c, closedP, b),
                    Triple.create(c, closedP, d),
                    Triple.create(c, closedP, c),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(null, directP, c)),
                new Object[] {
                    Triple.create(a, closedP, c),
                    Triple.create(b, closedP, c),
                    Triple.create(d, closedP, c),
                    Triple.create(f, closedP, c),
                    Triple.create(c, closedP, c),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(f, closedP, null)),
                new Object[] {
                    Triple.create(f, closedP, f),
                    Triple.create(f, closedP, b),
                    Triple.create(f, closedP, c),
                    Triple.create(f, closedP, d),
                    Triple.create(f, closedP, g),
                    Triple.create(f, closedP, e),
                });
    }
    
    /**
     * Two ring-of-three cycles joined at two points
     */
    public void testCycle3() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(b, closedP, c));
        cache.addRelation(Triple.create(c, closedP, a));
        cache.addRelation(Triple.create(d, closedP, e));
        cache.addRelation(Triple.create(e, closedP, f));
        cache.addRelation(Triple.create(f, closedP, d));
        cache.addRelation(Triple.create(b, closedP, d));
        cache.addRelation(Triple.create(f, closedP, c));
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(a, directP, null)),
                new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(a, closedP, b),
                Triple.create(a, closedP, c),
                Triple.create(a, closedP, d),
                Triple.create(a, closedP, e),
                Triple.create(a, closedP, f),
                });
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(null, directP, a)),
                new Object[] {
                Triple.create(a, closedP, a),
                Triple.create(b, closedP, a),
                Triple.create(c, closedP, a),
                Triple.create(d, closedP, a),
                Triple.create(e, closedP, a),
                Triple.create(f, closedP, a),
                });
    }
    
    /**
     * Test simple equivalences case
     */
    public void testEquivalencesSimple() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(b, closedP, a));
        TestUtil.assertIteratorValues(this, 
                cache.find(new TriplePattern(null, closedP, null)),
                new Object[] {
                Triple.create(a, closedP, b),
                Triple.create(b, closedP, a),
                Triple.create(b, closedP, b),
                Triple.create(a, closedP, a),
        });
        TestUtil.assertIteratorLength( cache.find(new TriplePattern(null, closedP, null)), 4);
    }
    
    /**
     * Test equivalences case
     */
    public void testEquivalences() {
        TransitiveGraphCache cache = new TransitiveGraphCache(directP, closedP);
        cache.addRelation(Triple.create(a, closedP, b));
        cache.addRelation(Triple.create(b, closedP, a));
        
        cache.addRelation(Triple.create(c, closedP, d));
        cache.addRelation(Triple.create(d, closedP, c));
        
        cache.addRelation(Triple.create(b, closedP, d));
        cache.addRelation(Triple.create(d, closedP, b));

        assertTrue("Test eq", cache.contains(new TriplePattern(a, closedP, d)));
    }

}
