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

package org.apache.jena.rdf_star;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestSPARQLStarParse {

    // See also testing/ARQ/Syntax/Syntax-ARQ
    private static final String PREFIXES = StrUtils.strjoinNL
                ("PREFIX rdf: <"+RDF.getURI()+">"
                ,"PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>"
                ,"PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>"
                ,"PREFIX :    <http://example/>"
                ,""
                );

    private static Query parse(String string) {
        Query query = QueryFactory.create(PREFIXES+"SELECT * "+string, Syntax.syntaxARQ);
        return query;
    }

    private static void parseVars(String string, String... varNames) {
        Query query = parse(string);

        List<String> vars = query.getResultVars();
        assertEquals("Wrong number of variables: "+Arrays.asList(varNames)+" : query: "+vars, varNames.length, vars.size());
        for ( String v : varNames )
            assertTrue("Expected variable ?"+v, vars.contains(v));
    }

    @Test public void parse_good_1()    { parse("{ << :s :p :o >> :q 456 }"); }

    @Test public void parse_good_2()    { parse("{ ?X :q << ?s ?p 123 >> }"); }

    @Test public void parse_good_3()    { parse("{ << ?s ?p 123 >> :q << ?s ?p 123 >> }"); }

    @Test public void parse_good_4()    { parse("{ << << :s :p 12 >> ?p << :s :p 34 >> >> :q << ?s ?p 123 >> }"); }

    @Test public void parse_vars_1()    { parseVars("{ ?X :q << ?s ?p 123 >> }", "X", "s", "p"); }

    @Test public void parse_vars_2()    { parseVars("{ ?X :q << ?s :p <<:s :q ?q>> >> }", "X", "s", "q"); }

    // Test structures created.
    @Test public void build_1()    {
        Triple t = build("{ << :s :p :o >> :q 456 }");
        assertTrue(t.isConcrete());
        assertTrue(t.getSubject().isNodeTriple());
    }

    @Test public void build_2()    {
        Triple t = build("{ :x  :q << <<:s ?p :o>> :p 678 >> }");
        assertFalse(t.isConcrete());
        assertTrue(t.getObject().isNodeTriple());
        assertTrue(t.getObject().getTriple().getSubject().isNodeTriple());
    }

    @Test public void build_3()    {
        Triple t = build("{ << :s ?p :o >> :q 456 }");
        assertFalse(t.isConcrete());
        assertTrue(t.getSubject().isNodeTriple());
    }

    // OpAsQuery
    @Test public void queryToOpToQuery_1()    {
        queryToOpToQuery("{ << :s :p :o >> :q 456 }");
    }

    @Test public void queryToOpToQuery_2()    {
        queryToOpToQuery("{ << ?s ?p ?o >> :q 456 }");
    }

    @Test public void queryToOpToQuery_3()    {
        queryToOpToQuery("{ << << :s :p ?x2 >> ?p << :s :p ?x2 >> >> :q << ?s ?p 123 >> }");
    }

    private static void queryToOpToQuery(String string) {
        Query query = parse(string);
        Op op = Algebra.compile(query);
        query.getPrefixMapping().clearNsPrefixMap();
        Query query2 = OpAsQuery.asQuery(op);
        assertEquals(query, query2);
    }

    private static Triple build(String string) {
        Query query = parse(string);
        Op op = Algebra.compile(query);
        BasicPattern bgp = ((OpBGP)op).getPattern();
        assertEquals(1, bgp.size());
        Triple t = bgp.get(0);
        return t ;
    }


    @Test(expected=QueryParseException.class)
    public void parse_bad_1()           { parse("{ <<:s :p :o>> }"); }

    @Test(expected=QueryParseException.class)
    public void parse_bad_2()           { parse("{ ?X << :s :p 123 >> ?Z }"); }

    @Test(expected=QueryParseException.class)
    public void parse_bad_3()           { parse("{ << :subject << :s :p 12 >> :object >> :q 123 }"); }

}
