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
package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex.GeometryIndex;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.util.FmtUtils;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 *
 *
 */
public class IntersectionFF extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {

        try {
            GeometryWrapper geometry1 = GeometryWrapper.extract(v1, GeometryIndex.PRIMARY);
            GeometryWrapper geometry2 = GeometryWrapper.extract(v2, GeometryIndex.SECONDARY);

            GeometryWrapper intersection = geometry1.intersection(geometry2);

            return intersection.asNodeValue();

        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        } catch (FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + FmtUtils.stringForNode(v1.asNode()) + ", " + FmtUtils.stringForNode(v2.asNode()), ex);
        }

    }

}
