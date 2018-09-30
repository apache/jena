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

package org.apache.jena.sparql.syntax.syntaxtransform;

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.junit.Test ;

public class TestFlattenSyntax {
    static String PRE = "PREFIX : <http://example/>\n" ;
    
    @Test public void test_flatten_basic_01() 
    { test(":s0 :p :o .", null) ; }

    @Test public void test_flatten_basic_02()
    { test("{ :s1 :p :o }", ":s1 :p :o") ; }

    @Test public void test_flatten_basic_03()
    { test("{{ :s2 :p :o }}", ":s2 :p :o") ; }

    @Test public void test_flatten_basic_04()
    { test("{{{ :s3 :p :o }}}", ":s3 :p :o") ;  }

    @Test public void test_flatten_filter_01() 
    { test(":s0 :p :o .{FILTER(?x)}", null) ; }

    @Test public void test_flatten_fileter_02()
    { test("{ :s1 :p :o {FILTER(?x)} }", ":s1 :p :o {FILTER(?x)}") ; }

    @Test public void test_flatten_filter_03()
    { test("{{ :s1 :p :o {FILTER(?x)}}}", " :s1 :p :o {FILTER(?x)}") ; }

    @Test public void test_flatten_optional_01()
    { test("OPTIONAL{ ?s1 :q ?z }", null) ;  }
    
    @Test public void test_flatten_optional_02()
    { test("OPTIONAL{{?s2 :q ?z}}", "OPTIONAL{?s2 :q ?z}") ;  }
    
    @Test public void test_flatten_optional_03()
    { test("OPTIONAL{?s1f :q ?z FILTER(?z) }", null) ;  }
    
    @Test public void test_flatten_optional_04()
    { test("OPTIONAL{{?S2 :q ?z FILTER(?z) }}", null);  }
    
    @Test public void test_flatten_optional_05()
    { test("OPTIONAL{{{?S3 :q ?z FILTER(?z) }}}", "OPTIONAL{{?S3 :q ?z FILTER(?z) }}") ; }
    
    @Test public void test_flatten_optional_06()
    { test("OPTIONAL{?sx :q ?z {FILTER(?z)} }", null) ;  }

    @Test public void test_flatten_pattern_01()
    { test("{?s :q ?z } UNION {?s :q ?z }", null) ;  }

    @Test public void test_flatten_pattern_02()
    { test("{{?s :q ?z}} UNION {?s :q ?z }", "{?s :q ?z} UNION {?s :q ?z }") ;  }
    
    @Test public void test_flatten_pattern_03()
    { test("{ ?s :q ?z} UNION {{?s :q ?z}}", "{?s :q ?z} UNION {?s :q ?z }") ;  }

    @Test public void test_flatten_pattern_04()
    { test("{{ ?s :q ?z } UNION {{?s :q ?z}}}", "{?s :q ?z} UNION {?s :q ?z }") ;  }

    @Test public void test_flatten_expr_01()
    { test("FILTER EXISTS { :s :p :o }", null) ;  }

    @Test public void test_flatten_expr_02()
    { test("FILTER EXISTS {{ :s :p :o }}", "FILTER EXISTS { :s :p :o }") ;  }

    @Test public void test_flatten_arq_01()
    { test("NOT EXISTS {{ :s :p :o FILTER(1) }}", "NOT EXISTS { :s :p :o  FILTER(1)}") ;  }

    @Test public void test_flatten_arq_02()
    { test("EXISTS {{ :s :p :o }}", "EXISTS { :s :p :o }") ;  }
    
    private static void test(String input, String expected) {
        if ( expected == null )
            expected = input ;
        String qs = gen(PRE, input) ;
        String qsExpected = gen(PRE, expected) ;
        
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Query query2 = QueryTransformOps.transform(query, new ElementTransformCleanGroupsOfOne()) ;
        Query queryExpected = QueryFactory.create(qsExpected, Syntax.syntaxARQ) ;
        
        Op op1 = Algebra.compile(query) ;
        Op op2 = Algebra.compile(query2) ;
        assertEquals("Algebra different", op1, op2) ;
        
        boolean modified = ! query.equals(query2) ;
        boolean expectModification = !queryExpected.equals(query) ;
        assertEquals("Expect query modifed?", expectModification, modified) ;
    }
    
    private static String gen(String PRE, String string) {
        return PRE+"\nSELECT * { "+string+"\n}" ;
    }
}

