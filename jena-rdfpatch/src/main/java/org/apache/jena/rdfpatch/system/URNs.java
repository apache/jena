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

package org.apache.jena.rdfpatch.system;

import java.util.UUID;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

// Helper code to go along with Ids.
/**
 * Some functions to do with URN {@link Node Nodes}.
 * All UUID generation should go though this class.
 */
public class URNs {
    public static final String SchemeUuid = "uuid:";
    public static final String SchemeUrnUuid = "urn:uuid:";

    private static final String SCHEME = SchemeUuid;
    // Version 1 are guessable.
    // Version 4 are not.
    // Version 7 (https://ietf-wg-uuidrev.github.io/rfc4122bis/draft-00/draft-ietf-uuidrev-rfc4122bis.html updates RFC 4122)

    /** Generate a UUID */
    public static UUID genUUID() { return UUID.randomUUID() ; }

    /**
     * This is <i>not</i> a function!
     * It returns a fresh UUID URI on every call.
     */
    public static Node unique() {
        return unique(SCHEME);
    }

    /**
     * This is <i>not</i> a function!
     * It returns a fresh UUID URI with the given scheme name on every call.
     */
    public static Node unique(String schemeName) {
        return NodeFactory.createURI(schemeName+genUUID().toString());
    }
}
