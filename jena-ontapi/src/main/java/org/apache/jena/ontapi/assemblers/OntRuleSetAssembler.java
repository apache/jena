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

package org.apache.jena.ontapi.assemblers;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.RuleSet;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.rulesys.Rule;

import java.util.ArrayList;
import java.util.List;

public class OntRuleSetAssembler extends AssemblerBase {
    @Override
    public Object open(Assembler a, Resource root, Mode irrelevant) {
        checkType(root, OA.RuleSet);
        return createRuleSet(a, root);
    }

    public static RuleSet createRuleSet(Assembler a, Resource root) {
        return RuleSet.create(addRules(new ArrayList<>(), a, root));
    }

    public static List<Rule> addRules(List<Rule> result, Assembler a, Resource root) {
        addLiteralRules(root, result);
        addIndirectRules(a, root, result);
        addExternalRules(root, result);
        return result;
    }

    private static void addIndirectRules(Assembler a, Resource root, List<Rule> result) {
        root.listProperties(OA.rules).forEach(statement -> {
            Resource r = getResource(statement);
            result.addAll(((RuleSet) a.open(r)).getRules());
        });
    }

    static private void addExternalRules(Resource root, List<Rule> result) {
        root.listProperties(OA.rulesFrom).forEach(statement -> {
            Resource s = getResource(statement);
            result.addAll(Rule.rulesFromURL(s.getURI()));
        });
    }

    private static void addLiteralRules(Resource root, List<Rule> result) {
        root.listProperties(OA.rule).forEach(statement -> {
            String s = getString(statement);
            result.addAll(Rule.parseRules(s));
        });
    }
}
