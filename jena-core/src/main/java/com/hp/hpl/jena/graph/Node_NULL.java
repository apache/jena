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
    The Highlander Node_NULL exists for the database code (as a Node that
    coresponds to a null in database tables for the reification code).

*/
@Deprecated
public class Node_NULL extends Node_Concrete
    {
    /* package */  Node_NULL() { super( "" ); }
    
    /** Node_NULL's are equal to no null nodes; strictly speaking,
     *  this incorrect but suits our purposes. really want an isNull
     *  method on Node but that's too much surgery. */
    
    @Override
    public boolean equals( Object other )
        {
        if ( this == other ) return true ;
        return other instanceof Node_NULL;
        }
        
    @Override
    public Object visitWith( NodeVisitor v )
        { return null; }
                
    @Override
    public String toString()
        { return "NULL"; }
    }
