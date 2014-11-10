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

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.util.IsoMatcher ;

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
        // Could generalise run4() to cover both cases.
        // run3() predates dataset reading and is more tested. 
        if ( RDFLanguages.isTriples(lang) )
            run3() ;
        else
            run4() ;
    }
    
    private void run4() { 
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        try {
            if ( baseIRI != null )
                RDFDataMgr.read(dsg, input, baseIRI, lang) ;
            else
                RDFDataMgr.read(dsg, input, lang) ;
            
            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NQUADS) ;
            
            DatasetGraph results = DatasetGraphFactory.createMem() ;
            try {
                RDFDataMgr.read(results, output, outLang) ;
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage()) ;
            }

            boolean b = isomorphic(dsg, results) ;

            if ( !b )
            {
                System.out.println("**** Test: "+getName()) ;
                System.out.println("---- Parsed");
                RDFDataMgr.write(System.out, dsg, Lang.TRIG) ;
                System.out.println("---- Expected");
                RDFDataMgr.write(System.out, results, Lang.TRIG) ;
                System.out.println("--------");
            }
            
            assertTrue("Datasets not isomorphic", b) ;
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

    private boolean isomorphic(DatasetGraph dsg1, DatasetGraph dsg2) {
        return IsoMatcher.isomorphic(dsg1, dsg2) ;
    }

    // Triples test.
    private void run3() {     
        Model model = ModelFactory.createDefaultModel() ;
        try {
            if ( baseIRI != null )
                RDFDataMgr.read(model, input, baseIRI, lang) ;
            else
                RDFDataMgr.read(model, input, lang) ;
            
            Lang outLang = RDFLanguages.filenameToLang(output, Lang.NQUADS) ;
            
            Model results = ModelFactory.createDefaultModel() ;
            try {
                RDFDataMgr.read(results, output, outLang) ;
            } catch (RiotException ex) {
                fail("Failed to read results: "+ex.getMessage()) ;
            }

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
