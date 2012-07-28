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

package com.hp.hpl.jena.graph.query;

/**
     PatternLiteral - an interface for pattern literals recognised by Rewrite.
     Also contains the definitions for the pattern language names.
     
*/
public interface PatternLiteral
    {
    public static final String rdql = "http://jena.hpl.hp.com/2003/07/query/RDQL";
    
    /**
         Answer the pattern string, in whatever the pattern language is.
    */
    String getPatternString();
    
    /**
         Answer the pattern modifiers (a string of characters) of this pattern.
         If the notion of modifiers does not apply, answer null. If there are no
         modifiers, answer the empty string.
    */
    String getPatternModifiers();
    
    /**
         Answer the language of the pattern. The only name recognised at this
         time is "rdql", for the pattern language used by Jena's RDQL.
    */
    String getPatternLanguage();
    }
