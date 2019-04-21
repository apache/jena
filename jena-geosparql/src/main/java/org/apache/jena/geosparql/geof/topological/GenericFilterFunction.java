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
package org.apache.jena.geosparql.geof.topological;

import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.index.GeometryLiteralIndex.GeometryIndex;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
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
 */
public abstract class GenericFilterFunction extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        boolean result = exec(v1.asNode(), v2.asNode());
        return NodeValue.makeBoolean(result);
    }

    public Boolean exec(Node v1, Node v2) {
        try {

            //Check if the first literal is unparseable or geometry is empty (always fails).
            GeometryWrapper geometry1 = GeometryWrapper.extract(v1, GeometryIndex.PRIMARY);

            if (geometry1.isEmpty()) {
                return Boolean.FALSE;
            }

            //Check if the second literal is unparseable or geometry is empty (always fails).
            GeometryWrapper geometry2 = GeometryWrapper.extract(v2, GeometryIndex.SECONDARY);

            if (geometry2.isEmpty()) {
                return Boolean.FALSE;
            }

            if (!permittedTopology(geometry1.getDimensionInfo(), geometry2.getDimensionInfo())) {
                return Boolean.FALSE;
            }

            boolean result = relate(geometry1, geometry2);
            return result;
        } catch (DatatypeFormatException | FactoryException | MismatchedDimensionException | TransformException ex) {
            throw new ExprEvalException(ex.getMessage() + ": " + FmtUtils.stringForNode(v1) + ", " + FmtUtils.stringForNode(v2), ex);
        }
    }

    protected abstract boolean permittedTopology(DimensionInfo sourceDimensionInfo, DimensionInfo targetDimensionInfo);

    protected abstract boolean relate(GeometryWrapper sourceGeometry, GeometryWrapper targetGeometry) throws FactoryException, MismatchedDimensionException, TransformException;

    public abstract boolean isDisjoint();

    public abstract boolean isDisconnected();

}
