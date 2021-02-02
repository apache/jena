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



/**
    RDF blank nodes, ie nodes with identity but without URIs.
*/

public class Node_Blank extends Node_Concrete
{
    /* package */ Node_Blank( BlankNodeId id ) { super( id ); } 

    @Override
    public boolean isBlank() { return true; }

    @Override
    public BlankNodeId getBlankNodeId()  { return (BlankNodeId) label; }
    
    @Override
    public Object visitWith( NodeVisitor v )
        { return v.visitBlank( this, (BlankNodeId) label ); }
    
    @Override
    public boolean equals( Object other )
        {
        if ( this == other ) return true ;
        return other instanceof Node_Blank && label.equals( ((Node_Blank) other).label ); }
        }
