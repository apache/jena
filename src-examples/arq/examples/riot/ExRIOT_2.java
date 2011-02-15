/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.riot;

import java.io.FileInputStream ;
import java.io.FileNotFoundException ;
import java.io.InputStream ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.SysRIOT ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.system.RiotLib ;

import com.hp.hpl.jena.sparql.core.Quad ;

/** Example of using RIOT directly.
 */
public class ExRIOT_2
{
    public static void main(String...argv) throws FileNotFoundException
    {
        // Ensure RIOT loaded.
        SysRIOT.wireIntoJena() ;

        Sink<Quad> noWhere = new SinkNull<Quad>() ;

        // ---- Parse to a Sink.
        // RIOT controls the conversion from bytes to java chars.
        InputStream in = new FileInputStream("data.trig") ;
        
        RiotReader.parseQuads(in, Lang.TRIG, "http://example/base", noWhere) ;
        
        
        // --- Or create a parser and do the parsing as separate steps.
        String baseURI = "http://example/base" ;
            
        in = new FileInputStream("data.trig") ;
        LangRIOT parser = RiotReader.createParserQuads(in, Lang.TRIG, "http://example/base", noWhere) ;
        
        // Parser to first error or warning.
        ErrorHandler errHandler = ErrorHandlerFactory.errorHandlerStrict ;

        // Now enable stricter checking, even N-TRIPLES must have absolute URIs. 
        ParserProfile profile = RiotLib.profile(baseURI, true, true, errHandler) ;

        // Just set the error handler.
        parser.getProfile().setHandler(errHandler) ;
        
        // Or replave the whole parser profile.
        parser.setProfile(profile) ;

        // Do the work.
        parser.parse() ;
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