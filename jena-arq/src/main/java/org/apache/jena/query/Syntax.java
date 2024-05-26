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

package org.apache.jena.query;

import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sparql.util.TranslationTable ;

/** Symbols for query language syntaxes */

public class Syntax extends Symbol {

    /** The syntax that the DAWG working group defined. */
    public static final Syntax syntaxSPARQL_10 = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_10");

    /** The syntax that the SPARQL 1.1 working group has defined. */
    public static final Syntax syntaxSPARQL_11 = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_11");

    /** The syntax that is SPARQL 1.2 from the RDF-star working group. */
    public static final Syntax syntaxSPARQL_12 = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_12");

    /** The query syntax for extended SPARQL. */
    public static final Syntax syntaxARQ = new Syntax("http://jena.hpl.hp.com/2003/07/query/ARQ");

    /** SPARQL algebra (SSE syntax) - custom extension. */
    public static final Syntax syntaxAlgebra = new Syntax("http://jena.hpl.hp.com/2003/07/query/SPARQL_Algebra");

    /** The system default syntax */
    public static Syntax defaultSyntax = syntaxARQ;

    /**
     * The name of the default query language for query parsing. The default query
     * language syntax must be capable of accepting any SPARQL query but may also
     * accept extensions.
     */
    public static Syntax defaultQuerySyntax = defaultSyntax;

    /**
     * The name of the default update language for update parsing. The default update
     * language syntax must be capable of accepting any SPARQL query but may also
     * accept extensions.
     */
    public static Syntax defaultUpdateSyntax = defaultSyntax;

    /** The latest SPARQL query syntax - no ARQ syntax extensions. */
    public static final Syntax syntaxSPARQL = syntaxSPARQL_12;

    public static TranslationTable<Syntax> querySyntaxNames = new TranslationTable<>(true);
    static {
        querySyntaxNames.put("sparql", syntaxSPARQL);

        querySyntaxNames.put("sparql10", syntaxSPARQL_10);
        querySyntaxNames.put("sparql_10", syntaxSPARQL_10);

        querySyntaxNames.put("sparql11", syntaxSPARQL_11);
        querySyntaxNames.put("sparql_11", syntaxSPARQL_11);

        querySyntaxNames.put("sparql12", syntaxSPARQL_12);
        querySyntaxNames.put("sparql_12", syntaxSPARQL_12);

        querySyntaxNames.put("arq", syntaxARQ);

        querySyntaxNames.put("alg", syntaxAlgebra);
        querySyntaxNames.put("op", syntaxAlgebra);
    }

    public static TranslationTable<Syntax> updateSyntaxNames = new TranslationTable<>(true);
    static {
        updateSyntaxNames.put("sparql", syntaxSPARQL);
        updateSyntaxNames.put("sparql_11", syntaxSPARQL_11);
        updateSyntaxNames.put("sparql_12", syntaxSPARQL_11);
        updateSyntaxNames.put("arq", syntaxARQ);
    }

    protected Syntax(String s) {
        super(s);
    }

    protected Syntax(Syntax s) {
        super(s);
    }

    public static Syntax make(String uri) {
        if ( uri == null )
            return null;

        Symbol sym = Symbol.create(uri);

        if ( sym.equals(syntaxARQ) )
            return syntaxARQ;

        if ( sym.equals(syntaxSPARQL) )
            return syntaxSPARQL;
        if ( sym.equals(syntaxSPARQL_10) )
            return syntaxSPARQL_10;
        if ( sym.equals(syntaxSPARQL_11) )
            return syntaxSPARQL_11;
        if ( sym.equals(syntaxSPARQL_11) )
            return syntaxSPARQL_12;
        if ( sym.equals(syntaxAlgebra) )
            return syntaxAlgebra;
        return null;
    }

    public static Syntax guessFileSyntax(String url) {
        return guessFileSyntax(url, defaultQuerySyntax);
    }

    /** Guess the syntax (query and update) based on filename */
    public static Syntax guessFileSyntax(String url, Syntax defaultSyntax) {
        if ( url.endsWith(".arq") )
            return syntaxARQ;
        if ( url.endsWith(".rq") )
            return defaultQuerySyntax;

        if ( url.endsWith(".aru") )
            return syntaxARQ;
        if ( url.endsWith(".ru") )
            return defaultUpdateSyntax;

        if ( url.endsWith(".sse") )
            return syntaxAlgebra;

        // Default
        return defaultSyntax;
    }

    /** Guess the query syntax based on file name */
    public static Syntax guessQueryFileSyntax(String url) {
        return guessFileSyntax(url, defaultQuerySyntax);
    }

    /** Guess the query syntax based on file name */
    public static Syntax guessQueryFileSyntax(String url, Syntax defaultSyntax) {
        if ( url.endsWith(".arq") )
            return syntaxARQ;
        if ( url.endsWith(".rq") )
            return defaultQuerySyntax;
        if ( url.endsWith(".sse") )
            return syntaxAlgebra;
        return defaultSyntax;
    }

    /** Guess the update syntax based on file name */
    public static Syntax guessUpdateFileSyntax(String url) {
        return guessUpdateFileSyntax(url, defaultUpdateSyntax);
    }

    /** Guess the update syntax based on file name */
    public static Syntax guessUpdateFileSyntax(String url, Syntax defaultSyntax) {
        if ( url.endsWith(".aru") )
            return syntaxARQ;
        if ( url.endsWith(".ru") )
            return defaultQuerySyntax;
        if ( url.endsWith(".sse") )
            return syntaxAlgebra;
        return defaultSyntax;
    }

    public static Syntax lookup(String s) {
        return querySyntaxNames.lookup(s);
    }
}
