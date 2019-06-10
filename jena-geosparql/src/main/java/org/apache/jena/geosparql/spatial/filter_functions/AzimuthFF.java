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
import org.apache.jena.geosparql.implementation.great_circle.Azimuth;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase4;
import org.apache.jena.sparql.util.FmtUtils;

/**
 *
 *
 */
public class AzimuthFF extends FunctionBase4 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2, NodeValue v3, NodeValue v4) {

        try {
            if (!v1.isNumber()) {
                throw new ExprEvalException("Not a Number: " + FmtUtils.stringForNode(v1.asNode()));
            }

            if (!v2.isNumber()) {
                throw new ExprEvalException("Not a Number: " + FmtUtils.stringForNode(v2.asNode()));
            }

            if (!v3.isNumber()) {
                throw new ExprEvalException("Not a Number: " + FmtUtils.stringForNode(v3.asNode()));
            }

            if (!v4.isNumber()) {
                throw new ExprEvalException("Not a Number: " + FmtUtils.stringForNode(v4.asNode()));
            }

            double lat1 = v1.getDouble();
            double lon1 = v2.getDouble();
            double lat2 = v3.getDouble();
            double lon2 = v4.getDouble();

            double radians = Azimuth.find(lat1, lon1, lat2, lon2);

            return NodeValue.makeDouble(radians);
        } catch (DatatypeFormatException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }

}
