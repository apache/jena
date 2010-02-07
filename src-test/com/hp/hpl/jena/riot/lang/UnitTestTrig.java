/*
 * (c) Copyright 2010 Talis Information Ltd
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.io.InputStream ;

import junit.framework.TestCase ;
import atlas.io.IO ;
import atlas.lib.Sink ;

import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.ParseException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.lib.DatasetLib ;

public class UnitTestTrig extends TestCase
{
    String input ;
    String output ;
    String baseIRI ;
    
    public UnitTestTrig(String name, String input, String output, String baseIRI)
    {
        super(name) ;
        this.input = input ;
        this.output = output ;
        this.baseIRI = baseIRI ;
    }
    
    @Override
    public void runTest()
    {
        try {
            DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
            Sink<Quad> sink = DatasetLib.datasetSink(dsg) ;
            InputStream in =  IO.openFile(input) ;
            LangRIOT parser = Lang.createParserTriG(baseIRI, in, sink) ; 
            parser.parse() ;
            sink.flush();

            //DatasetLib.dump(dsg) ;
            
            DatasetGraph dsg2 = DatasetLib.createDatasetGraphMem() ;
            DatasetLib.read(IO.openFile(output), dsg2, "NQUADS") ;

            // Compare with expected.

            boolean b = DatasetLib.isomorphic(dsg, dsg2) ;
            if ( ! b )
            {
                System.out.println("---- Parsed");
                DatasetLib.dump(dsg) ;
                System.out.println("---- Expected");
                DatasetLib.dump(dsg2) ;
                System.out.println("--------");
            }

            assertTrue("Datasets are not isomorphic", b) ;
        } 
        // Catch and retrhow - debugging.
        catch (ParseException ex) 
        {
            throw ex ;
        }
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd
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