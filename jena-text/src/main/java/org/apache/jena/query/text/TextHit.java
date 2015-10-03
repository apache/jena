/**
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

package org.apache.jena.query.text ;

import org.apache.jena.graph.Node ;

/** Class representing a single hit from a jena-text index */ 
public class TextHit
{
    private Node node;
    private float score;
    private Node literal;

    public TextHit(Node node, float score, Node literal) {
        this.node = node;
        this.score = score;
        this.literal = literal;
    }

    public Node getNode() {
        return this.node;
    }
    
    public float getScore() {
        return this.score;
    }

    public Node getLiteral() {
        return this.literal;
    }
}
