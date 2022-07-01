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

import java.util.List ;
import java.util.StringJoiner ;

import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase ;

/** Function that prints the system time to stderr at the point of execution.
 *  Returns true.
 *  This is a debugging aid only.
 */
public class execTime extends FunctionBase {

    static long lastms = -1 ;
    
    @Override
    public NodeValue exec(List<NodeValue> args)
    {
        long now = System.currentTimeMillis() ;
        StringJoiner sj = new StringJoiner(" ", "", "");
        args.forEach((a)-> { sj.add(a.asString()) ; }) ;
        if ( lastms != -1 )
            sj.add("("+Long.toString(now - lastms)+")") ;
        String str = sj.toString() ;
        
        if ( ! str.isEmpty() )
            System.err.printf("%s : %d ms\n",str, System.currentTimeMillis()) ;
        else
            System.err.printf("---- %d ms\n",str, System.currentTimeMillis()) ;
        lastms = now ;
        return NodeValue.TRUE ;
    }
    
    @Override
    public void checkBuild(String uri, ExprList args)
    {}
}
