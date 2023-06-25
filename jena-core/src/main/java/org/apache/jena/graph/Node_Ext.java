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

import java.util.Objects;

/** Extension to the RDF Data model.
 *  This class and any subclasses exist for experimentation and custom extensions.
 *  There is no support for them within Apache Jena.
 *  <p>
 *  Extension nodes exist so that the machinery of datastructures (graphs, triples)
 *  can be used.  There is no guarantee that processing Nodes (e.g. writing) will handle
 *  extensions.
 */
public abstract class Node_Ext<X> extends Node {

    private final X object;

    protected Node_Ext(X object) {
        this.object = Objects.requireNonNull(object);
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        return null;
    }

    // If the extension wants the node to match in memory graphs, this must be true
    // and extension type X must support value-based .equals() and .hashCode().
    @Override
    public boolean isConcrete() {
        return false;
    }

    public X get() {
        return object;
    }

    @Override
    public boolean isExt() {
        return true;
    }

    @Override
    public int hashCode() {
        if ( object == null )
            return Node.hashExt;
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Node_Ext<?> other = (Node_Ext<?>)obj;
        return Objects.equals(object, other.object);
    }
}
