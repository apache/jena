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
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class KeysFct extends FunctionBase1
{
    @Override
    public NodeValue exec( final NodeValue nv ) {
        final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.checkAndGetMap(nv);

        final List<CDTValue> list = new ArrayList<>( map.size() );
        for ( final CDTKey key : map.keySet() ) {
            final CDTValue value = CDTFactory.createValue( key.asNode() );
            list.add(value);
        }

        return CDTLiteralFunctionUtils.createNodeValue(list);
    }

}
