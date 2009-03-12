/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.solver.TestSolverLib.bgp;
import static com.hp.hpl.jena.tdb.solver.TestSolverLib.matcher;
import static com.hp.hpl.jena.tdb.solver.TestSolverLib.triple;
import org.junit.Test;
import test.BaseTest;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProc;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderProcIndexes;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderWeighted;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;

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
        StatsMatcher matcher = matcher("(:p 11) ") ;
        Triple t = triple("(:x :p 1913)") ;
        double d = matcher.match(t) ;
        assertEquals(1, d, 0) ;
    }

    @Test public void match_23()
    {
        StatsMatcher matcher = matcher("(:p 11) ") ;
        Triple t = triple("(:x ?p 1913)") ; // No match.
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }

    @Test public void match_24()
    {
        StatsMatcher matcher = matcher("(:p 11) (TERM 11)") ;
        Triple t = triple("(?x :q ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(11, d, 0) ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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