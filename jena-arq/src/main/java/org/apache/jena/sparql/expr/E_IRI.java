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

import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.Tags;

/**
 * IRI(expr). The function URI(expr) is the same, but under a different name as a
 * subclass.
 */
public class E_IRI extends ExprFunction1 {
    private static final String symbol = Tags.tagIri;

    // The base in force when the function was created.
    // Kept separate from baseExpr so we can see whether it was the one argument or two argument form.
    protected final String parserBase;

    // ARQ extension: "IRI(base, relative)"
    protected final Expr baseExpr;
    protected final Expr relExpr;

    public E_IRI(Expr relExpr) {
        this(null, null, relExpr);
    }

    public E_IRI(String parserBaseURI, Expr relExpr) {
        this(null, parserBaseURI, relExpr);
    }

    public E_IRI(Expr baseExpr, Expr relExpr) {
        this(baseExpr, null, relExpr);
    }

    public E_IRI(Expr baseExpr, String parserBaseURI, Expr relExpr) {
        this(baseExpr, parserBaseURI, relExpr, symbol);
    }

    protected E_IRI(Expr baseExpr, String baseStr, Expr relExpr, String altSymbol) {
        super(relExpr, symbol);
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
            NodeValue baseValueResolved = evalOneArg(baseValue, parserBase, env);
            // Check for errors.
            baseIRI = baseValueResolved.getNode().getURI();
        } else {
            baseIRI = parserBase;
        }

        NodeValue nvRel = relExpr.eval(binding, env);
        return evalOneArg(nvRel, baseIRI, env);
    }

    private NodeValue evalOneArg(NodeValue relative, String baseIRI, FunctionEnv env) {
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
        return evalOneArg(v, parserBase, env);
    }

    @Override
    public Expr copy(Expr expr) {
        return new E_IRI(baseExpr, parserBase, expr);
    }

    @Override
    public NodeValue eval(NodeValue v) {
        throw new ARQInternalErrorException("Should not be called");
    }
}
