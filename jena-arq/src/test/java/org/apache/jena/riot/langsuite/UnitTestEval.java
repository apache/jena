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

package org.apache.jena.riot.langsuite;

import java.io.InputStream ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.stream.StreamManager ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.util.FileUtils ;

public class UnitTestEval extends LangTestCase
{
    String input ;
    String output ;
    String baseIRI ;
    Lang lang ;
    
    public UnitTestEval(String name, String testURI, String input, String output, String baseIRI, Lang lang, EarlReport earl)
    {
        super(name, testURI, earl) ;
        this.input = input ;
        this.output = output ;
        this.baseIRI = baseIRI ;
        this.lang = lang ;
    }
    
    @Override
    protected void _setUp()
    {}

    @Override
    protected void _tearDown()
    {}

    @Override
    public void runTestForReal()
    {
        Model model = ModelFactory.createDefaultModel() ;
        try {
            if ( baseIRI != null )
                RDFDataMgr.read(model, input, baseIRI, lang) ;
            else
                RDFDataMgr.read(model, input, lang) ;
            
            String syntax = FileUtils.guessLang(output, FileUtils.langNTriple) ;
            Model results = ModelFactory.createDefaultModel() ;
            // Directly get an N-triples reader
            InputStream in = StreamManager.get().open(output) ;
            RDFDataMgr.read(results, in, null, RDFLanguages.NTRIPLES) ;

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
        } catch (RiotException ex)
        {
            // Catch and rethrow - debugging.
            throw ex ;    
        }
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ; }
    }
}
