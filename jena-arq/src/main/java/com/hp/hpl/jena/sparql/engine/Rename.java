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

package com.hp.hpl.jena.sparql.engine;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;

/** Support for renaming all the variables in an algebra expession
 *  except for a fixed set to leave alone */
public class Rename
{
    /** Given an old name, and some names already in use, pick a fresh, new name*/ 
    public static Var chooseVarName(Var var, Collection<Var> inUse, String prefix)
    {
        Var var2 = var ;
        do {
            var2 = Var.alloc(prefix+var2.getName()) ;
        } while (inUse.contains(var2)) ;    
        return var2 ;
    }
    
    /** Rename one node to another */
    public static Op renameNode(Op op, Node oldName, Node newName)
    {
        NodeTransform renamer = new RenameNode(oldName, newName) ;
        return NodeTransformLib.transform(renamer, op) ;
    }

    /** Rename one variable to another */
    public static Op renameVar(Op op, Var oldName, Var newName)
    {
        NodeTransform renamer = new RenameNode(oldName, newName) ;
        return NodeTransformLib.transform(renamer, op) ;
    }
    
    
    // See also OpVar, VarFinder and VarLib - needs to be pulled together really.
    // Also need renaming support for renames where only a
    // certain set are mapped (for (assign (?x ?.0)))
    
    private static final String prefix = ARQConstants.allocVarScopeHiding ;
    
    /** Rename all variables in a pattern, EXCEPT for those named as constant */ 
    public static Op renameVars(Op op, Collection<Var> constants)
    {
        return NodeTransformLib.transform(new RenameAnyVars(constants, prefix), op) ;
    }

    /** Rename all variables in an expression, EXCEPT for those named as constant */ 
    public static ExprList renameVars(ExprList exprList, Set<Var> constants)
    {
        NodeTransform renamer = new RenameAnyVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, exprList) ;
    }
        
    public static Expr renameVars(Expr expr, Set<Var> constants)
    {
        NodeTransform renamer = new RenameAnyVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, expr) ;
    }
    
    /** Undo the effect of the rename operation, once or repeatedly.
     * This assumes the op was renamed by VarRename.rename */
    public static Op reverseVarRename(Op op, boolean repeatedly)
    {
        NodeTransform renamer = new UnrenameAnyVars(prefix, repeatedly) ;
        return NodeTransformLib.transform(renamer, op) ;
    }
    
    // ---- Transforms that do the renaming and unrenaming.
    
    static class RenameNode implements NodeTransform
    {
        private final Node oldName ;
        private final Node newName ;
        public RenameNode (Node oldName, Node newName)
        {
            this.oldName = oldName ; 
            this.newName = newName ; 
        }
        
        @Override
        public Node convert(Node node)
        {
            if ( node.equals(oldName) )
                return newName ;
            return node ;
        }
        
    }
    
    static class RenameAnyVars implements NodeTransform
    {
        private final Map<Var, Var> aliases = new HashMap<>() ;
        private final Collection<Var> constants ;
        private final String varPrefix ;
        
        public RenameAnyVars(Collection<Var> constants, String varPrefix)
        {
            this.constants = constants ;
            this.varPrefix = varPrefix ;
        }
        
        @Override
        public final Node convert(Node node)
        {
            if ( ! Var.isVar(node) ) return node ;
            if ( constants.contains(node) ) return node ;

            Var var = (Var)node ;
            Var var2 = aliases.get(var) ;
            if ( var2 != null )
                return var2 ;
            // The new name is the old name with a "/" - clashes?
            // Provided the old name isn't a constant as well, this is safe 
            // if renaming is bottom up and done once. 
            // Really safe - use the global allocator.
            //var2 = allocator.allocVar() ;
            
            var2 = Rename.chooseVarName(var, constants, prefix) ;
            aliases.put(var, var2) ;
            return var2 ; 
        }
    }

    /** Reverse a renaming (assuming renaming was done by prefixing variable names) */
    static class UnrenameAnyVars implements NodeTransform
    {
        private final String varPrefix ;
        private final boolean repeatedly ;
        
        public UnrenameAnyVars(String varPrefix, boolean repeatedly)
        {
            this.varPrefix = varPrefix ;
            this.repeatedly = repeatedly ;
        }

        @Override
        public Node convert(Node node)
        {
            if ( ! Var.isVar(node) ) 
                return node ;
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
}
