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

import org.apache.jena.riot.Lang2 ;
import org.apache.jena.riot.WebReader2 ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class UnitTestSyntax extends LangTestCase
{
    private final String uri ;
    private final Lang2 lang ;
    public UnitTestSyntax(String name, String uri, Lang2 lang) { super(name) ; this.uri = uri ; this.lang = lang ; }
    
    @Override
    public void runTest()
    {
        Model model = ModelFactory.createDefaultModel() ;
        WebReader2.read(model, uri, uri, lang) ;
    }

    @Override
    protected void _setUp()
    {}

    @Override
    protected void _tearDown()
    {}

}
