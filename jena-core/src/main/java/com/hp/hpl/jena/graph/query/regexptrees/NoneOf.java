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
 NoneOf
 @author kers
 */
public class NoneOf extends RegexpTree
    {
    protected String impossibles;
    
    public NoneOf( String impossibles )
        { this.impossibles = impossibles; }
    
    @Override
    public boolean equals( Object other )
        { return other instanceof NoneOf && impossibles.equals( ((NoneOf) other).impossibles ); }

    @Override
    public int hashCode()
        { return impossibles.hashCode(); }

    @Override
    public String toString()
        { return "<none '" + impossibles + "'>"; }
    }
