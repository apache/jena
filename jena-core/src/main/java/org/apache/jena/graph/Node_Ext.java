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
 *  Extension nodes exist so that the machinary of datastructures (graphs, triples)
 *  can be used.  There is no guarantee that processing Nodes (e.g. writing) will handle
 *  extensions. For the usual RDF syntaxes, {@code NodeExt} are not handled. 
 */
public abstract class Node_Ext<X> extends Node {

    Node_Ext(X label) {
        super(label);
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        return null;
    }

    @Override
    public boolean isConcrete() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public X get() {
        return (X)label;
    }

    // Super is OK : it is based on label. 
//    @Override
//    public int hashCode() {
//    }

    @Override
    public boolean equals(Object o) {
        if ( o == this )
            return true;
        if ( !(o instanceof Node_Ext<? >) )
            return false;
        @SuppressWarnings("unchecked")
        Node_Ext<X> other = (Node_Ext<X>)o;
        return Objects.equals(this.get(), other.get());
    }
}
