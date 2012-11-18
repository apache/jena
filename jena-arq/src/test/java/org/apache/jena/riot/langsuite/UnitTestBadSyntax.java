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

import static org.openjena.riot.SysRIOT.fmtMessage ;
import org.apache.jena.riot.Lang2 ;
import org.apache.jena.riot.WebReader2 ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.RiotException ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class UnitTestBadSyntax extends LangTestCase
{
    private final String uri ;
    private final Lang2 lang ;

    protected UnitTestBadSyntax(String name, String uri, Lang2 lang)
    {
        super(name) ;
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
    public void runTest()
    {
        Model model = ModelFactory.createDefaultModel() ;
        try {
            WebReader2.read(model, uri, uri, lang) ;
        } catch (RiotException ex) { return ; }
        catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            fail("Unexpected exception") ;
        }
        fail("Bad syntax test succeed in parsing the file") ;
    }
}
