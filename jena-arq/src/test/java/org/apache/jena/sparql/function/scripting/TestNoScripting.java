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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestNoScripting {
    private static Context ctx = ARQ.getContext();

    private String language;
    private String library;
    private String functions;

//    @BeforeClass public static void enableScripting() {
//        System.setProperty(ScriptFunction.systemPropertyScripting, "true");
//    }
//
//    @AfterClass public static void disbleScripting() {
//        System.clearProperty(ScriptFunction.systemPropertyScripting);
//    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "js", "testing/ARQ/Scripting/test-library.js",
                        "function toCamelCase(str) { return str.split(' ').map(cc).join('');}\n"
                        + "function ucFirst(word)    { return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();}\n"
                        + "function lcFirst(word)    { return word.toLowerCase(); }\n"
                        + "function cc(word,index)   { return (index == 0) ? lcFirst(word) : ucFirst(word); }\n" }
                , {"python", "testing/ARQ/Scripting/test-library.py",
                        "def toCamelCase(str):\n" +
                        "  return ''.join([cc(word, index) for index, word in enumerate(str.split(' '))])\n" +
                        "def ucFirst(word):\n" +
                        "  return word[0].upper() + word[1:].lower()\n" +
                        "def lcFirst(word):\n" +
                        "  return word.lower()\n" +
                        "def cc(word,index):\n" +
                        "  if index == 0:\n" +
                        "    return lcFirst(word)\n" +
                        "  return ucFirst(word)\n" }
        });
    }


    public TestNoScripting(String language, String library, String functions) {
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

    @Test(expected = ExprException.class)
    public void script_dt_boolean() {
        // Scripting not enabled.
        NodeValue nv = eval("rtnBoolean");
        assertTrue(nv.isBoolean());
    }

    private NodeValue eval(String fn, String ...args) {
        NodeValue[] nvs = new NodeValue[args.length];
        for ( int i = 0 ; i < args.length ; i++ ) {
            nvs[i] = nv(args[i]);
        }
        ScriptFunction f = new ScriptFunction();
        f.build( "http://jena.apache.org/ARQ/" + language + "Function#" + fn, null);
        return f.exec(Arrays.asList(nvs));
    }

    private static NodeValue nv(String str) {
        return NodeValue.makeNode(SSE.parseNode(str));
    }
}
