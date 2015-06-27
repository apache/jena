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

package org.apache.jena.sparql.function.library;

//import org.apache.commons.logging.*;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase ;

/** Function that concatenates strings using a separator.
 *  This is not fn:string-join because 
 *  (1) that takes a sequence as argument
 *  (2) the arguments are in a different order 
 */

public class strjoin extends FunctionBase
{
    @Override
    public final NodeValue exec(List<NodeValue> args)
    {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException(Lib.className(this)+": Null args list") ;
        
        Iterator<NodeValue> iter = args.iterator() ;
        String sep = iter.next().asString() ;

        List<String> x = new ArrayList<>() ;
        for ( ; iter.hasNext() ; )
        {
            NodeValue arg = iter.next();
            x.add( arg.asString() ) ;
        }
        
        return NodeValue.makeString(StrUtils.strjoin(sep, x)) ;
    }

    @Override
    public void checkBuild(String uri, ExprList args)
    {
        if ( args.size() < 1 )
            throw new QueryBuildException("Function '"+Lib.className(this)+"' requires at least one arguments") ;
    }
}
