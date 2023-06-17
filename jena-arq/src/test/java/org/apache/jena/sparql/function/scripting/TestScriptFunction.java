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

package org.apache.jena.sparql.function.scripting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestScriptFunction {
    private static Context ctx = ARQ.getContext();

    private String language;
    private String library;
    private String functions;

    @BeforeClass public static void enableScripting() {
        System.setProperty(ARQ.systemPropertyScripting, "true");
    }

    @AfterClass public static void disableScripting() {
        System.clearProperty(ARQ.systemPropertyScripting);
    }

    /*package*/ static String DIR = "testing/ARQ/Scripting";

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "js", DIR+"/test-library.js"
                      , "function toCamelCase(str) { return str.split(' ').map(cc).join('');}\n"
                      + "function ucFirst(word)    { return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();}\n"
                      + "function lcFirst(word)    { return word.toLowerCase(); }\n"
                      + "function cc(word,index)   { return (index == 0) ? lcFirst(word) : ucFirst(word); }\n" }
                , {"python", DIR+"/test-library.py"
                        , "def toCamelCase(str):\n"
                        + "  return ''.join([cc(word, index) for index, word in enumerate(str.split(' '))])\n"
                        + "def ucFirst(word):\n"
                        + "  return word[0].upper() + word[1:].lower()\n"
                        + "def lcFirst(word):\n"
                        + "  return word.lower()\n"
                        + "def cc(word,index):\n"
                        + "  if index == 0:\n"
                        + "    return lcFirst(word)\n"
                        + "  return ucFirst(word)\n" }
        });
    }

    // Script library functions (JS and Python)
    private static String[] testLibFunctions = {
        "bar",
        "value",
        "combine",
        "identity",
        "rtnBoolean",
        "rtnString",
        "rtnInteger",
        "rtnDouble",
        "rtnUndef",
        "rtnNull",
        // Entry point from text defintions above. And no others.
        "toCamelCase"
    };

    /*package*/ static String testLibAllow = String.join(",", testLibFunctions);
    private static Context context = new Context().set(ARQ.symCustomFunctionScriptAllowList, testLibAllow);

    public TestScriptFunction(String language, String library, String functions) {
        this.language = language;
        this.library = library;
        this.functions = functions;
    }

    @Before
    public void setup() {
        ctx.set(ScriptLangSymbols.scriptLibrary(language), library);
        ctx.set(ScriptLangSymbols.scriptFunctions(language), functions);
    }

    @After
    public void teardown() {
        ctx.unset(ScriptLangSymbols.scriptFunctions(language));
        ctx.unset(ScriptLangSymbols.scriptLibrary(language));
        ScriptFunction.clearEngineCache();
    }

    @Test
    public void script_dt_boolean() {
        NodeValue nv = eval("rtnBoolean");
        assertTrue(nv.isBoolean());
    }

    @Test public void script_dt_string() {
        NodeValue nv = eval("rtnString");
        assertTrue(nv.isString());
    }

    //Note: NodeValue isDouble etc means "can be used as" so NodeValue integer isDouble
    // Use assertDatatype.

    @Test public void script_dt_integer() {
        NodeValue nv = eval("rtnInteger");
        assertDatatype(nv, XSDDatatype.XSDinteger);
    }

    @Test public void script_dt_double() {
        NodeValue nv = eval("rtnDouble");
        assertDatatype(nv, XSDDatatype.XSDdouble);
    }

    @Test(expected= ExprEvalException.class)
    public void script_dt_undef() {
        NodeValue nv = eval("rtnUndef");
    }

    @Test(expected=ExprEvalException.class)
    public void script_dt_null() {
        NodeValue nv = eval("rtnNull");
    }

