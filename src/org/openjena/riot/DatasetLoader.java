/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;


import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;

public class DatasetLoader
{
    
//    static private Loader loader = new Loader() ;
//    static public Loader get() { return loader ; }
    
    /** Parse a file and return the quads in a dataset (in-memory) */ 
    public static DatasetGraph load(String filename, Lang lang)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        read(filename, dsg, lang, null) ;
        return dsg ;
    }
    
    /** Parse a file and return the quads in a dataset (in-memory) */ 
    public static DatasetGraph load(String filename, Lang lang, String baseURI)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        read(filename, dsg, lang, baseURI) ;
        return dsg ;
    }
    
    /** Parse a string and return the quads in a dataset (in-memory) (convenience operation)*/ 
    public static DatasetGraph fromString(String string, Lang language, String baseURI)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = DatasetLib.datasetSink(dsg) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        
        if ( language == Lang.NQUADS )
        {
            LangRIOT parser = RiotReader.createParserNQuads(tokenizer, sink) ;
            parser.parse() ;
            sink.flush();
            return dsg;
        }
        if ( language == Lang.TRIG )
        {
            LangRIOT parser = RiotReader.createParserTriG(tokenizer, baseURI, sink) ;
            parser.parse() ;
            sink.flush();
            return dsg;
        }
        return dsg ;
    }

    /** Parse a file and send the quads to a dataset */ 
    public static void read(String filename, DatasetGraph dataset, Lang lang, String baseURI)
    {
        InputStream input = IO.openFile(filename) ;
        read(input, dataset, lang, baseURI) ;
    }
    
    /** Parse an input stream and send the quads to a dataset */ 
    public static void read(InputStream input, DatasetGraph dataset, Lang language, String baseURI)
    {
        Sink<Quad> sink = DatasetLib.datasetSink(dataset) ;
        read(input, language, baseURI, sink) ;
    }
    
    /** Parse an input stream and send the quads to the sink */ 
    public static void read(InputStream input, Lang language, String baseURI, Sink<Quad> sink)
    {
        if ( language == Lang.NQUADS )
        {
            LangRIOT parser = RiotReader.createParserNQuads(input, sink) ;
            parser.parse() ;
            sink.flush();
            return ;
        }
        if ( language == Lang.TRIG )
        {
            LangRIOT parser = RiotReader.createParserTriG(input, baseURI, sink) ;
            parser.parse() ;
            sink.flush();
            return ;
        }
        throw new RiotException("Language not supported for quads: "+language) ;
    }
    
    
    
}

/*
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