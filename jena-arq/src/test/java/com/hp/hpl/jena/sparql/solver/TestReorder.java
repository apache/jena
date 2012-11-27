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

package com.hp.hpl.jena.sparql.solver;

import static com.hp.hpl.jena.sparql.solver.TestSolverLib.bgp ;
import static com.hp.hpl.jena.sparql.solver.TestSolverLib.matcher ;
import static com.hp.hpl.jena.sparql.solver.TestSolverLib.triple ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderProc ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderProcIndexes ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderWeighted ;

public class TestReorder extends BaseTest
{

    @Test public void match_01()
    {
        StatsMatcher matcher = matcher("((:x :p ANY) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5.0, d, 0) ;
    }
    
    @Test public void match_02()
    {
        StatsMatcher matcher = matcher("((:x :p ANY) 5)") ;
        Triple t = triple("(:x :q ?v)") ;   // No match
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }
    
    @Test public void match_03()
    {
        StatsMatcher matcher = matcher("((:x :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }
    
    @Test public void match_04()
    {
        StatsMatcher matcher = matcher("((TERM :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }
    
    @Test public void match_05()
    {
        StatsMatcher matcher = matcher("((URI :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }
    
    @Test public void match_06()
    {
        StatsMatcher matcher = matcher("((LITERAL :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;   // No match
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }

    @Test public void match_07()
    {
        StatsMatcher matcher = matcher("((BNODE :p VAR) 5)") ;
        Triple t = triple("(_:a :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }

    @Test public void match_08()
    {
        StatsMatcher matcher = matcher("((VAR :p LITERAL) 5)") ;
        Triple t = triple("(?x :p ?v)") ;   // No match
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }

    @Test public void match_09()
    {
        StatsMatcher matcher = matcher("((VAR :p LITERAL) 5)") ;
        Triple t = triple("(?x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }

    // Test first match wins. 
    @Test public void match_10()
    {
        StatsMatcher matcher = matcher("((VAR :p LITERAL) 5) ((VAR :p ANY) 10)") ;
        Triple t = triple("(?x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }

    @Test public void match_11()
    {
        StatsMatcher matcher = matcher("((VAR :p ANY) 10) ((VAR :p LITERAL) 5)") ;
        Triple t = triple("(?x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(10, d, 0) ;
    }

    // Abbreviated forms.
    @Test public void match_20()
    {
        StatsMatcher matcher = matcher("(:p 10) ") ;
        Triple t = triple("(?x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(10, d, 0) ;
        
    }
    
    @Test public void match_21()
    {
        StatsMatcher matcher = matcher("(:p 10) ") ;
        Triple t = triple("(?x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(StatsMatcher.weightPO_small, d, 0) ;
    }
    
    @Test public void match_22()
    {
        StatsMatcher matcher = matcher("(:p 11)") ;
        Triple t = triple("(:x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(1, d, 0) ;
    }

    @Test public void match_23()
    {
        StatsMatcher matcher = matcher("(:p 11)") ;
        Triple t = triple("(:x ?p 1913)") ; // No match.
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }

    @Test public void match_24()
    {
        StatsMatcher matcher = matcher("(:p 11) (TERM 12)") ;
        Triple t = triple("(?x :q ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(12, d, 0) ;
    }
    
    // Bounds abbreviation rules.
    @Test 
    public void match_25()
    {
        StatsMatcher matcher = matcher("(:p 3) (other 1)") ;
        Triple t = triple("(?x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(3, d, 0) ;
    }
    
    @Test 
    public void match_26()
    {
        StatsMatcher matcher = matcher("(:pp 3) (other 1)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(1, d, 0) ;
    }

    // Bounds abbreviation rules.
    @Test public void match_27()
    {
        StatsMatcher matcher = matcher("(:p 200) (TERM 2)") ;
        Triple t = triple("(?x :q :v)") ;
        double d = matcher.match(t) ;
        assertEquals(2, d, 0) ;
    }

    @Test public void reorderIndexes1() 
    { 
        ReorderProc proc = new ReorderProcIndexes(new int[]{0,1}) ;
        BasicPattern bgp = bgp("(bgp (:x :p ?v) (:x :q ?w))") ; 
        BasicPattern bgp2 = proc.reorder(bgp) ;
        assertEquals(bgp, bgp2) ;
    }
    
    @Test public void reorderIndexes2() 
    { 
        ReorderProc proc = new ReorderProcIndexes(new int[]{1,0}) ;
        BasicPattern bgp1 = bgp("(bgp (:x :p ?v) (:x :q ?w))") ; 
        BasicPattern bgp2 = bgp("(bgp (:x :q ?w) (:x :p ?v))") ; 
        BasicPattern bgp3 = proc.reorder(bgp1) ;
        assertEquals(bgp2, bgp3) ;
    }
    
    @Test public void stats_01()
    {
        StatsMatcher m = matcher("((:x :p ANY) 5)") ;
        ReorderTransformation transform = new ReorderWeighted(m) ;
        BasicPattern bgp = bgp("(bgp)") ;
        BasicPattern bgp2 = transform.reorder(bgp) ;
        assertEquals(bgp2, bgp) ;
    }
    
    
    @Test public void stats_dft_01()
    {
        ReorderTransformation transform = ReorderLib.fixed() ;
        BasicPattern bgp = bgp("(bgp)") ;
        BasicPattern bgp2 = transform.reorder(bgp) ;
        assertEquals(bgp2, bgp) ;
    }
  
}
