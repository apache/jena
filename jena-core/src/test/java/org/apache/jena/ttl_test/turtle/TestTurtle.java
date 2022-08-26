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

package org.apache.jena.ttl_test.turtle;


import junit.framework.TestCase;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFReaderI ;
import org.apache.jena.ttl.turtle.TurtleParseException;
import org.apache.jena.ttl.turtle.TurtleReader;
import org.apache.jena.util.FileManager ;
import org.apache.jena.util.FileUtils ;


public class TestTurtle extends TestCase
{
    String input ;
    String output ;
    String baseIRI ;
    
    public TestTurtle(String name, String input, String output, String baseIRI)
    { super(name) ; this.input = input ; this.output = output ; this.baseIRI = baseIRI ; }
    
    @Override
    public void runTest()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReaderI t = new TurtleReader() ;
        try {
            if ( baseIRI != null )
                t.read(model, FileManager.getInternal().open(input), baseIRI) ;
            else
                t.read(model, input) ;  
            // "http://www.w3.org/2001/sw/DataAccess/df1/tests/rdfq-results.ttl"

            String syntax = FileUtils.guessLang(output, FileUtils.langNTriple) ;
            
            @SuppressWarnings("deprecation")
            Model results = FileManager.getInternal().loadModel(output, syntax);
            boolean b = model.isIsomorphicWith(results) ;
            if ( !b )
                assertTrue("Models not isomorphic", b) ;
        } catch (TurtleParseException ex)
        {
            throw ex ;    
        }
    }
}
