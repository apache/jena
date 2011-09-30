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

package org.openjena.riot.lang;

import junit.framework.TestCase ;
import org.openjena.riot.system.JenaReaderTurtle2 ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.shared.JenaException ;


public class UnitTestTurtleBadSyntax extends TestCase
{
    String uri ;
    public UnitTestTurtleBadSyntax(String name, String uri) { super(name) ; this.uri = uri ; }
    
    @Override
    public void runTest()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader t = new JenaReaderTurtle2() ;
        try {
            t.read(model, uri) ;
            fail("Bad syntax test succeed in parsing the file") ;
        } catch (JenaException ex)
        {
            return ;    
        }

    }
}
