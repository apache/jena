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

package com.hp.hpl.jena.sparql.expr;

import java.util.Collection ;
import java.util.Set ;

import javax.xml.datatype.DatatypeConstants ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

public interface Expr
{
    public static final int CMP_GREATER  =  DatatypeConstants.GREATER ;
    public static final int CMP_EQUAL    =  DatatypeConstants.EQUAL ;
    public static final int CMP_LESS     =  DatatypeConstants.LESSER ;
    
    public static final int CMP_UNEQUAL  = -9 ;
    public static final int CMP_INDETERMINATE  = DatatypeConstants.INDETERMINATE ;
    
    /** Test whether a Constraint is satisfied, given a set of bindings
     *  Includes error propagtion and Effective Boolean Value rules.
     * 
     * @param binding   The bindings
     * @param execCxt   FunctionEnv   
     * @return  true or false
     */ 
    public boolean isSatisfied(Binding binding, FunctionEnv execCxt) ;
  
    /** Variables used by this expression - excludes variables scoped to (NOT)EXISTS*/
    public Set<Var>  getVarsMentioned() ;
    /** Variables used by this expression - excludes variables scoped to (NOT)EXISTS*/
    public void varsMentioned(Collection<Var> acc) ;
    
    /** Return true iff this constraint is implemened by something in the expr package
     * and hence is fully integrated or compatible with the visitors.
     * @return boolean
     */

    /** Evaluate this expression against the binding
     * @param binding 
     * @param env
     */
    public NodeValue eval(Binding binding, FunctionEnv env) ;
    
    // These (copySubstitute, applyNodeTransform) predate transform support and should be changed.
    // But they work so there is no hurry.
    
    /** Deep copy with substitution */
    public Expr copySubstitute(Binding binding) ;

//    /** Deep copy with substitution, possibly collapsing constant sub-expressions */
// Issues: 
// 1/ folding constants should be separated out (static optimization)
// 2/ Danger of some functions not evaluating if the FunctionEnv is not available.
    public Expr copySubstitute(Binding binding, boolean foldConstants) ;

    /**
     * Rewrite, applying a node->node transformation
     */
    public Expr applyNodeTransform(NodeTransform transform) ;

//    /** Transform. */
//    public Expr apply(ExprTransform transform) ;
    
    /** Deep copy */
    public Expr deepCopy() ;
    
    /** Answer whether this is a variable (in which case getVarName and getNodeVar can be called) */ 
    public boolean isVariable() ;
    /** Variable name (returns null if not a variable) */
    public String  getVarName() ;
    /** Variable (or null) */
    public ExprVar getExprVar() ;
    /** Convert to a Var variable.*/
    public Var asVar() ;

    /** Answer whether this is a constant expression - false includes "don't know"
     *  No constant folding so "false" from an expression that evaluates to a constant
     */  
    public boolean   isConstant() ;
    /** NodeValue constant (returns null if not a constant) */
    public NodeValue getConstant() ;
    
    /** Answer wether this is a function. */
    public boolean isFunction() ;
    /** Get the function (returns null if not a function) */
    public ExprFunction getFunction() ;
    
    public void visit(ExprVisitor visitor) ;
}
