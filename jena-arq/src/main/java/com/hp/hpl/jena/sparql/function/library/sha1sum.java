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

package com.hp.hpl.jena.sparql.function.library;
// Contribution from Leigh Dodds 

import com.hp.hpl.jena.sparql.expr.E_SHA1 ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

/**
 * ARQ Extension Function that will calculate the 
 * SHA1 sum of any literal. Useful for working with 
 * FOAF data.
 */
public class sha1sum extends FunctionBase1 
{
    // This exists for compatibility.
    // SPARQL 1.1 has a SHA1(expression) function.
    // This stub implements afn:sha1(expr) as an indirection to that function.
    
    private E_SHA1 sha1 = new E_SHA1(null) ;
    
    public sha1sum() {}
    
    @Override
    public NodeValue exec(NodeValue nodeValue) 
    {
        nodeValue = NodeFunctions.str(nodeValue) ;
        return sha1.eval(nodeValue) ;
    }
}
