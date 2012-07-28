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

package com.hp.hpl.jena.graph;

/**
    "variable" nodes; these are outside the RDF2003 specification, but are
    used internally for "placeholder" nodes where blank nodes would be
    wrong, most specifically in Query.
*/

public class Node_Variable extends Node_Fluid
    {
    /**
         Initialise this Node_Variable with a name object (which should be a
         VariableName object).
    */
    protected Node_Variable( Object name )
        { super( name ); }
    
    /**
        Initialise this Node_Variable from a string <code>name</code>,
        which becomes wrapped in a VariableName.
    */
    public Node_Variable( String name )
        { super( new VariableName( name ) ); }

    @Override
    public String getName()
        { return ((VariableName) label).name; }
    
    @Override
    public Object visitWith( NodeVisitor v )
        { return v.visitVariable( this, getName() ); }
        
    @Override
    public boolean isVariable()
        { return true; }
        
    @Override
    public String toString()
        { return label.toString(); }
    
    @Override
    public boolean equals( Object other )
        {
        if ( this == other ) return true ;
        return other instanceof Node_Variable && label.equals( ((Node_Variable) other).label );
        }
    
    public static Object variable( String name )
        { return new VariableName( name ); }
    
    public static class VariableName
        {
        private String name;
        
        public VariableName( String name ) 
            { this.name = name; }
        
        @Override
        public int hashCode()
            { return name.hashCode(); }
        
        @Override
        public boolean equals( Object other )
            {
            if ( this == other ) return true ;
            return other instanceof VariableName && name.equals( ((VariableName) other).name );
            }
        
        @Override
        public String toString()
            { return "?" + name; }
        }
    }
