/**
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

package com.hp.hpl.jena.sparql.engine;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** Reverse a renaming (assujign renaming was done by prefixing variable names */
public class UnrenameVars implements NodeTransform
{
    private final String varPrefix ;
    private final boolean repeatedly ;
    
    public UnrenameVars(String varPrefix, boolean repeatedly)
    {
        this.varPrefix = varPrefix ;
        this.repeatedly = repeatedly ;
    }

    @Override
    public Node convert(Node node)
    {
        if ( ! Var.isVar(node) ) return node ;
        // Remove the prefix, repeatedly. 
        Var var = (Var)node ;
        String varName = var.getName() ;
        
        if ( repeatedly )
        {
            while ( varName.startsWith(varPrefix) )
                varName = varName.substring(varPrefix.length()) ;
        }
        else
        {
            if ( varName.startsWith(varPrefix) )
                varName = varName.substring(varPrefix.length()) ;
        }
            
        if ( varName == var.getName() )
            return node ;
        return Var.alloc(varName) ;
    }
}
