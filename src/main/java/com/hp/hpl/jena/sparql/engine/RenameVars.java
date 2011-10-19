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

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

public class RenameVars implements NodeTransform
{
    private final Map<Var, Var> aliases = new HashMap<Var, Var>() ;
    private final Collection<Var> constants ;
    private final String varPrefix ;
    
    public RenameVars(Collection<Var> constants, String varPrefix)
    {
        this.constants = constants ;
        this.varPrefix = varPrefix ;
    }
    
    @Override
    public final Node convert(Node node)
    {
        if ( ! Var.isVar(node) ) return node ;
        if ( constants.contains(node ) ) return node ;

        Var var = (Var)node ;
        Var var2 = aliases.get(var) ;
        if ( var2 != null )
            return var2 ;
        // TODO The new name is the old name with a "/" - clashes?
        // Provided the old name isn't a constant as well, this is safe 
        // if renaming is bottom up. 
        // Really safe - use the global allocator.
        //var2 = allocator.allocVar() ;
        var2 = Var.alloc(varPrefix+var.getVarName()) ;
        aliases.put(var, var2) ;
        return var2 ; 
    }
}
