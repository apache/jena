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

package org.apache.jena.rfc3986;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class TestParseURNComponents {

    @Test public void urn_components_01() { testURNComponents("?+abc", "abc", null, null); }
    @Test public void urn_components_02() { testURNComponents("?+abc?=def", "abc", "def", null); }
    @Test public void urn_components_03() { testURNComponents("?+abc?=def#xyz", "abc", "def", "xyz"); }

    // Corner case: two '?+' The first starts an r-compoent that includes the second ?+ and its characters.
    @Test public void urn_components_04() { testURNComponents("?+abc?+def", "abc?+def", null, null); }

    // Corner case: two '?='
    @Test public void urn_components_05() { testURNComponents("?=abc?=def", null, "abc?=def", null); }

    // Corner case: there is a q-component and it includes the out-of-place r-component character.
    @Test public void urn_components_06() { testURNComponents("?=abc?+def", null, "abc?+def", null); }

    @Test public void urn_components_07() { testURNComponents("#", null, null, ""); }
    @Test public void urn_components_08() { testURNComponents("#frag", null, null, "frag"); }

    @Test public void urn_components_bad_01() { testURNComponentsBad("?not_urn"); }

    @Test public void urn_components_bad_02() { testURNComponentsBad("?#frag"); }

    @Test public void urn_components_bad_03() { testURNComponentsBad("?+"); }
    @Test public void urn_components_bad_04() { testURNComponentsBad("?+#frag"); }
    @Test public void urn_components_bad_05() { testURNComponentsBad("?+abc?="); }
    @Test public void urn_components_bad_06() { testURNComponentsBad("?+abc?=#frag"); }
    @Test public void urn_components_bad_07() { testURNComponentsBad("?="); }

    private static void testURNComponents(String compStr, String rComp, String qComp, String fComp) {
        URNComponents components = ParseURNComponents.parseURNComponents(compStr);
        testComponent("r-component", rComp, components.rComponent());
        testComponent("q-component", qComp, components.qComponent());
        testComponent("f-component", fComp, components.fComponent());

        URNComponents componentsRegex = ParseURNComponents.parseURNcomponentsRegex(compStr);
        if ( componentsRegex == null )
            fail("No match: "+compStr);
        testComponent("r-component", rComp, componentsRegex.rComponent());
        testComponent("q-component", qComp, componentsRegex.qComponent());
        testComponent("f-component", fComp, componentsRegex.fComponent());
    }

    private static void testComponent(String componentName, String expected, String actual) {
        if ( expected == null && actual == null )
            return;
        assertEquals(expected, actual, componentName);
    }

    private void testURNComponentsBad(String compStr) {
        URNComponents components = ParseURNComponents.parseURNComponents(compStr);
        URNComponents components2 = ParseURNComponents.parseURNcomponentsRegex(compStr);
        if ( components == null ) {
            if ( components2 == null ) {
                return;
            }
            fail(format("Regex Different: Expected=null, Actual=(%s,%s, %s)\n",
                        components2.rComponent(), components2.qComponent(), components2.fComponent()));

        }
        fail(format("Different: Expected=null, Actual=(%s,%s, %s)\n",
                    components.rComponent(), components.qComponent(), components.fComponent()));
    }
}

