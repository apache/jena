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

import com.hp.hpl.jena.shared.PrefixMapping;

/**
    A Node_ANY (there should be only one) is a meta-node that is used to stand
    for any other node in a query.
*/

public class Node_ANY extends Node_Fluid
    {
    /* package */ Node_ANY() { super( "" ); }
    
    /** Node_ANY's are only equal to other Node_ANY's */
    @Override
    public boolean equals( Object other )
        {
        if ( this == other ) return true ;
        return other instanceof Node_ANY;
        }
        
    @Override
    public Object visitWith( NodeVisitor v )
        { return v.visitAny( this ); }
        
    @Override
    public boolean matches( Node other )
        { return other != null; }
        
    @Override
    public String toString()
        { return "ANY"; }
    
    @Override
    public String toString( PrefixMapping pm, boolean quoting )
        { return "ANY"; }
    }
