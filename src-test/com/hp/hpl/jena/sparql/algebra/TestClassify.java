/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestCase ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin ;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin ;
import com.hp.hpl.jena.sparql.engine.main.JoinClassifier ;
import com.hp.hpl.jena.sparql.engine.main.LeftJoinClassifier ;

public class TestClassify extends TestCase
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestClassify.class) ;
    }
//    public static Test suite()
//    {
//        TestSuite ts = new TestSuite(TestClassify.class) ;
//        ts.setName(Utils.classShortName(TestClassify.class)) ;
//        return ts ;
//    }

    @Test public void testClassify_Join_01() 
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(true) } }", true) ; }

    @Test public void testClassify_Join_02() 
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(?s) } }", true) ; }

    @Test public void testClassify_Join_03() 
	{ classifyJ("{?s :p :o . { ?s :p ?o FILTER(?o) } }", true) ; }

    @Test public void testClassify_Join_04() 
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(?o) } }", true) ; }

    @Test public void testClassify_Join_05() 
	{ classifyJ("{?s :p :o . { ?x :p :o FILTER(?s) } }", false) ; }

    @Test public void testClassify_Join_06() 
	{ classifyJ("{ { ?s :p :o FILTER(true) } ?s :p :o }", true) ; }

	@Test public void testClassify_Join_07() 
	{ classifyJ("{ { ?s :p :o FILTER(?s) }   ?s :p :o }", true) ; }

	@Test public void testClassify_Join_08() 
	{ classifyJ("{ { ?s :p ?o FILTER(?o) }   ?s :p :o }", true) ; }

	@Test public void testClassify_Join_09() 
	{ classifyJ("{ { ?s :p :o FILTER(?o) }   ?s :p :o }", true) ; }

    // Actually, this is safe IF executed left, then streamed to right.
	@Test public void testClassify_Join_10() 
	{ classifyJ("{ { ?x :p :o FILTER(?s) }   ?s :p :o }", true) ; }

    // Not safe: ?s
    // Other parts of RHS may restrict ?s to things that can't match the LHS.
	@Test public void testClassify_Join_11() 
	{ classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o } } }", false) ; }

    // Not safe: ?s
	@Test public void testClassify_Join_12() 
	{ classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o FILTER(?s) } } }", false) ; }

	@Test public void testClassify_Join_13() 
	{ classifyJ("{?s :p :o . { ?x :p :o OPTIONAL { :s :p :o FILTER(?x) } } }", true) ; }

	@Test public void testClassify_Join_14() 
	{ classifyJ("{?s :p :o . { OPTIONAL { :s :p :o FILTER(?o) } } }", true) ; }

	@Test public void testClassify_Join_15() 
	{ classifyJ("{?s :p :o . { OPTIONAL { ?x :p :o FILTER(?s) } } }", false) ; }

    @Test public void testClassify_Join_20() 
    { classifyJ("{ {?s :p ?x } . { {} OPTIONAL { :s :p ?x } } }", false) ; }
    
    // Assuming left-right execution, this is safe.
    @Test public void testClassify_Join_21() 
    { classifyJ("{ { {} OPTIONAL { :s :p ?x } } {?s :p ?x } }", true) ; }

    // Not a join by adjacent BGP flattening. 
//    @Test public void testClassify_Join_30() 
//    { classifyJ("{ ?x ?y ?z {SELECT * { ?s ?p ?o} } }", true) ; }
    
    // Subselect with modifier is handled witout linearization
    @Test public void testClassify_Join_31() 
    { classifyJ("{ ?x ?y ?z {SELECT ?s { ?s ?p ?o} } }", false) ; }

    private void classifyJ(String pattern, boolean expected)
    {
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(query.getQueryPattern()) ;
        
        if ( ! ( op instanceof OpJoin ) )
            fail("Not a join: "+pattern) ;

        boolean nonLinear = JoinClassifier.isLinear((OpJoin)op) ;
        assertEquals("Join: "+pattern, expected, nonLinear) ;
    }

    @Test public void testClassify_LeftJoin_01()
    { classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?x} }", true)  ; }
    
    @Test public void testClassify_LeftJoin_02()
    { classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 ?p2 ?x} } }", true)  ; }
    
    @Test public void testClassify_LeftJoin_03()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?o3} } }", true)  ; }
    
    @Test public void testClassify_LeftJoin_04()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?x} } }", false)  ; }
    
    @Test public void testClassify_LeftJoin_05()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s ?p ?x OPTIONAL { ?s ?p ?x } } }", true)  ; }

    @Test public void testClassify_LeftJoin_06()  // Note use of {{ }}
    { classifyLJ("{ ?s ?p ?x OPTIONAL { { ?s ?p ?o FILTER(?x) } } }", false)  ; }

    @Test public void testClassify_LeftJoin_07()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s ?p ?x1 OPTIONAL { ?s ?p ?x2 FILTER(?x) } } }", false)  ; }

    @Test public void testClassify_LeftJoin_10()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { SELECT ?s { ?s ?p ?o } } }", false)  ; }
        
    
    private void classifyLJ(String pattern, boolean expected)
    {
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(query.getQueryPattern()) ;
        
        if ( ! ( op instanceof OpLeftJoin ) )
            fail("Not a leftjoin: "+pattern) ;

        boolean nonLinear = LeftJoinClassifier.isLinear((OpLeftJoin)op) ;
        assertEquals("LeftJoin: "+pattern, expected, nonLinear) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
