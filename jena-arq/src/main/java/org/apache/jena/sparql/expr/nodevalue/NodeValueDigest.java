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

package org.apache.jena.sparql.expr.nodevalue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeValueDigest {

    /**
     * Calculate digest of a string literals. Pass in the digest name exactly, upper
     * case and with a dash e.g. "SHA-1")
     */
    public static NodeValue calculateDigest(NodeValue nv, String digestName) {
        // Exact digest name.
        MessageDigest digest = createDigest(digestName);
        return calculate(nv, digest);
    }

    /**
     * Calculate digest of a string literals by function name or namespace fragment
     * without the @code #}.
     */
    public static NodeValue function(NodeValue nv, String functionName) {
        if ( !digestFunctionName.contains(functionName) )
            throw new ExprEvalException("Digest not supported: " + functionName);

        functionName = Lib.lowercase(functionName);
        String digestName = translateDigestFunctionNames.get(functionName);
        return calculateDigest(nv, digestName);
    }

    // SPARQL name (lower case) to Java digest name.
    // @formatter:off
    private static Map<String, String> translateDigestFunctionNames = Map.of("md5",     "MD-5",
                                                                             "sha1",    "SHA-1",
                                                                             "sha224",  "SHA-224",
                                                                             "sha256",  "SHA-256",
                                                                             "sha384",  "SHA-384",
                                                                             "sha512",  "SHA-512");
    // @formatter:on

    private static Set<String> digestFunctionName = Set.of("md5", "sha1", "sha224", "sha256", "sha384", "sha512");

    private static Cache<String, MessageDigest> digtests = CacheFactory.createCache(20);

    private static MessageDigest getDigest(String digestName) {
        MessageDigest digest = digtests.get(digestName, NodeValueDigest::createDigest);
        MessageDigest digest2 = null;
        try {
            digest2 = (MessageDigest)digest.clone();
            return digest2;
        } catch (CloneNotSupportedException ex) {
            // This should not happen. All the supported digests are cloneable.
            digtests.remove(digestName);
            return digest;
        }
    }

    private static MessageDigest createDigest(String digestName) {
        try {
            return MessageDigest.getInstance(digestName);
        } catch (Exception ex2) {
            throw new ARQInternalErrorException("Digest not provided in this Java system: " + digestName);
        }
    }

    private static NodeValue calculate(NodeValue nv, MessageDigest digest) {
        Node n = nv.asNode();
        if ( !n.isLiteral() )
            throw new ExprEvalException("Not a literal: " + nv);
        if ( n.getLiteralLanguage() != null && !n.getLiteralLanguage().equals("") )
            throw new ExprEvalException("Can't make a digest of an RDF term with a language tag");
        // Literal, no language tag.
        if ( n.getLiteralDatatype() != null && !XSDDatatype.XSDstring.equals(n.getLiteralDatatype()) )
            throw new ExprEvalException("Not a simple literal nor an XSD string");
        try {
            String x = n.getLiteralLexicalForm();
            byte b[] = x.getBytes(StandardCharsets.UTF_8);
            byte d[] = digest.digest(b);
            String y = Bytes.asHexLC(d);
            NodeValue result = NodeValue.makeString(y);
            return result;
        } catch (Exception ex2) {
            throw new ARQInternalErrorException(ex2);
        }
    }
}
