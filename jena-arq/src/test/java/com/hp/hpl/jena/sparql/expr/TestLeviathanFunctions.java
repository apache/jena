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

package com.hp.hpl.jena.sparql.expr;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.function.library.leviathan.LeviathanConstants ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestLeviathanFunctions extends BaseTest {

    static boolean warnOnBadLexicalForms = true;

    @BeforeClass
    public static void beforeClass() {
        warnOnBadLexicalForms = NodeValue.VerboseWarnings;
        NodeValue.VerboseWarnings = false;
    }

    @AfterClass
    public static void afterClass() {
        NodeValue.VerboseWarnings = warnOnBadLexicalForms;
    }

    private static PrefixMapping pmap = new PrefixMappingImpl();
    private static SerializationContext serContext = new SerializationContext(false);

    static {
        pmap.setNsPrefixes(ARQConstants.getGlobalPrefixMap());
        pmap.setNsPrefix("lfn", LeviathanConstants.LeviathanFunctionLibraryURI);
    }

    @Test
    public void sq_01() {
        test("lfn:sq(2)", "4");
    }

    @Test
    public void sq_02() {
        test("lfn:sq(3)", "9");
    }

    @Test
    public void sq_03() {
        test("lfn:sq(0.5)", "0.25");
    }

    @Test
    public void cube_01() {
        test("lfn:cube(2)", "8");
    }

    @Test
    public void cube_02() {
        test("lfn:cube(3)", "27");
    }

    @Test
    public void cube_03() {
        test("lfn:cube(0.5)", "0.125");
    }

    @Test
    public void e_01() {
        test("lfn:e(2)", NodeFactoryExtra.doubleToNode(Math.exp(2d)));
    }
    
    @Test
    public void pow_01() {
        test("lfn:pow(2, 4)", "16");
    }
    
    @Test
    public void pow_02() {
        test("lfn:pow(0.5, 3)", "0.125");
    }
    
    private static void test(String string, String result) {
        Node r = NodeFactoryExtra.parseNode(result);
        test(string, r);
    }

    private static void test(String string, Node result) {
        Expr expr = ExprUtils.parse(string, pmap);
        NodeValue nv = expr.eval(null, new FunctionEnvBase());
        NodeValue nvr = NodeValue.makeNode(result);

        // Note that we don't test lexical form because we can get mismatches
        // between how things like doubles are expressed
        assertTrue("Not same value: Expected: " + nvr + " : Actual = " + nv, NodeValue.sameAs(nvr, nv));
    }

}
