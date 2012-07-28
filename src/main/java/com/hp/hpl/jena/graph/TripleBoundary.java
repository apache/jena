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
     An interface for expressing a stopping condition on triples, such as in 
     sub-graph extraction.
  */
public interface TripleBoundary
    {
    /**
         Answer true if this triple is a stopping triple, and whatever search is using
         this interface should proceed no further.
    */
    boolean stopAt( Triple t );
    
    /**
         A TripleBoundary without limits - stopAt always returns false.
    */
    public static final TripleBoundary stopNowhere = new TripleBoundary()
        { @Override
        public boolean stopAt( Triple t ) { return false; } };
    
    /**
        A TripleBoundary that stops at triples with anonymous objects.
    */
    public static final TripleBoundary stopAtAnonObject = new TripleBoundary()
        { @Override
        public boolean stopAt( Triple t ) { return t.getObject().isBlank(); } };

    }
