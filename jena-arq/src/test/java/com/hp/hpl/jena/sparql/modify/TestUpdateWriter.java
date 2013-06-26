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

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class TestUpdateWriter extends BaseTest {
    @Test public void updateWrite01()   { test("INSERT DATA {}") ; }
    @Test public void updateWrite02()   { test("PREFIX : <http://example/> INSERT DATA { <s> :p 123 }") ; }
    @Test public void updateWrite03()   { test("PREFIX : <http://example/> INSERT DATA { _:a :p 123 , 456 }") ; }
    @Test public void updateWrite04()   { test("PREFIX : <http://example/> INSERT DATA { _:a :p 123 ; :q 456 }") ; }
    @Test public void updateWrite05()   { test("DELETE {<s> <p> ?v } INSERT {<s> <p> _:x } WHERE {?v <p> _:x }") ; }
    @Test public void updateWrite06()   { test("INSERT {<s> <p> ?v } WHERE {?v <p> 123}") ; }
    @Test public void updateWrite07()   { test("DELETE {<s> <p> ?v} WHERE {?v <p> 123}") ; }
    @Test public void updateWrite08()   { test("DELETE WHERE {?v <p> 123}") ; }
    @Test public void updateWrite09()   { test("DELETE { ?w <q> 56 } INSERT {?v <p> 123} WHERE { _:a ?p _:b }") ; }
   
    @Test public void updateWrite10()   { test("INSERT {} WHERE { ?x ?p [ ?a  ?b ] }") ; }
    
    @Test public void updateWrite20()   { test("PREFIX : <http://example/> DELETE {} INSERT {} WHERE {}") ; }
    @Test public void updateWrite21()   { test("PREFIX : <http://example/> DELETE {} INSERT {} USING :G WHERE {}") ; }
    @Test public void updateWrite22()   { test("PREFIX : <http://example/> DELETE {} INSERT {} USING NAMED :GN WHERE {}") ; }
    @Test public void updateWrite23()   { test("PREFIX : <http://example/> WITH :ABC DELETE {} INSERT {} WHERE {}") ; }

    private void test(String updateString) {
        UpdateRequest update1 = UpdateFactory.create(updateString);
        IndentedLineBuffer w = new IndentedLineBuffer() ;
        UpdateWriter.output(update1, w) ;
        String s = w.asString() ;
        UpdateRequest update2 = UpdateFactory.create(s);
        assertTrue(update1.equalTo(update2)) ;
    }
}

