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

import static org.apache.jena.sparql.expr.E_IRI.resolve;

import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

/**
 * IRI(base, expr). Two arguemnt SPARQl extension. The function URI(expr) is the same, but under a different name as a
 * subclass.
 * <p>
 * As an ARQ extension, {@code IRI(base, relative)} resolves the relative
 * URI (string or IRI) against the result of the "base" expression,
 * which in turn is resolved as per the one-argument form.
 */
public class E_IRI2 extends ExprFunction2 {

    private static final String sparqlPrintName = "IRI";
    private static final String sseFunctionName = Tags.tagIri2;

    // The base in force when the function was created.
    // Kept separate from baseExpr so we can see whether it was the one argument or two argument form.
    protected final String parserBase;

    // ARQ extension: "IRI(base, relative)"
    protected final Expr baseExpr;
    protected final Expr relExpr;

    public E_IRI2(Expr baseExpr, String parserBaseURI, Expr relExpr) {
        this(baseExpr, parserBaseURI, relExpr, sparqlPrintName, sseFunctionName);
    }

    protected E_IRI2(Expr baseExpr, String baseStr, Expr relExpr, String sparqlName, String sseName) {
        //super(baseExpr, relExpr, sparqlName, sseName);
        super(baseExpr, relExpr, sseName);
        this.parserBase = baseStr;
        this.baseExpr = baseExpr;
        this.relExpr = relExpr;
    }

    // Evaluation of a "one argument or two" function.
    @Override
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) {
        // IRI(<base>, relative) or IRI(relative);
        // relative can be a string or an IRI.
        // <base> can be relative (becomes IRI(IRI(<base>), rel)).

        String baseIRI = null;
        if ( baseExpr != null ) {
            NodeValue baseValue = baseExpr.eval(binding, env);
            // Check: IRI.
            NodeValue baseValueResolved = resolve(baseValue, parserBase, env);
            // Check for errors.
            baseIRI = baseValueResolved.getNode().getURI();
        } else {
            baseIRI = parserBase;
        }

        NodeValue nvRel = relExpr.eval(binding, env);
        return resolve(nvRel, baseIRI, env);
    }

    @Override
    public NodeValue eval(NodeValue v1, NodeValue v2, FunctionEnv env) {
        // Shouldn't be called. Legacy only. Does not support baseExpr!=null
        return resolve(v1, parserBase, env);
    }

    @Override
    public Expr copy(Expr expr1, Expr expr2) {
        return new E_IRI2(expr1, parserBase, expr2);
    }

    @Override
    public NodeValue eval(NodeValue v1, NodeValue v2) {
        throw new ARQInternalErrorException("Should not be called");
    }

    @Override
    public String getFunctionPrintName(SerializationContext cxt)
    { return sparqlPrintName ; }

    /**
     * Get the parser base - the base URI a a string at the point in parsing when
     * this object was created. If there is an explicit base expression (2 argument
     * form), the parse base is used to make the base expression absolute.
     * <p>
     * This may be null - the object may not have been created by the parser.
     */
    public String getParserBase() {
        return parserBase;
    }

    /**
     * Expression for the relative URI. This is the argument to the one-argument form
     * or the second argument for the two-argument form.
     */
    public Expr getRelExpr() {
        return relExpr;
    }

    /*
     * Base expression - 2 argument for of "IRI(base, relative)".
     * This is an ARQ extension.
     */
    public Expr getBaseExpr() {
        return baseExpr;
    }
}
