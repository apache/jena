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
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.junit.EarlReport ;

public class UnitTestSyntax extends LangTestCase
{
    private final String uri ;
    private final Lang lang ;

    public UnitTestSyntax(String name, String testURI, String uri, Lang lang, EarlReport earl)
    {
        super(name, testURI, earl) ;
        this.uri = uri ;
        this.lang = lang ;
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
        Parse.parse(graph, uri, lang);
    }
    
    private void run4()
    {
        DatasetGraph dsg = DatasetGraphFactory.createGeneral() ;
        Parse.parse(dsg, uri, lang);
    }
    
    @Override
    protected void _setUp()
    {}

    @Override
    protected void _tearDown()
    {}

}
