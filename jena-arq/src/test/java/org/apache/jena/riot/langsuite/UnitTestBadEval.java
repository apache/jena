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


import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.junit.EarlReport ;
import org.junit.After ;
import org.junit.Before ;

public class UnitTestBadEval extends LangTestCase
{
    private final String uri ;
    private final Lang lang ;

    public UnitTestBadEval(String name, String testURI, String uri, Lang lang, EarlReport earl)
    {
        super(name, testURI, earl) ;
        this.uri = uri ;
        this.lang = lang ;
    }
    
    protected ErrorHandler installed ;
    
    @Override
    @Before
    public void _setUp()
    {
        installed = ErrorHandlerFactory.getDefaultErrorHandler() ;
        ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerStrictSilent()) ;
    }
    
    @Override
    @After
    public void _tearDown()
    {
        ErrorHandlerFactory.setDefaultErrorHandler(installed) ;
    }
    

    @Override
    public void runTestForReal()
    {
        if ( RDFLanguages.isTriples(lang) )
            run3() ;
        else
            run4() ;
    }

    
    private void run3()
    {
        Graph graph = GraphFactory.createDefaultGraph(); 
        try {
            Parse.parse(graph, uri, lang);
            fail("Managed to read a bad evaluation test without error") ;
        }
        catch (JenaException ex) {}
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ; }
    }
    
    private void run4()
    {
        DatasetGraph dsg = DatasetGraphFactory.createGeneral() ;
        try {
            Parse.parse(dsg, uri, lang);
            fail("Managed to read a bad evaluation test without error") ;
        }
        catch (JenaException ex) {}
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ; }
    }

}