//    @Test public void script_dt_symbol() {
//        NodeValue nv = eval("rtnSymbol");
//    }

    @Test public void script_dt_1() {
        NodeValue nv = eval("identity", "'2018-01-06T17:56:41.293+00:00'^^xsd:dateTime");
    }

    @Test public void script_1() {
        NodeValue nv = eval("identity", "1");
        assertNotNull(nv);
    }

    @Test public void script_2() {
        NodeValue nv = eval("value", "<http://example/xyz>");
        assertNotNull(nv);
        assertTrue(nv.isString());
    }

    // combine dynamics.

    @Test public void script_3() {
        NodeValue nv = eval("combine", "1", "2");
        NodeValue nvx = nv("3");
        assertTrue(nv.isNumber());
        assertDatatype(nv, XSDDatatype.XSDinteger);
        assertEquals(nvx, nv);
    }

    @Test public void script_4() {
        NodeValue nv = eval("combine", "'a'", "2");
        NodeValue nvx = nv("'a2'");
        assertTrue(nv.isString());
        assertEquals(nvx, nv);
    }

    @Test public void script_5() {
        NodeValue nv = eval("combine", "2", "'a'");
        NodeValue nvx = nv("'2a'");
        assertTrue(nv.isString());
        assertEquals(nvx, nv);
    }

    @Test public void script_6() {
        NodeValue nv = eval("combine", "2", "2.5");
        NodeValue nvx = nv("4.5e0");
        assertDatatype(nv, XSDDatatype.XSDdouble);
        assertEquals(nvx, nv);
    }

    // Narrow to integer
    @Test public void script_7() {
        NodeValue nv = eval("combine", "2.5", "3.5");
        NodeValue nvx = nv("6");
        assertDatatype(nv, XSDDatatype.XSDinteger);
        assertEquals(nvx, nv);
    }

    @Test public void script_8() {
        NodeValue nv = eval("bar", "'abc'", "'def'");
        NodeValue nvx = nv("'||abcdef||'");
        assertTrue(nv.isString());
        assertEquals(nvx, nv);
    }

    @Test public void script_10() {
        NodeValue nv = eval("toCamelCase", "'abc def ghi'");
        NodeValue nvx = nv("'abcDefGhi'");
        assertDatatype(nv, XSDDatatype.XSDstring);
        assertEquals(nvx, nv);
    }

    private void assertDatatype(NodeValue nv, XSDDatatype xsdDatatype) {
        assertEquals(nv.asNode().getLiteralDatatype(), xsdDatatype);
    }

    @Test(expected=ExprException.class)
    public void script_err_1() {
        NodeValue nv = eval("no_such_function");
    }

    // Wrong number of argument is OK in JavaScript - "null" return becomes ExprEvalException.
    @Test(expected=ExprEvalException.class)
    public void script_err_2() {
        NodeValue nv = eval("identity");
    }

    @Test
    public void script_err_3() {
        if (!language.equalsIgnoreCase("JS")) {
            return;
        }
        // Legal in JS.
        NodeValue nv = eval("combine", "'a'");
        assertNotNull(nv);
    }

    @Test
    public void script_err_4() {
        if (!language.equalsIgnoreCase("JS")) {
            return;
        }
        // Legal in JS.
        NodeValue nv = eval("identity", "3", "2");
        NodeValue nvx = nv("3");
        assertEquals(nvx, nv);
    }

    @Test(expected=ScriptDenyException.class)
    public void script_sparql_bad_1() {
        Query query = QueryFactory.read(DIR+"/js-query-5.rq");
        QueryExec qExec = QueryExec.dataset(DatasetGraphFactory.empty()).query(query).build();
        // Exception happens here during query build time, which is the start of execution.
        qExec.select();
    }

    @Test
    public void script_sparql_bad_2() {
        Query query = QueryFactory.read(DIR+"/js-query-5.rq");
        QueryExec qExec = QueryExec.dataset(DatasetGraphFactory.empty()).query(query).build();
        // No exception
    }

    // --- Test System property.

    private static void execScriptable(String value, Runnable action) {
        String x = System.getProperty(ARQ.systemPropertyScripting);
        try {
            if ( value == null )
                System.clearProperty(ARQ.systemPropertyScripting);
            else
                System.setProperty(ARQ.systemPropertyScripting, value);
            action.run();
        } finally {
            System.setProperty(ARQ.systemPropertyScripting, x);
        }
    }

    @Test(expected=ExprException.class)
    public void scripting_not_enabled_1() {
        execScriptable(null, ()->{
            NodeValue nv = eval("identity", "1");
            assertNotNull(nv);
        });
    }

    @Test(expected=ExprException.class)
    public void scripting_not_enabled_2() {
        execScriptable("false", ()->{
            NodeValue nv = eval("identity", "1");
            assertNotNull(nv);
        });
    }

    @Test
    public void scripting_enabled_1() {
        execScriptable("true", ()->{
            NodeValue nv = eval("identity", "1");
            assertNotNull(nv);
        });
    }

    private NodeValue eval(String fn, String ...args) {
        NodeValue[] nvs = new NodeValue[args.length];
        for ( int i = 0 ; i < args.length ; i++ ) {
            nvs[i] = nv(args[i]);
        }
        ScriptFunction f = new ScriptFunction();
        String functionURI = "http://jena.apache.org/ARQ/" + language + "Function#" + fn;

        f.build( functionURI, null, context);
        return f.exec(Arrays.asList(nvs));
    }

    private static NodeValue nv(String str) {
        return NodeValue.makeNode(SSE.parseNode(str));
    }
}
