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
import java.util.Set ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;

public class VarRename
{
    // See also OpVar, VarFinder and VarLib - needs to be pulled together really.
    // Also need to renaming support for renames where only a
    // certain set are mapped (for (assign (?x ?.0)))
    
    private static final String prefix = ARQConstants.allocVarScopeHiding ;
    
    /** Rename all variables in a pattern, EXCEPT for those named as constant */ 
    public static Op rename(Op op, Collection<Var> constants)
    {
        return NodeTransformLib.transform(new RenameVars(constants, prefix), op) ;
    }

    /** Rename all variables in an expression, EXCEPT for those named as constant */ 
    public static ExprList rename(ExprList exprList, Set<Var> constants)
    {
        NodeTransform renamer = new RenameVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, exprList) ;
    }
        
    public static Expr rename(Expr expr, Set<Var> constants)
    {
        NodeTransform renamer = new RenameVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, expr) ;
    }
    
    /** Undo the effect of the rename operation, once or repeatedly.
     * This assumes the op was renamed by VarRename.rename */
    public static Op reverseRename(Op op, boolean repeatedly)
    {
        NodeTransform renamer = new UnrenameVars(prefix, repeatedly) ;
        return NodeTransformLib.transform(renamer, op) ;
    }
    
}
