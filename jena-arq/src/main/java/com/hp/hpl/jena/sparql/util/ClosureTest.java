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

/**
 * Test whether a */
 
package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;

public interface ClosureTest
{
    /** Return true if the closure algorithm should continue with statements
     *  with this resource as subject.  Applied to subject and object iof
     *  each statement traversed
     * 
     *  @param r
     */
    public boolean traverse(Resource r) ; 
    
    /** Return true if the statement should be included in the closure.
     *  The algorithm still recurses on the subject and object - this test
     *  is just about whether it is included in the result collection.
     * 
     *  @param s  Statement to test
     */
    public boolean includeStmt(Statement s) ;
    
}
