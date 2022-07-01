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

import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;

/**
 * This class is an alternative name for {@linkplain E_IRI}.
 * See the javadoc for {@linkplain E_IRI}.
 */
public class E_URI extends E_IRI {
    private static final String sparqlPrintName = "URI";
    private static final String sseFunctionName = Tags.tagUri;

    public E_URI(Expr relExpr) {
        this(null, relExpr);
    }

    public E_URI(String baseStr, Expr relExpr) {
        super(baseStr, relExpr, sparqlPrintName, sseFunctionName);
    }

    @Override
    public String getFunctionPrintName(SerializationContext cxt)
    { return sparqlPrintName ; }

    @Override
    public Expr copy(Expr expr) {
        return new E_URI(parserBase, expr);
    }

    @Override
    public int hashCode() {
        return super.hashCode()+1;
    }

    @Override
    public boolean equals(Expr obj, boolean bySyntax) {
        if ( this == obj )
            return true;
        if ( getClass() != obj.getClass() )
            return false;
        return super.equals(obj, bySyntax);
    }
}
