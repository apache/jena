/**
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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;

import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.EarlTestCase ;

public abstract class LangTestCase extends EarlTestCase
{
    protected LangTestCase(String name, String testURI, EarlReport earl)
    { super(name, testURI, earl) ; }
    
    protected abstract void _setUp() ; 
    protected abstract void _tearDown() ; 
    
    protected boolean sysRIOT_strictMode ;
    protected boolean sysRIOT_strictXSDLexicialForms ;
    
    @Override
    final public void setUpTest()
    {        
        // The W3C Turtle and TriG test suites contain IRIs that generate warnings.
        // They are bad NFC for the version of UTF-8 that Java6 understands.
        BaseTest.setTestLogging(ErrorHandlerFactory.errorHandlerNoWarnings) ;

        // If the test suite is sloppy, with IRIs that are not good practice, you may need
        // to run with warnings as not-errors ....
        //BaseTest.setTestLogging(ErrorHandlerFactory.errorHandlerStd) ;
        sysRIOT_strictMode = SysRIOT.strictMode ;
        sysRIOT_strictXSDLexicialForms = SysRIOT.StrictXSDLexicialForms ;
        
        SysRIOT.strictMode = true ;
        SysRIOT.StrictXSDLexicialForms = true ;
        _setUp() ;
    }
    
    @Override
    final public void tearDownTest()
    {
        _tearDown() ;
        SysRIOT.strictMode = sysRIOT_strictMode ;
        SysRIOT.StrictXSDLexicialForms = sysRIOT_strictXSDLexicialForms ;
        BaseTest.unsetTestLogging() ; 
    }

}

