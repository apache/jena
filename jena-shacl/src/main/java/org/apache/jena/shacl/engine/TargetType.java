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

package org.apache.jena.shacl.engine;

import org.apache.jena.graph.Node;
import org.apache.jena.shacl.vocabulary.SHACL;

public enum TargetType {
    targetNode(SHACL.targetNode, "targetNode"),
    targetClass(SHACL.targetClass, "targetClass"),
    targetObjectsOf(SHACL.targetObjectsOf, "targetObjectsOf"),
    targetSubjectsOf(SHACL.targetSubjectsOf, "targetSubjectsOf"),
    implicitClass(null,null),
    // Any use of sh:target
    targetExtension(SHACL.target, null);

    public final Node predicate;
    public final String compact;

    TargetType(Node n, String compactWord) {
        this.predicate = n;
        this.compact = compactWord;
    }
}
