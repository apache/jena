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
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.Tags;

/**
 * IRI(expr). The function URI(expr) is the same, but under a different name as a
 * subclass.
 */
public class E_IRI extends ExprFunction1 {
    private static final String symbol = Tags.tagIri;
    // The BASE in force when the function was created.
    // Used for relative IRIs. Maybe null (unset, unknown).
    protected final String base;

    public E_IRI(Expr expr) {
        super(expr, symbol);
        base = null;
    }

    protected E_IRI(Expr expr, String altSymbol) {
        super(expr, altSymbol);
        base = null;
    }

    public E_IRI(String baseURI, Expr expr) {
        super(expr, symbol);
        base = baseURI;
    }

    @Override
    public NodeValue eval(NodeValue v, FunctionEnv env) {
        if ( base != null )
            return NodeFunctions.iri(v, base);
        // Legacy, mainly for old SSE which does not have the base.
        String baseIRI = null ;
        if ( env.getContext() != null )
        {
            Query query = (Query)env.getContext().get(ARQConstants.sysCurrentQuery) ;
            if ( query != null )
                baseIRI = query.getBaseURI() ;
        }
        return NodeFunctions.iri(v, baseIRI) ;
    }

    @Override
    public Expr copy(Expr expr) {
        return new E_IRI(base, expr);
    }

    @Override
    public NodeValue eval(NodeValue v) {
        throw new ARQInternalErrorException("Should not be called");
    }
}
