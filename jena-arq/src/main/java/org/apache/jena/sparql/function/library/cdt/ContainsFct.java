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

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ContainsFct extends FunctionBase2
{
    @Override
    public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
        final List<CDTValue> list = CDTLiteralFunctionUtils.checkAndGetList(nv1);
        final boolean result = containsNode( list, nv2 );
        return NodeValue.booleanReturn(result);
    }

    /**
     * Returns true if the given list contains the given RDF term.
     */
    protected boolean containsNode( final List<CDTValue> list, final NodeValue nv ) {
        for ( final CDTValue v : list ) {
            if ( v.isNode() ) {
                final NodeValue vv = NodeValue.makeNode( v.asNode() );
                if ( NodeValue.sameValueAs(vv, nv) ) {
                    return true;
                }
            }
        }

        return false;
    }

}