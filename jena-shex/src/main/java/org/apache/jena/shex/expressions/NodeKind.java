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

package org.apache.jena.shex.expressions;

import java.util.Locale;

import org.apache.jena.shex.ShexException;

public enum NodeKind {
    IRI("IRI"), BNODE("BNode"), NONLITERAL("NonLiteral"), LITERAL("Literal"), TRIPLE("Triple");

    private final String label;
    private final String ucLabel;

    NodeKind(String string) {
        this.label = string;
        this.ucLabel = string.toUpperCase(Locale.ROOT);
    }

    public static NodeKind create(String nodeKind) {
        switch(nodeKind.toLowerCase()) {
            case "iri":         return IRI;
            case "bnode":       return BNODE;
            case "literal":     return LITERAL;
            case "nonliteral":  return NONLITERAL;
            case "triple":      return TRIPLE;
            default:
                throw new ShexException("NodeKind not recognized: '"+nodeKind+"'");
        }
    }

    public String label() {
        // Print label.
        return ucLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
