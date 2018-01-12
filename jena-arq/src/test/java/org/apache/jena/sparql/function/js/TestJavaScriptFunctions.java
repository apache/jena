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

package org.apache.jena.sparql.function.js;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.Test;

public class TestJavaScriptFunctions {
    static final String JS_LIB_FILE = "testing/ARQ/JS/test-library.js";
    
    static final String CamelCaseJS = 
        StrUtils.strjoinNL
         ("function toCamelCase(str) { return str.split(' ').map(cc).join('');}"
         ,"function ucFirst(word)    { return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();}"
         ,"function lcFirst(word)    { return word.toLowerCase(); }"
         ,"function cc(word,index)   { return (index == 0) ? lcFirst(word) : ucFirst(word); }"
         );

    private static EnvJavaScript setupJS() {
        Context cxt = ARQ.getContext().copy();
        cxt.set(EnvJavaScript.symJavaScriptFunctions, CamelCaseJS);
        cxt.set(EnvJavaScript.symJavaScriptLibFile, JS_LIB_FILE);
        return EnvJavaScript.create(cxt);
    }

    private EnvJavaScript envJS = setupJS();

    @Test public void js_dt_boolean() {
        NodeValue nv = eval("rtnBoolean");
        assertTrue(nv.isBoolean());
    }
    
    @Test public void js_dt_string() {
        NodeValue nv = eval("rtnString");
        assertTrue(nv.isString());
    }

    //Note: NodeValue isDouble etc means "can be used as" so NodeValue integer isDouble
    // Use assertDatatype.
    
    @Test public void js_dt_integer() {
        NodeValue nv = eval("rtnInteger");
        assertDatatype(nv, XSDDatatype.XSDinteger);
    }
    
    @Test public void js_dt_double() {
        NodeValue nv = eval("rtnDouble");
        assertDatatype(nv, XSDDatatype.XSDdouble);
    }
    
    @Test(expected=ExprEvalException.class)
    public void js_dt_undef() {
        NodeValue nv = eval("rtnUndef");
    }
    
    @Test(expected=ExprEvalException.class) 
    public void js_dt_null() {
        NodeValue nv = eval("rtnNull");
    }
    
//    @Test public void js_dt_symbol() {
//        NodeValue nv = eval("rtnSymbol");
//    }
    
    @Test public void js_dt_1() { 
        NodeValue nv = eval("identity", "'2018-01-06T17:56:41.293+00:00'^^xsd:dateTime");
    }

    @Test public void js_1() {
        NodeValue nv = eval("identity", "1");
        assertNotNull(nv);
    }

    @Test public void js_2() {
        NodeValue nv = eval("value", "<http://example/xyz>");
        assertNotNull(nv);
        assertTrue(nv.isString());
    }

    //combine dynamics.

    @Test public void js_3() {
        NodeValue nv = eval("combine", "1", "2");
        NodeValue nvx = nv("3");
        assertTrue(nv.isNumber());
        assertDatatype(nv, XSDDatatype.XSDinteger);
        assertEquals(nvx, nv);
    }

    @Test public void js_4() {
        NodeValue nv = eval("combine", "'a'", "2");
        NodeValue nvx = nv("'a2'");
        assertTrue(nv.isString());
        assertEquals(nvx, nv);
    }

    @Test public void js_5() {
        NodeValue nv = eval("combine", "2", "'a'");
        NodeValue nvx = nv("'2a'");
        assertTrue(nv.isString());
        assertEquals(nvx, nv);
    }

    @Test public void js_6() {
        NodeValue nv = eval("combine", "2", "2.5");
        NodeValue nvx = nv("4.5e0");
        assertDatatype(nv, XSDDatatype.XSDdouble);
        assertEquals(nvx, nv);
    }

    // Narrow to integer
    @Test public void js_7() {
        NodeValue nv = eval("combine", "2.5", "3.5");
        NodeValue nvx = nv("6");
        assertDatatype(nv, XSDDatatype.XSDinteger);
        assertEquals(nvx, nv);
    }
    
    @Test public void js_8() {
        NodeValue nv = eval("toCamelCase", "'abc def ghi'");
        NodeValue nvx = nv("'abcDefGhi'");
        assertDatatype(nv, XSDDatatype.XSDstring);
        assertEquals(nvx, nv);
    }
    
    private void assertDatatype(NodeValue nv, XSDDatatype xsdDatatype) {
        assertEquals(nv.asNode().getLiteralDatatype(), xsdDatatype);
    }

    @Test(expected=ExprEvalException.class)
    public void js_err_1() {
        NodeValue nv = eval("no_such_function()");
    }
    
    // Wrong number of argument is OK in JavaScript - "null" return becomes ExprEvalException.   
    @Test(expected=ExprEvalException.class)
    public void js_err_2() {
        NodeValue nv = eval("identity");
    }

    @Test
    public void js_err_3() {
        // Legal.
        NodeValue nv = eval("combine", "'a'");
        assertNotNull(nv);
    }

    @Test
    public void js_err_4() {
        // Legal.
        NodeValue nv = eval("identity", "3", "2");
        NodeValue nvx = nv("3");
        assertEquals(nvx, nv);
    }


    private NodeValue eval(String fn, String ...args) {
        NodeValue[] nvs = new NodeValue[args.length];
        for ( int i = 0 ; i < args.length ; i++ ) {
            nvs[i] = nv(args[i]);
        }
        FunctionJavaScript x = new FunctionJavaScript(fn, envJS);
        NodeValue nvr = x.exec(Arrays.asList(nvs));
        return nvr;
    }
    
    private static NodeValue nv(String str) {
        return NodeValue.makeNode(SSE.parseNode(str)); 
    }
}
