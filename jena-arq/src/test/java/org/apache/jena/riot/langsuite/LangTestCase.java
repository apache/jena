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

import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.SysRIOT ;

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
    final public void setUp()
    {        
        BaseTest.setTestLogging(ErrorHandlerFactory.errorHandlerStrictNoLogging) ;
        sysRIOT_strictMode = SysRIOT.strictMode ;
        sysRIOT_strictXSDLexicialForms = SysRIOT.StrictXSDLexicialForms ;
        
        SysRIOT.strictMode = true ;
        SysRIOT.StrictXSDLexicialForms = true ;
        _setUp() ;
    }
    
    @Override
    final public void tearDown()
    {
        _tearDown() ;
        SysRIOT.strictMode = sysRIOT_strictMode ;
        SysRIOT.StrictXSDLexicialForms = sysRIOT_strictXSDLexicialForms ;
        BaseTest.unsetTestLogging() ; 
    }

}

