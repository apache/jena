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

import org.apache.jena.shared.PrefixMapping;

/**
    RDF blank nodes, ie nodes with identity but without URIs.
*/

public class Node_Blank extends Node
{
    private final BlankNodeId blankNodeId;

    /* package */ Node_Blank( BlankNodeId id ) { this.blankNodeId = Objects.requireNonNull(id); }

    @Override
    public boolean isBlank() { return true; }

    @Override
    public boolean isConcrete() { return true; }

    @Override
    public BlankNodeId getBlankNodeId()  { return blankNodeId; }

    @Override
    public Object visitWith( NodeVisitor v )
    { return v.visitBlank( this, blankNodeId); }

    @Override
    public int hashCode() {
        return Objects.hash(blankNodeId);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Node_Blank other = (Node_Blank)obj;
        return Objects.equals(blankNodeId, other.blankNodeId);
    }

    @Override
    public String toString( PrefixMapping pmap ) { return toString(); }

    @Override
    public String toString() {
        return "_:"+blankNodeId.toString();
    }
}
