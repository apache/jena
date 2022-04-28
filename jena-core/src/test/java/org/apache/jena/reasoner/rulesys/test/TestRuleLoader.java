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

package org.apache.jena.reasoner.rulesys.test;

import org.apache.jena.reasoner.rulesys.BuiltinRegistry;
import org.apache.jena.reasoner.rulesys.MapBuiltinRegistry;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.apache.jena.shared.RulesetNotFoundException;
import org.apache.jena.shared.WrappedIOException;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Tests for the rule loader
 */
public class TestRuleLoader  {

    private static BuiltinRegistry createBuiltinRegistry() {
        BuiltinRegistry br = new MapBuiltinRegistry();
        br.register(new BaseBuiltin() {
            @Override
            public String getName() {
                return "customBuiltin";
            }
        });
        return br;
    }

    @Test(expected=RulesetNotFoundException.class)
    public void load_from_file_uri_non_existent() {
        Rule.rulesFromURL("file:///no-such-file.txt");
    }

    @Test
    public void load_from_file_with_include_uri_non_existent() {
        RulesetNotFoundException e = assertThrows(RulesetNotFoundException.class,
                () -> Rule.rulesFromURL("testing/reasoners/rules/include-test-not-found.rules"));
        assertEquals("file:testing/reasoners/includeAlt.rules", e.getURI());
    }

    @Test(expected=WrappedIOException.class)
    public void load_from_file_bad_encoding() {
        Rule.rulesFromURL("testing/reasoners/bugs/bad-encoding.rules");
    }

    /**
     * Test that {@link Rule#rulesFromURL(String, BuiltinRegistry)} uses the builtin registry argument given.
     */
    @Test
    public void load_from_file_with_custom_builtins() {
        BuiltinRegistry br = createBuiltinRegistry();
        List<Rule> rules = Rule.rulesFromURL("testing/reasoners/bugs/custom-builtins.rules", br);
        assertEquals(List.of("ruleWithBuiltin"), rules.stream().map(Rule::getName).collect(Collectors.toList()));
    }
}
