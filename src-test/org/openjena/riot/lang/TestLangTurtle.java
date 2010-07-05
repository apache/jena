/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import java.io.Reader ;
import java.io.StringReader ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.riot.ErrorHandlerLib ;
import org.openjena.riot.JenaReaderTurtle2 ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;

public class TestLangTurtle extends BaseTest
{
    @Test public void blankNodes1()
    {
        String s = "_:a <http://example/p> 'foo' . " ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        
        RDFReader reader = new JenaReaderTurtle2() ;
        reader.read(m, r, null) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;

        // reset - reread - new bNode.
        r = new StringReader(s) ;
        reader.read(m, r, null) ;
        assertEquals(2, m.size()) ;
    }
    
    @Test public void blankNodes2()
    {
        // Duplicate.
        String s = "_:a <http://example/p> 'foo' . _:a <http://example/p> 'foo' ." ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        
        RDFReader reader = new JenaReaderTurtle2() ;
        reader.read(m, r, null) ;
        assertEquals(1, m.size()) ;
    }

    
    @Test public void updatePrefixMapping()
    {
        JenaReaderTurtle2 parser = new JenaReaderTurtle2() ;
        Model model = ModelFactory.createDefaultModel() ;
        Reader reader = new StringReader("@prefix x: <http://example/x>.") ;
        parser.read(model, reader, "http://example/base/") ;
        
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/x", model.getNsPrefixURI("x")) ;
    }
    
    // Call parser directly.
    
    private static void parse(String string)
    {
        Reader reader = new StringReader(string) ;
        String baseIRI = "http://base/" ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(reader) ;
        LangTurtle parser = RiotReader.createParserTurtle(tokenizer, "http://base/", new SinkNull<Triple>()) ;
        parser.setProfile(RiotReader.profile(Lang.TURTLE, baseIRI, ErrorHandlerLib.errorHandlerNoLogging)) ;
        parser.parse() ;
    }
    
//    private static void parseSilent(String string)
//    {
//        Reader reader = new StringReader(string) ;
//        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(reader) ;
//        String baseIRI = "http://base/" ;
//        LangTurtle parser = RiotReader.createParserTurtle(tokenizer, baseIRI, new SinkNull<Triple>()) ;
//        parser.setProfile(RiotReader.profile(Lang.TURTLE, baseIRI, ErrorHandlerLib.errorHandlerNoLogging)) ;
//        parser.parse() ;
//    }

    @Test
    public void triple()                    { parse("<s> <p> <o> .") ; }
    
    @Test(expected=RiotException.class)
    public void errorJunk_1()               { parse("<p>") ; }
    
    @Test(expected=RiotException.class)
    public void errorJunk_2()               { parse("<r> <p>") ; }

    @Test(expected=RiotException.class)
    public void errorNoPrefixDef()          { parse("x:p <p> 'q' .") ; }
    
    @Test(expected=RiotException.class)
    public void errorNoPrefixDefDT()        { parse("<p> <p> 'q'^^x:foo .") ; }

    @Test(expected=RiotException.class)
    public void errorBadDatatype()          { parse("<p> <p> 'q'^^.") ; }
    
    @Test(expected=RiotException.class)
    public void errorBadURI_1()
    { parse("<http://example/a b> <http://example/p> 123 .") ; }

    @Test(expected=RiotException.class)
    public void errorBadURI_2()
    { parse("<http://example/a%aAb> <http://example/p> 123 .") ; }

    // Test that messages get printed
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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