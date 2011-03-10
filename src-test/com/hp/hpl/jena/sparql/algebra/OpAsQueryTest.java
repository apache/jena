package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class OpAsQueryTest {

    /**
     * Test of asQuery method, of class OpAsQuery.
     */
    @Test
    public void testCountStar() {
        Object[] result = checkQuery("select (count(*) as ?cs) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testCountGroup() {
        Object[] result = checkQuery("select (count(?p) as ?cp) { ?s ?p ?o } group by ?s");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testCountGroupAs() {
        Object[] result = checkQuery("select (count(?p) as ?cp) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testDoubleCount() {
        Object[] result = checkQuery("select (count(?s) as ?sc) (count(?p) as ?pc) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    public Object[] checkQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op a = Algebra.compile(orig);
        Query got = OpAsQuery.asQuery(a);
        Object[] r = { a, Algebra.compile(got) };
        return r;
    }
}