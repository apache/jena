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

package com.hp.hpl.jena.graph.query.regexptrees;

/**
     AnyOf - a pattern that can match any of a set of specific characters
     @author kers
*/
public class AnyOf extends RegexpTree
    {
    protected String possibles;
    
    public AnyOf( String possibles )
        { this.possibles = possibles; }
    
    @Override
    public boolean equals( Object other )
        { return other instanceof AnyOf && possibles.equals( ((AnyOf) other).possibles ); }
    
    @Override
    public int hashCode()
        { return possibles.hashCode(); }
    
    @Override
    public String toString()
        { return "<anyof '" + possibles + "'>"; }
    }
