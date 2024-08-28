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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;

public class MapFct extends FunctionBase
{
    @Override
    public void checkBuild( final String uri, final ExprList args ) {
        if ( args.size() % 2 == 1 )
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes an even number of arguments");
    }

    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        if ( args.size() % 2 == 1 )
            throw new ExprException("Function '"+Lib.className(this)+"' takes an even number of arguments");

        final Map<CDTKey,CDTValue> map = new HashMap<>();

        final Iterator<Expr> it = args.iterator();
        while ( it.hasNext() ) {
            final Expr exprKey = it.next();
            final Expr exprValue = it.next();

            final CDTKey key = getKey(exprKey, binding, env);
            if ( key != null ) {
                final CDTValue value = getValue(exprValue, binding, env);
                map.put(key, value);
            }
        }

        return CDTLiteralFunctionUtils.createNodeValue(map);
    }

    protected CDTKey getKey( final Expr e, final Binding binding, final FunctionEnv env ) {
        final NodeValue nv;
        try {
            nv = e.eval(binding, env);
        } catch ( final ExprException ex ) {
            return null;
        }

        final Node n = nv.asNode();
        if ( ! n.isURI() && ! n.isLiteral() )
            return null;

        return CDTFactory.createKey(n);
    }

    protected CDTValue getValue( final Expr e, final Binding binding, final FunctionEnv env ) {
        final NodeValue nv;
        try {
            nv = e.eval(binding, env);
        } catch ( final ExprException ex ) {
            return CDTFactory.getNullValue();
        }

        return CDTFactory.createValue( nv.asNode() );
    }


    @Override
    public NodeValue exec( final List<NodeValue> args ) {
        throw new IllegalStateException("should never end up here");
    }

}
