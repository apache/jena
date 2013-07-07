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

import static org.apache.jena.riot.SysRIOT.fmtMessage ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;

public class UnitTestBadSyntax extends LangTestCase
{
    private final String uri ;
    private final Lang lang ;

    protected UnitTestBadSyntax(String name, String testURI, String uri, Lang lang, EarlReport earl)
    {
        super(name, testURI, earl) ;
        this.uri = uri ;
        this.lang = lang ;
    }
    
    /** An error handler that throw exceptions on warnings and errors */ 
    private static ErrorHandler errorHandlerTestStrict = new ErrorHandler()
    {
        /** report a warning  - do not carry on */
        @Override
        public void warning(String message, long line, long col)
        { 
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
        
        /** report an error - do not carry on */
        @Override
        public void error(String message, long line, long col)
        { 
            throw new RiotException(fmtMessage(message, line, col)) ;
        }

        @Override
        public void fatal(String message, long line, long col)
        {
            throw new RiotException(fmtMessage(message, line, col)) ;
        }
    } ;

    @Override
    protected void _setUp()         { BaseTest.setTestLogging(ErrorHandlerFactory.errorHandlerStrictNoLogging) ; }

    @Override
    protected void _tearDown()      { BaseTest.unsetTestLogging() ; }

    @Override
    public void runTestForReal()
    {
        if ( RDFLanguages.isTriples(lang) )
            run3() ;
        else
            run4() ;
    }
    
    private void run3() {
        Model model = ModelFactory.createDefaultModel() ;
        try {
            RDFDataMgr.read(model, uri, uri, lang) ;
        } catch (RiotException ex) { return ; }
        catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            fail("Unexpected exception") ;
        }
        fail("Bad syntax test succeed in parsing the file") ;
    }
    
    private void run4() {
        Dataset ds = DatasetFactory.createMem() ;
        try {
            RDFDataMgr.read(ds, uri, uri, lang) ;
        } catch (RiotException ex) { return ; }
        catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            fail("Unexpected exception") ;
        }
        fail("Bad syntax test succeed in parsing the file") ;
    }
}
