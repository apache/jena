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

import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.* ;

/**
    RDF nodes with a global identity given by a URI.
*/
public class Node_URI extends Node
{
    private final String uriStr;

    protected Node_URI( String uri )
    { this.uriStr = Objects.requireNonNull(uri); }

    @Override
    public boolean isConcrete() { return true; }

    @Override
    public String getURI()
    { return uriStr; }

    @Override
    public Object visitWith( NodeVisitor v )
    { return v.visitURI( this, uriStr ); }

    @Override
    public boolean isURI()
    { return true; }

    @Override
    public String toString( )
    // Should be:
    //{ return "<"+uriStr+">"; }
    // but it is safer (Jena5) to follow the unlimited style
    // which is the traditional Jena behaviour.
    // This has a knock on effect on Triple.toString.
    // Test TestTriple.testTripleToStringOrdering is affected.
    { return uriStr; }

    /**
     * Answer a String representing the node, taking into account the PrefixMapping.
     */
    @Override
    public String toString(PrefixMapping pm) {
        if ( pm != null ) {
            String x = pm.qnameFor(uriStr);
            if ( x != null )
                return x;
        }
        return toString();
    }

    @Override
    public String getNameSpace() {
        return uriStr.substring(0, Util.splitNamespaceXML(uriStr));
    }

    @Override
    public String getLocalName() {
        return uriStr.substring(Util.splitNamespaceXML(uriStr));
    }

    @Override
    public boolean hasURI(String uri) {
        return uriStr.equals(uri);
    }

    @Override
    public int hashCode() {
        return Node.hashURI+uriStr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Node_URI other = (Node_URI)obj;
        return uriStr.equals(other.uriStr);
    }
}
