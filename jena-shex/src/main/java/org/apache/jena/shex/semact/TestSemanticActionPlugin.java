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

package org.apache.jena.shex.semact;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.expressions.SemAct;
import org.apache.jena.shex.expressions.ShapeExpression;
import org.apache.jena.shex.expressions.TripleExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface ExtractVar {
    String run (String str);
}

public class TestSemanticActionPlugin implements SemanticActionPlugin {
    static String SemActIri = "http://shex.io/extensions/Test/";
    static Pattern ParsePattern, LeadPattern, LastPattern;

    static {
        String term = "(\\\"(?:(?:[^\\\\\\\"]|\\\\[^\\\"])+)\\\"|[spo])";
        ParsePattern = Pattern.compile("^ *(fail|print) *\\(((?:" + term + ", )*" + term + ")\\) *$");
        LeadPattern = Pattern.compile(term + ", ");
        LastPattern = Pattern.compile("((" + term + "))");
    }

    static Pattern ParsePatter1 = Pattern.compile("^ *(fail|print) *\\((\\\"(?:(?:[^\\\\\\\"]|\\\\[^\\\"])+)\\\"|[spo])\\) *$");

    @Override
    public List<String> getUris() {
        List<String> uris = new ArrayList<>();
        uris.add(SemActIri);
        return uris;
    }

    List<String> out = new ArrayList<>();

    public List<String> getOut () { return out; }

    @Override
    public boolean evaluateStart(SemAct semAct, ShexSchema schema) {
        return parse(semAct, (str) -> resolveStartVar(str));
    }

    @Override
    public boolean evaluateShapeExpr(SemAct semAct, ShapeExpression shapeExpression, Node focus) {
        return parse(semAct, (str) -> resolveNodeVar(str, focus));
    }

    @Override
    public boolean evaluateTripleExpr(SemAct semAct, TripleExpression tripleExpression, Collection<Triple> triples) {
        Iterator<Triple> tripleIterator = triples.iterator();
        Triple triple = tripleIterator.hasNext() ? tripleIterator.next() : null; // should be one triple, as currently defined.
        return parse(semAct, (str) -> resolveTripleVar(str, triple));
    }

    private boolean parse(SemAct semAct, ExtractVar extractor) {
        String code = semAct.getCode();
        if (code == null)
            throw new RuntimeException(String.format("%s semantic action should not be null", SemActIri));

        Matcher m = ParsePattern.matcher(code);
        if (!m.find())
            throw new RuntimeException(String.format("%s semantic action %s did not match %s", SemActIri, code, ParsePattern));
        String function = m.group(1);
        String argument = m.group(2);

        List<String> printed = new ArrayList<>();
        while((m = LeadPattern.matcher(argument)).find()) {
            printed.add(extractor.run(m.group(1)));
            argument = argument.substring(m.end());
        }
        m = LastPattern.matcher(argument);
        m.find();
        printed.add(extractor.run(m.group(1)));
        out.add(String.join(", ", printed));
        return function.equals("fail") ? false : true;
    }

    private static String resolveStartVar(String varName) {
        if (varName.charAt(0) == '"')
            return varName.replaceAll("\\\\(.)", "$1");

        throw new RuntimeException(String.format("%s semantic action argument %s was not a literal", SemActIri, varName));
    }

    private static String resolveNodeVar(String varName, Node focus) {
        if (varName.charAt(0) == '"')
            return varName.replaceAll("\\\\(.)", "$1");

        Node pos;
        switch (varName) {
            case "s": pos = focus; break;
            default:
                throw new RuntimeException(String.format("%s semantic action argument %s was not literal or 's', 'p', or 'o'", SemActIri, varName));
        }
        return pos.toString();
    }

    private static String resolveTripleVar(String varName, Triple triple) {
        if (varName.charAt(0) == '"')
            return varName.replaceAll("\\\\(.)", "$1");

        if (triple == null)
            return null;

        Node pos;
        switch (varName) {
            case "s": pos = triple.getSubject(); break;
            case "p": pos = triple.getPredicate(); break;
            case "o": pos = triple.getObject(); break;
            default:
                throw new RuntimeException(String.format("%s semantic action argument %s was not a literal or 's', 'p', or 'o'", SemActIri, varName));
        }
        return pos.toString();
    }
}
