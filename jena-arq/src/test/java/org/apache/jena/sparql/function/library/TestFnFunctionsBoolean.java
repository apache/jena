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

package org.apache.jena.sparql.function.library;

import static org.apache.jena.sparql.expr.NodeValue.FALSE;
import static org.apache.jena.sparql.expr.NodeValue.TRUE;
import static org.apache.jena.sparql.expr.LibTestExpr.test;

import org.apache.jena.sys.JenaSystem ;
import org.junit.Test ;

public class TestFnFunctionsBoolean {

    static { JenaSystem.init(); }

    @Test public void exprBoolean1()    { test("fn:boolean('')", FALSE) ; }
    @Test public void exprBoolean2()    { test("fn:boolean(0)", FALSE) ; }
    @Test public void exprBoolean3()    { test("fn:boolean(''^^xsd:string)", FALSE) ; }

    @Test public void exprBoolean4()    { test("fn:boolean('X')", TRUE) ; }
    @Test public void exprBoolean5()    { test("fn:boolean('X'^^xsd:string)", TRUE) ; }
    @Test public void exprBoolean6()    { test("fn:boolean(1)", TRUE) ; }

    @Test public void exprBoolean7()    { test("fn:not('')", TRUE) ; }
    @Test public void exprBoolean8()    { test("fn:not('X')", FALSE) ; }
    @Test public void exprBoolean9()    { test("fn:not(1)", FALSE) ; }
    @Test public void exprBoolean10()   { test("fn:not(0)", TRUE) ; }
}
