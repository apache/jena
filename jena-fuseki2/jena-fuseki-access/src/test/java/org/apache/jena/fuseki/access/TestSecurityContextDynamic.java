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

package org.apache.jena.fuseki.access;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

// Note: JUnit 4 does not support parameterized tests
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

/** Test parsing of assemblers with security aspects */
@RunWith(JUnitParamsRunner.class)
public class TestSecurityContextDynamic {

    private static final String ACL_PRAGMA = "#pragma acl.graphs";
    private static final String ONE_GRAPH_PRAGMA = ACL_PRAGMA + " some:graph";

    @Test
    @Parameters({
        // empty
        "",
        // other comments before pragma
        "# Comment\n" + ONE_GRAPH_PRAGMA,
        // pragma after grammar start
        "PREFIX ex: http://example.org/\n" + ONE_GRAPH_PRAGMA,
        // without space delimiter
        ACL_PRAGMA + "some:graph",
        // with empty graph list
        ACL_PRAGMA + " ",
    })
    public void forQuery_returns_allow_none_context_when_pragma_not_matched(String preamble) {
        final String query = "\nSELECT 1 {}";

        SecurityContext sCxt = SecurityContextDynamic.forQuery(preamble + query);

        assertEquals(SecurityContext.NONE, sCxt);
    }

    @Test
    @Parameters(method="graphValues")
    public void forQuery_returns_expected_context_when_pragma_matched(String[] graphs) {
        final String query = "\nSELECT 1 {}";

        String graphString = String.join(SecurityContextDynamic.GRAPH_PRAGMA_DELIMITER, graphs);

        SecurityContext sCxt = SecurityContextDynamic.forQuery(ACL_PRAGMA + " " + graphString + query);

        assertFalse(sCxt.visableDefaultGraph());
        assertEquals(graphs.length, sCxt.visibleGraphs().size());
        assertTrue(sCxt.visibleGraphNames().containsAll(Arrays.asList(graphs)));
    }
    private Object[] graphValues() {
        return new Object[]{
            new Object[]{new String[]{"graph:one"}},
            new Object[]{new String[]{"graph:one", "graph:two"}},
            new Object[]{new String[]{"graph:one", "graph:two", "graph:three"}},
        };
    }

}
