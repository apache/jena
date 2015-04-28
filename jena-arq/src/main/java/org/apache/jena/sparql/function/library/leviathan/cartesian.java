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

package org.apache.jena.sparql.function.library.leviathan;

import java.util.List;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase ;

public class cartesian extends FunctionBase {

    @Override
    public NodeValue exec(List<NodeValue> args) {
        if (args.size() != 4 && args.size() != 6)
            throw new ExprEvalException("Incorrect number of arguments");

        switch (args.size()) {
        case 4: {
            double dX = args.get(0).getDouble() - args.get(2).getDouble();
            double dY = args.get(1).getDouble() - args.get(3).getDouble();

            return NodeValue.makeDouble(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
        }
        case 6: {
            double dX = args.get(0).getDouble() - args.get(3).getDouble();
            double dY = args.get(1).getDouble() - args.get(4).getDouble();
            double dZ = args.get(2).getDouble() - args.get(5).getDouble();

            return NodeValue.makeDouble(Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2) + Math.pow(dZ, 2)));
        }
        default:
            throw new ExprEvalException("Incorrect number of arguments");
        }
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if (args.size() != 4 && args.size() != 6)
            throw new QueryBuildException("Function '" + Lib.className(this) + "' takes 4 or 6 argument(s)");
    }

}
