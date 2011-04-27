/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import java.nio.ByteBuffer ;
import java.util.Arrays ;
import java.util.Collection ;

import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.ByteBufferLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

@RunWith(Parameterized.class)
public class TestCodec extends BaseTest 
{
    static private final String asciiBase             = "abc" ;
    static private final String latinBase             = "Àéíÿ" ;
    static private final String latinExtraBase        = "ỹﬁﬂ" ;     // fi-ligature, fl-ligature
    static private final String greekBase             = "αβγ" ;
    static private final String hewbrewBase           = "אבג" ;
    static private final String arabicBase            = "ءآأ";
    static private final String symbolsBase           = "☺☻♪♫" ;
    static private final String chineseBase           = "孫子兵法" ; // The Art of War 
    static private final String japaneseBase          = "日本" ;    // Japanese
    
    // Each Object[] becomes the arguments to the class constructor (with reflection)
    // Reflection is not sensitive to generic parameterization (it's type erasure) 
    @Parameters public static Collection<Object[]> data()
    { 
        return Arrays.asList(new Object[][]
                                        { { new NodecSSE() } } 
                                        ) ;                                        
    }


    private Nodec nodec ;
    
    public TestCodec(Nodec nodec) { this.nodec = nodec ; }
    
    @Test public void nodec_lit_01()    { test ("''") ; }
    @Test public void nodec_lit_02()    { test ("'a'") ; }
    @Test public void nodec_lit_03()    { test ("'ab'") ; }
    @Test public void nodec_lit_04()    { test ("'abc'") ; }
    @Test public void nodec_lit_05()    { test ("'abcd'") ; }
    
    @Test public void nodec_lit_06()    { test ("''@e") ; }
    @Test public void nodec_lit_07()    { test ("''@en") ; }
    @Test public void nodec_lit_08()    { test ("''@EN-uk") ; }
    @Test public void nodec_lit_09()    { test ("'\\n'@EN-uk") ; }
    
    @Test public void nodec_lit_10()    { test ("'"+latinBase+"'") ; }
    @Test public void nodec_lit_11()    { test ("'"+latinExtraBase+"'") ; }
    @Test public void nodec_lit_12()    { test ("'"+greekBase+"'") ; }
    @Test public void nodec_lit_13()    { test ("'"+hewbrewBase+"'") ; }
    @Test public void nodec_lit_14()    { test ("'"+arabicBase+"'") ; }
    @Test public void nodec_lit_15()    { test ("'"+symbolsBase+"'") ; }
    @Test public void nodec_lit_16()    { test ("'"+chineseBase+"'") ; }
    @Test public void nodec_lit_17()    { test ("'"+japaneseBase+"'") ; }
    
    @Test public void nodec_lit_20()    { test ("1") ; }
    @Test public void nodec_lit_21()    { test ("12.3") ; }
    @Test public void nodec_lit_22()    { test ("''^^<>") ; }
    
    @Test public void nodec_uri_01()    { test ("<>") ; }
    @Test public void nodec_uri_02()    { test ("<http://example/>") ; }
    
    // Jena anon ids can have a string form including ":"
    @Test public void nodec_blank_01()  { test (Node.createAnon(new AnonId("a"))) ; }
    @Test public void nodec_blank_02()  { test (Node.createAnon(new AnonId("a:b:c-d"))) ; }
    @Test public void nodec_blank_03()  { test (Node.createAnon()) ; }
    
    private void test(String sseString)
    {
        Node n = NodeFactory.parseNode(sseString) ;
        test(n) ;
    }
    
    private void test(Node n)
    {
        int maxSize = nodec.maxSize(n) ;
        ByteBuffer bb = ByteBuffer.allocate(maxSize) ;
        int x = nodec.encode(n, bb, null) ;
        int bbLen = bb.limit()-bb.position();
        assertEquals(bbLen, x) ;
        assertEquals(0, bb.position()) ;
        
        ByteBuffer bb2 = ByteBufferLib.duplicate(bb) ;
        Node n2 = nodec.decode(bb2, null) ;
        assertEquals(n, n2) ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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