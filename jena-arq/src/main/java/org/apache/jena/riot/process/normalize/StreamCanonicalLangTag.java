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

package org.apache.jena.riot.process.normalize;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.function.BiFunction;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.process.StreamRDFApplyObject;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.NodeUtils;

/** {@link StreamRDF} that converts language tags to lower case o rto canokcal form (RFC 4646). */
public class StreamCanonicalLangTag extends StreamRDFApplyObject {
    /** Return a {@link StreamRDF} that converts language tags to lower case */ 
    public static StreamRDF toLC(StreamRDF other) {
        Locale.Builder locBuild = new Locale.Builder();
        return new StreamCanonicalLangTag(other, locBuild,
                                          (b,n) -> canonical(locBuild, n, StreamCanonicalLangTag::langTagLC));
    }
    
    /** Return a {@link StreamRDF} that converts language tags to canonical form (RFC 4646, 5646). */ 
    public static StreamRDF toCanonical(StreamRDF other) {
        Locale.Builder locBuild = new Locale.Builder();
        return new StreamCanonicalLangTag(other, locBuild, 
                                          (b,n) -> canonical(locBuild, n, StreamCanonicalLangTag::langTagCanonical));
    }

    
    
    private StreamCanonicalLangTag(StreamRDF other, Locale.Builder locBuild, BiFunction<Locale.Builder, Node, Node> stringMapper) {
        super(other, (n)->stringMapper.apply(locBuild, n));
    }
    
    private static Node canonical(Locale.Builder locBuild, Node n, BiFunction<Locale.Builder, String, String> tagMapper ) {
        // If no change, return the original object; this prevents object churn.
        if ( ! NodeUtils.hasLang(n) )
            return n;
        String langTag = n.getLiteralLanguage();
        if ( langTag == null || langTag.isEmpty() )
            return n;
        String langTag2 = tagMapper.apply(locBuild, langTag);
        if ( langTag == langTag2 )
            return n;
        Node obj2 = NodeFactory.createLiteral(n.getLiteralLexicalForm(), langTag2);
        return obj2;
    }
    
    // From taken from "xsd4ld" (which has an Apache License).
    public static String langTagCanonical(Locale.Builder locBuild, String str) {
        try {
            // Does not do conversion of language for ISO 639 codes that have changed.
            return locBuild.setLanguageTag(str).build().toLanguageTag();
        } catch (IllformedLocaleException ex) {
            return str;
        }
    }

    public static String langTagLC(Locale.Builder locBuild, String str) {
        return str.toLowerCase(Locale.ROOT);
    }
}
