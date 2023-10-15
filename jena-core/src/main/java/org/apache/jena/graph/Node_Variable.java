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
 * "variable" nodes; these are outside the RDF specification
 */

public class Node_Variable extends Node
{
    private final String varName;

    /**
        Initialise this Node_Variable from a string <code>name</code>,
        which becomes wrapped in a VariableName.
     */
    public Node_Variable( String name )
    { this.varName = name ; }   // Node_RuleVariable

    @Override
    public boolean isConcrete() { return false; }

    @Override
    public String getName()
    { return varName; }

    @Override
    public Object visitWith( NodeVisitor v )
    { return v.visitVariable( this, getName() ); }

    @Override
    public boolean isVariable()
    { return true; }

    @Override
    public int hashCode() {
        if ( varName == null )
            return hashVariable;
        return varName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        // For jena-arq Var
        if ( ! (obj instanceof Node_Variable) )
            return false;
        Node_Variable other = (Node_Variable)obj;
        return Objects.equals(varName, other.varName);
    }

    @Override
    public String toString( PrefixMapping pmap ) { return toString(); }

    @Override
    public String toString()
    { return "?"+varName; }
}

