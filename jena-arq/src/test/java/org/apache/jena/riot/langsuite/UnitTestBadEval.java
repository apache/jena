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


import junit.framework.TestCase ;
import org.apache.jena.riot.Lang2 ;
import org.junit.After ;
import org.junit.Before ;
import org.openjena.riot.* ;
import org.openjena.riot.system.JenaReaderTurtle2 ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.shared.JenaException ;

public class UnitTestBadEval extends TestCase
{
    private final String input ;
    private final Lang2 lang ;

    protected UnitTestBadEval(String name, String uri, Lang2 lang)
    {
        super(name) ;
        this.input = uri ;
        this.lang = lang ;
    }
    
    ErrorHandler installed ;
    
    @Override
    @Before
    public void setUp()
    {
        installed = ErrorHandlerFactory.getDefaultErrorHandler() ;
        ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerStrictSilent()) ;
    }
    
    @Override
    @After
    public void tearDown()
    {
        ErrorHandlerFactory.setDefaultErrorHandler(installed) ;
    }
    

    @Override
    public void runTest()
    {
        // Install picky error handler.
        
        
        
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader rdfreader = new JenaReaderTurtle2() ;
        try {
            rdfreader.read(model, input) ;
            fail("Managed to read a bad evaluation test without error") ;
        } 
        catch (RiotException ex) {}
        catch (JenaException ex) {}
        catch (RuntimeException ex) 
        { 
            ex.printStackTrace(System.err) ;
            throw ex ; }
    }
}
