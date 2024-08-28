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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class ConcatFct extends FunctionBase
{
    @Override
    public void checkBuild( final String uri, final ExprList args ) {
        // nothing to check
    }

    @Override
    public NodeValue exec( final List<NodeValue> args ) {
        if ( args.isEmpty() ) {
            final List<CDTValue> result = Collections.emptyList();
            return CDTLiteralFunctionUtils.createNodeValue(result);
        }

        if ( args.size() == 1 ) {
            final NodeValue nv =  args.get(0);
            // make sure that the argument is a well-formed cdt:List literal
            CDTLiteralFunctionUtils.checkAndGetList(nv);

            return nv;
        }

        final List<CDTValue> result = new ArrayList<>();
        for ( final NodeValue nv : args ) {
            final List<CDTValue> l = CDTLiteralFunctionUtils.checkAndGetList(nv);
            result.addAll(l);
        }

        return CDTLiteralFunctionUtils.createNodeValue(result);
    }

}
