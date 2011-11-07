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

package org.openjena.riot.lang;


import java.io.InputStream ;
import java.io.Reader ;

import junit.framework.TestCase ;
import org.openjena.atlas.io.PeekReader ;
import org.openjena.riot.RiotParseException ;
import org.openjena.riot.system.JenaReaderNTriples2 ;
import org.openjena.riot.system.JenaReaderTurtle2 ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.util.FileUtils ;

public class UnitTestTurtle extends TestCase
{
    String input ;
    String output ;
    String baseIRI ;
    
    public UnitTestTurtle(String name, String input, String output, String baseIRI)
    {
        super(name) ;
        this.input = input ;
        this.output = output ;
        this.baseIRI = baseIRI ;
    }
    
    @Override
    public void runTest()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader t = new JenaReaderTurtle2() ;
        try {
            if ( baseIRI != null )
            {
                InputStream in =  FileManager.get().open(input) ;
                Reader r = PeekReader.makeUTF8(in) ;
                t.read(model, r, baseIRI) ;
            }
            else
                t.read(model, input) ;  
            // "http://www.w3.org/2001/sw/DataAccess/df1/tests/rdfq-results.ttl"

            String syntax = FileUtils.guessLang(output, FileUtils.langNTriple) ;
            
            //Model results = FileManager.get().loadModel(output, syntax);

            Model results = ModelFactory.createDefaultModel() ;
            // Supports \ U escapes
            // But the tokenizer had better be right! (they share the same tokenizer)
            new JenaReaderNTriples2().read(results, output) ;

            boolean b = model.isIsomorphicWith(results) ;
            if ( !b )
            {
                //model.isIsomorphicWith(results) ;
                System.out.println("---- Parsed");
                model.write(System.out, "TTL") ;
                System.out.println("---- Expected");
                results.write(System.out, "TTL") ;
                System.out.println("--------");
            }
            
            assertTrue("Models not isomorphic", b) ;
        } catch (RiotParseException ex)
        {
            // Catch and retrhow - debugging.
            throw ex ;    
        }
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ; }
    }
}
