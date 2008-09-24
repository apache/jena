/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import org.junit.Test;
import test.BaseTest;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;

public class TestReorder extends BaseTest
{
    
    static class TestTransform extends ReorderTransformationBase
    {
        @Override
        protected double weight(PatternTriple pt)
        {
            return 0 ;
        }
        
    } ;
    
    @Test public void match_1()
    {
        StatsMatcher matcher = matcher("((:x :p ANY) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5.0, d, 0) ;
    }
    
    @Test public void match_2()
    {
        StatsMatcher matcher = matcher("((:x :p ANY) 5)") ;
        Triple t = triple("(:x :q ?v)") ;   // No match
        double d = matcher.match(t) ;
        assertEquals(-1, d, 0) ;
    }
    
    @Test public void match_3()
    {
        StatsMatcher matcher = matcher("((:x :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }
    
    @Test public void match_4()
    {
        StatsMatcher matcher = matcher("((TERM :p VAR) 5)") ;
        Triple t = triple("(:x :p ?v)") ;
        double d = matcher.match(t) ;
        assertEquals(5, d, 0) ;
    }
    
    
    private static StatsMatcher matcher(String str)
    {
        String s1 = "(prefix ((: <http://example/>))\n(stats " ;
        String s2 = "))" ;
        Item item = SSE.parse(s1+str+s2) ;
        return new StatsMatcher(item) ; 
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

    
    
    private static BasicPattern bgp(String str)
    {
        String s1 = "(prefix ((: <http://example/>)) " ;
        String s2 = ")" ;
        return SSE.parseBGP(s1+str+s2) ;
    }
    
    private static Triple triple(String str)
    {
        String s1 = "(prefix ((: <http://example/>)) " ;
        String s2 = ")" ;
        return SSE.parseTriple(s1+str+s2) ;
    }
    
//  static class Reverse implements ReorderProc {
//
//      @Override
//      public BasicPattern reorder(BasicPattern bgp)
//      {
//          BasicPattern x = new BasicPattern() ;
//          for ( Triple t : NodeLib.tripleList(bgp.getList()) )
//              x.add(0, t) ;
//          return x ;
//      } }
//  
//  static class Y implements ReorderTransformation
//  {
//
//      @Override
//      public BasicPattern reorder(BasicPattern pattern)
//      {
//          return null ;
//      }
//
//      @Override
//      public ReorderProc reorderIndexes(BasicPattern pattern)
//      {
//          return null ;
//      }
//      
//  }
  
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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