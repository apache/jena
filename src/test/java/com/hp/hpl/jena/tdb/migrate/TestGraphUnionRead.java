/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import java.util.Arrays ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;

public class TestGraphUnionRead extends BaseTest
{
    private static String dataStr = StrUtils.strjoinNL(
      "(dataset" ,
      "  (graph" ,
      "   (triple <http://example/s> <http://example/p> 'dft')" ,
      "   (triple <http://example/s> <http://example/p> <http://example/o>)" ,
      " )" ,
      " (graph <http://example/g1>",
      "   (triple <http://example/s> <http://example/p> 'g1')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g2>", 
      "   (triple <http://example/s> <http://example/p> 'g2')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g3>",
      "   (triple <http://example/s> <http://example/p> 'g3')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " ))") ;
    private static DatasetGraph dsg = null ;
    static {
        Item item = SSE.parse(dataStr) ;
        dsg = BuilderGraph.buildDataset(item) ;
    }
    private static Node gn1 = SSE.parseNode("<http://example/g1>") ;
    private static Node gn2 = SSE.parseNode("<http://example/g2>") ;
    private static Node gn3 = SSE.parseNode("<http://example/g3>") ;
    private static Node gn9 = SSE.parseNode("<http://example/g9>") ;
    
    @Test public void gr_union_01()
    {
        List<Node> gnodes = list(gn1, gn2) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(3, x) ;
    }
    
    @Test public void gr_union_02()
    {
        List<Node> gnodes = list(gn1, gn2) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        Node s = Node.createURI("http://example/s") ; 
        long x = Iter.count(g.find(s, null, null)) ;
        assertEquals(3, x) ;
    }

    @Test public void gr_union_03()
    {
        List<Node> gnodes = list(gn1, gn2, gn9) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        Node o = Node.createLiteral("g2") ; 
        long x = Iter.count(g.find(null, null, o)) ;
        assertEquals(1, x) ;
    }
    
    @Test public void gr_union_04()
    {
        List<Node> gnodes = list(gn9) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(0, x) ;
    }

    @Test public void gr_union_05()
    {
        List<Node> gnodes = list() ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(0, x) ;
    }
    
    @Test public void gr_union_06()
    {
        List<Node> gnodes = list(gn1, gn1) ;
        Graph g = new GraphUnionRead(dsg, gnodes) ;
        long x = Iter.count(g.find(null, null, null)) ;
        assertEquals(2, x) ;
    }

    static <T> List<T> list(T...x)
    {
        return Arrays.asList(x) ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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