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

package org.apache.jena.sparql.expr;

import java.util.Set ;

import javax.xml.datatype.DatatypeConstants ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeTransform;

public interface Expr
{
    public static final Expr NONE = ExprNone.NONE0 ;

    public static final int CMP_GREATER  =  DatatypeConstants.GREATER ;
    public static final int CMP_EQUAL    =  DatatypeConstants.EQUAL ;
    public static final int CMP_LESS     =  DatatypeConstants.LESSER ;

    public static final int CMP_UNEQUAL  = -9 ;
    public static final int CMP_INDETERMINATE  = DatatypeConstants.INDETERMINATE ;

    /** Test whether a Constraint is satisfied, given a set of bindings
     *  Includes error propagation and Effective Boolean Value rules.
     *
     * @param binding   The bindings
     * @param execCxt   FunctionEnv
     * @return  true or false
     */
    public boolean isSatisfied(Binding binding, FunctionEnv execCxt) ;

    /**
     * Variables used by this expression.
     * @see ExprVars#getVarNamesMentioned
     * @see ExprVars#getNonOpVarNamesMentioned
     */
    public Set<Var>  getVarsMentioned() ;

    /**
     * Evaluate this expression against the binding
     * @param binding
     * @param env
     */
    public NodeValue eval(Binding binding, FunctionEnv env) ;

    /** Deep copy with substitution */
    public Expr copySubstitute(Binding binding) ;

    /**
     * Rewrite, applying a node{@literal ->}node transformation
     */
    public Expr applyNodeTransform(NodeTransform transform) ;

    /** Deep copy */
    public Expr deepCopy() ;

    /** Answer whether this is a variable. */
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

    /** Answer whether this is a function. */
    public boolean isFunction() ;
    /** Get the function (returns null if not a function) */
    public ExprFunction getFunction() ;

    public void visit(ExprVisitor visitor) ;

    /**
     * <code>Expr</code> are used in both syntax and algebra. There is no syntax
     * to algebra translation step because the parser uses operator precedence
     * to build the right evaluation structure directly.
     * <p>
     * The exceptions to this are the <code>NOT EXISTS</code> and
     * <code>EXISTS</code> expressions which involve a query pattern. As a
     * result there are different ways in syntax to produce the same algebra
     * form.
     * <p>
     * Two <code>Expr</code> are considered equal if they are equal as algebra
     * expressions. <code>hashCode</code> and <code>equals</code> must implement
     * that.
     * <p>
     * There is also <code>equalsBySyntax</code>. Because two different syntax
     * forms can yield the same algebra, but two different algebra forms
     * must be different syntax, <code>equalsBySyntax</code> implies <code>equals</code>
     * (by algebra).
     * <p>
     * Hence, different {@code hashCode} {@literal =>} not {@code equalsBySyntax}.
     */
    @Override
    public int hashCode() ;

    @Override
    public boolean equals(Object other) ;

    public boolean equalsBySyntax(Expr other) ;

    /** General equality operation - consider this to be 'protected' */
    public boolean equals(Expr other, boolean bySyntax) ;
}
