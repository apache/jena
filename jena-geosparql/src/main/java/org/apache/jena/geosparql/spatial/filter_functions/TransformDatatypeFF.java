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
package org.apache.jena.geosparql.spatial.filter_functions;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public class TransformDatatypeFF extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {

        try {

            if (!(v2.isIRI() || v2.isString())) {
                throw new ExprEvalException("Not a URI: " + FmtUtils.stringForNode(v2.asNode()));
            }

            String datatypeURI;
            if (v2.isIRI()) {
                datatypeURI = v2.asNode().getURI();
            } else {
                datatypeURI = v2.asString();
            }

            GeometryWrapper geometry = GeometryWrapper.extract(v1, GeometryLiteralIndex.GeometryIndex.PRIMARY);
            Literal convertedGeom = geometry.asLiteral(datatypeURI);

            return NodeValue.makeNode(convertedGeom.getLexicalForm(), convertedGeom.getDatatype());
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }
}
