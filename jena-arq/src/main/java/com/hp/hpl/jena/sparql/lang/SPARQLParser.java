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

package com.hp.hpl.jena.sparql.lang;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;

/** This class provides the root of lower level access to all the parsers.
 *  Each subclass hides the details of the per-language exception handlers and other
 *  javacc details to provide a methods that deal with setting up Query objects
 *  and using QueryException exceptions for problems. */

public abstract class SPARQLParser
{
    public final Query parse(Query query, String queryString) throws QueryParseException
    {
        // Sort out BOM
        if ( queryString.startsWith("\uFEFF") )
            queryString = queryString.substring(1) ;
        return parse$(query, queryString) ;
    }
    
    protected abstract Query parse$(Query query, String queryString) throws QueryParseException ;
    
    
    public static boolean canParse(Syntax syntaxURI)
    {
        return SPARQLParserRegistry.get().containsFactory(syntaxURI) ;
    }
    
    public static SPARQLParser createParser(Syntax syntaxURI)
    {
        return SPARQLParserRegistry.get().createParser(syntaxURI) ;
    }

    // Do any testing of queries after the construction of the parse tree.
    protected void validateParsedQuery(Query query)
    {
        SyntaxVarScope.check(query) ;
    }
}
