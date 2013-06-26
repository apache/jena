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

package com.hp.hpl.jena.sparql.modify;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class TestUpdateCompare extends BaseTest {
    @Test public void updateCompare01()     { test("INSERT DATA {}") ; }
    @Test public void updateCompare02()     { test("INSERT DATA {<s> <p> <o>}") ; }
    @Test public void updateCompare03()     { test("INSERT DATA {<s> <p> _:a}", "INSERT DATA {<s> <p> _:b}") ; }
    @Test public void updateCompare04()     { test("INSERT DATA {<s> <p> _:a ; <p> 123 }", "INSERT DATA {<s> <p> _:b . <s> <p> 123 }") ; }
    @Test public void updateCompare05()     { test("INSERT DATA {<s> <p> _:a ; <p> 123 }", "INSERT DATA {<s> <p> 123 . <s> <p> _:b .  }", false) ; }

    @Test public void updateCompare06()     { test("DELETE DATA {}", "INSERT DATA {}", false) ; }
    @Test public void updateCompare07()     { test("DELETE DATA {<s> <p> <o>}", "DELETE DATA {<s> <p> <o>}") ; }
    
    @Test public void updateCompare08()     { test("DELETE {} INSERT {} WHERE {}") ; } 
    @Test public void updateCompare09()     { test("DELETE {<s> <p> ?v} INSERT {<s> <t> _:a } WHERE { <s> <p> _:a }") ; } 
    
    @Test public void updateCompare10()     { test("PREFIX : <http://example/> INSERT DATA { :s :p :o }") ; }
    @Test public void updateCompare11()     { test("PREFIX : <http://example/> INSERT DATA { :s :p :o }",
                                                   "PREFIX ex: <http://example/> INSERT DATA { ex:s ex:p ex:o }",
                                                   false) ; }
    
    @Test public void updateCompare20()     { 
        String u1 = StrUtils.strjoinNL("PREFIX : <http://example/>",
                                       "WITH :g1 INSERT { :s :p :o } WHERE {}") ;
        test(u1) ;
    }

    @Test public void updateCompare21()     { 
        String u1 = "PREFIX : <http://example/> WITH :AAA INSERT { } WHERE {}" ;
        String u2 = "PREFIX : <http://example/> WITH :ZZZ INSERT { } WHERE {}" ;
        test(u1, u2, false) ;
    }
    
    @Test public void updateCompare22()     { 
        String u1 = "PREFIX : <http://example/> DELETE { } USING :G WHERE {}" ;
        test(u1) ;
    }

    @Test public void updateCompare23()     { 
        String u1 = "PREFIX : <http://example/> DELETE { } USING :G WHERE {}" ;
        String u2 = "PREFIX : <http://example/> DELETE { } USING :X WHERE {}" ;
        test(u1, u2, false) ;
    }

    @Test public void updateCompare24()     { 
        String u1 = "PREFIX : <http://example/> DELETE { } USING NAMED :G WHERE {}" ;
        test(u1) ;
    }

    @Test public void updateCompare25()     { 
        String u1 = "PREFIX : <http://example/> DELETE { } USING NAMED :G WHERE {}" ;
        String u2 = "PREFIX : <http://example/> DELETE { } USING :G WHERE {}" ;
        test(u1, u2, false) ;
    }

    private void test(String updateString) {
        test(updateString, updateString, true) ;
    }

    private void test(String updateString1, String updateString2) {
        test(updateString1, updateString2, true) ;
    }

    private void test(String updateString1, String updateString2, boolean isomorphic) {
        UpdateRequest update1 = UpdateFactory.create(updateString1);
        UpdateRequest update2 = UpdateFactory.create(updateString2);
        test(update1, update2, isomorphic) ;
    }
        
    private void test(UpdateRequest update1, UpdateRequest update2, boolean isomorphic) {
        boolean b = UpdateCompare.isomorphic(update1, update2) ;
        assertEquals(update1.toString(), b, isomorphic) ;
    }
}

