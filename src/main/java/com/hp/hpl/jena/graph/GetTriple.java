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
    this interface describes types that can have a triple extracted using
    a <code>getTriple</code> method. It was constructed so that Node's 
    can have possibly embedded triples but defer to a GetTriple object if 
    they have no triple of their own; the particular GetTriple used initially is
    in Reifier, but that seemed excessively special.
*/

public interface GetTriple
    {
    /**
        Answer the triple associated with the node <code>n</code>.
        @param n the node to use as the key
        @return the associated triple, or <code>null</code> if none
    */
    public Triple getTriple( Node n );
    }
