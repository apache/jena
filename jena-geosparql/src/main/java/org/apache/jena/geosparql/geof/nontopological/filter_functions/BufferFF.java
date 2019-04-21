/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase3;
import org.apache.jena.sparql.util.FmtUtils;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * Return type ogc:geomLiteral ogc:geomLiteral can be a string value like
 * {@code "<gml:Point ...>...</gml:Point>"}
 *
 *
 *
 */
public class BufferFF extends FunctionBase3 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3) {

        try {
            GeometryWrapper geometry = GeometryWrapper.extract(v1);

            //Transfer the parameters as Nodes
            if (!v2.isDouble()) {
                throw new ExprEvalException("Not a Double: " + FmtUtils.stringForNode(v2.asNode()));
            }
            Node node2 = v2.asNode();
            double radius = Double.parseDouble(node2.getLiteralLexicalForm());

            //Obtain the target distance units
            if (!v3.isIRI()) {
                throw new ExprEvalException("Not a IRI: " + FmtUtils.stringForNode(v3.asNode()));
            }
            String unitsURI = v3.getNode().getURI();
            GeometryWrapper buffer = geometry.buffer(radius, unitsURI);

            return buffer.asNodeValue();
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + FmtUtils.stringForNode(v1.asNode()) + ", " + FmtUtils.stringForNode(v2.asNode()) + ", " + FmtUtils.stringForNode(v3.asNode()), ex);
        }

    }
}
