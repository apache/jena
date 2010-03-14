/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.io.PrintStream ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.lang.LangTriG ;
import com.hp.hpl.jena.riot.out.SinkQuadOutput ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class trig extends LangParse<Quad>
{
    /** Run the N-triples parser - and produce N-triples */
    public static void main(String... argv)
    {
        Log.setLog4j() ;
        new trig(argv).mainRun() ;
    }    
    
    protected trig(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.classShortName(trig.class) ;
    }

    @Override
    protected void parseEngine(Tokenizer tokens, String baseIRI, Sink<Quad> sink, Checker checker, boolean skipOnBadTerm)
    {
        LangTriG parser = new LangTriG(baseIRI, tokens, checker, sink) ;
        parser.setChecker(checker) ;
        parser.parse();
        sink.close() ;
    }
    
    @Override
    protected Sink<Quad> makePrintSink(PrintStream out)
    {
        return new SinkQuadOutput(out) ;
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