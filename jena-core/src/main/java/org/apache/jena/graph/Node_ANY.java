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

package org.apache.jena.graph;

import org.apache.jena.shared.PrefixMapping;

/**
    A Node_ANY (there should be only one) is a meta-node that is used to stand
    for any other node in a query.
*/

public class Node_ANY extends Node {

    /*package*/ static final Node_ANY nodeANY = new Node_ANY();

    private Node_ANY() {}

    @Override
    public boolean isConcrete() { return false; }

    @Override
    public int hashCode() {
        return Node.hashANY;
    }

    @Override
    public boolean equals(Object other) {
        // This is only one such object.
        if ( this == other )
            return true;
        return false;
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        return v.visitAny(this);
    }

    @Override
    public boolean matches(Node other) {
        return other != null;
    }

    @Override
    public String toString( PrefixMapping pmap ) { return toString(); }

    @Override
    public String toString() {
        return "ANY";
    }
}
