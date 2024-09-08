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

package org.apache.jena.sparql.function.library.cdt;

import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class SubSeqFct extends FunctionBase
{
    @Override
    public void checkBuild( final String uri, final ExprList args ) {
        if ( args.size() < 2 || args.size() > 3 )
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes two or three arguments");
    }

    @Override
    public NodeValue exec( final List<NodeValue> args ) {
        final NodeValue nv1 = args.get(0);
        final Node n1 = nv1.asNode();

        CDTLiteralFunctionUtils.ensureListLiteral(n1);

        final NodeValue nv2 = args.get(1);

        if ( ! nv2.isInteger() )
            throw new ExprEvalException("Not an integer literal: " + nv2);

        final int index  = nv2.getInteger().intValue();

        if ( index < 1 )
            throw new ExprEvalException("Out of bounds index value: " + nv2);

        final int length;
        final List<CDTValue> list;
        if ( args.size() == 3 ) {
            final NodeValue nv3 = args.get(2);

            if ( ! nv3.isInteger() )
                throw new ExprEvalException("Not an integer literal: " + nv3);

            length = nv3.getInteger().intValue();

            if ( length < 0 )
                throw new ExprEvalException("Illegal length value: " + nv3);

            list = CDTLiteralFunctionUtils.getList(n1);
        }
        else {
            list = CDTLiteralFunctionUtils.getList(n1);
            length = list.size() - index + 1;
        }

        if ( index > list.size() + 1 )
            throw new ExprEvalException("Out of bounds index value: " + nv2);

        if ( index + length > list.size() + 1 )
            throw new ExprEvalException("Beyond list length (index: " + index + ", length: " + length + ")");

        final List<CDTValue> sublist;
        if ( index == list.size() + 1 ) {
            if ( length != 0 )
                throw new ExprEvalException("Illegal arguments (index: " + index + ", length: " + length + ")");

            if ( list.isEmpty() )
                return nv1;

            sublist = Collections.emptyList();
        }
        else {
            sublist = list.subList( index - 1, index - 1 + length );
        }

        return CDTLiteralFunctionUtils.createNodeValue(sublist);
    }

}
