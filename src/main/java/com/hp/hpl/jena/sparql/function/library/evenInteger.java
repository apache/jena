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

package com.hp.hpl.jena.sparql.function.library;

//import org.apache.commons.logging.*;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

/** Example function - checks for even integers. */

public class evenInteger extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue x)
    {
        if ( ! x.isInteger() )
            throw new ExprEvalException("evenInteger: Not an intger: "+x) ;
        int i = x.getInteger().getLowestSetBit() ;
        
        boolean b = (i == -1) || ( i != 0 ) ;  
        
        return NodeValue.makeNodeBoolean( b ) ;
    }

}
