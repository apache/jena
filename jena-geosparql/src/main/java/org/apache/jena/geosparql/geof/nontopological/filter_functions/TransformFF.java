/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GMLDatatype;
import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/** Implements geof:transform. */
public class TransformFF extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue value, NodeValue target) {
        try {
            String targetURI = targetURI(target);
            GeometryWrapper geometry = GeometryWrapper.extract(value);
            if (!targetURI.equals(geometry.getSrsURI())
                    && (!geometry.getSrsInfo().isSRSRecognised()
                            || !SRSRegistry.getSRSInfo(targetURI).isSRSRecognised())) {
                throw new ExprEvalException("Source or target CRS is not recognized.");
            }
            GeometryWrapper transformed = geometry.transform(targetURI);
            // GML is parsed using the CRS dimension, but transformation retains the source coordinate layout.
            if (GMLDatatype.URI.equals(transformed.getGeometryDatatypeURI())
                    && !transformed.isEmpty()
                    && transformed.getCoordinateDimension() != transformed.getCRS().getCoordinateSystem().getDimension()) {
                throw new ExprEvalException("GML transform result has a coordinate dimension incompatible with the target CRS.");
            }
            return transformed.asNodeValue();
        } catch (DatatypeFormatException | FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

    private static String targetURI(NodeValue target) {
        if (target.isIRI()) {
            return target.asNode().getURI();
        }
        if (target.asNode().isLiteral()
                && XSDDatatype.XSDanyURI.getURI().equals(target.asNode().getLiteralDatatypeURI())) {
            String uri = target.asNode().getLiteralValue().toString();
            if (!uri.isBlank()) {
                return uri;
            }
        }
        throw new ExprEvalException("Target CRS must be an IRI or nonempty xsd:anyURI literal.");
    }
}
