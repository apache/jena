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

import java.util.Objects;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

/**
 * IRI(expr), one argument, SPARQL standard form.
 * The function URI(expr) is the same, but under a different name as a
 * subclass.
 * <p>
 * As an ARQ extension , there is also {@code IRI(base, relative)} form which is E_IRI2.
 * The relative URI (string or IRI) is resolved against the result of the "base" expression.
 * which in turn is resolved as per the one-argument form.
 */
public class E_IRI extends ExprFunction1 {

    private static final String sparqlPrintName = "IRI";
    private static final String sseFunctionName = Tags.tagIri;

    // The base in force when the function was created.
    protected final String parserBase;
    protected final Expr relExpr;

    public E_IRI(Expr relExpr) {
        this(null, relExpr);
    }

    public E_IRI(String parserBaseURI, Expr relExpr) {
        this(parserBaseURI, relExpr, sparqlPrintName, sseFunctionName);
    }

    protected E_IRI(String baseStr, Expr relExpr, String sparqlName, String sseName) {
        //super(relExpr, sparqlName, sseName);
        super(relExpr, sseName);
        this.parserBase = baseStr;
        this.relExpr = relExpr;
    }

    // Evaluation of a "one argument with access to the env.
    @Override
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) {
        // IRI(relative)
        // relative can be a string or an IRI.
        String baseIRI = parserBase;
        NodeValue nvRel = relExpr.eval(binding, env);
        return resolve(nvRel, baseIRI, env);
    }

    /*package*/ static NodeValue resolve(NodeValue relative, String baseIRI, FunctionEnv env) {
        if ( baseIRI == null ) {
            // Legacy
            if ( env.getContext() != null ) {
                Query query = (Query)env.getContext().get(ARQConstants.sysCurrentQuery);
                if ( query != null )
                    baseIRI = query.getBaseURI();
            }
        }
        // XXX Need fix for relative already a URI
        if ( NodeFunctions.isIRI(relative.asNode()) ) {
            relative = NodeValue.makeString(relative.asString());
        }

        return NodeFunctions.iri(relative, baseIRI);
    }

    @Override
    public NodeValue eval(NodeValue v, FunctionEnv env) {
        // Shouldn't be called. Legacy only. Does not support baseExpr!=null
        return resolve(v, parserBase, env);
    }

    @Override
    public Expr copy(Expr expr) {
        return new E_IRI(parserBase, expr);
    }

    @Override
    public NodeValue eval(NodeValue v) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(parserBase);
        return result;
    }

    @Override
    public boolean equals(Expr obj, boolean bySyntax) {
        if ( this == obj )
            return true;
        if ( getClass() != obj.getClass() )
            return false;
        E_IRI other = (E_IRI)obj;
        return Objects.equals(parserBase, other.parserBase) &&
               Objects.equals(relExpr, other.relExpr);
    }
}
