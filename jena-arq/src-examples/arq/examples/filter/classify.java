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

package arq.examples.filter;

import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase1 ;

/** Example filter function that returns an indicative type string.
 *  <ul>
 *  <li>"Number", if it's a number of some kind</li>
 *  <li>"String", if it's string</li>
 *  <li>"DateTime", if it's a date time</li>
 *  <li>"unknown" otherwise</li>
 *  </ul>
 *  
 *  Usage:
 *  <pre>
 *    PREFIX ext: <java:arq.examples.ext.>
 *  </pre>
 *  <pre>
 *    FILTER ext:classify(3+?x)
 *  <pre> */ 

public class classify extends FunctionBase1
{
    public classify() { super() ; }

    @Override
    public NodeValue exec(NodeValue v)
    { 
        if ( v.isNumber() ) return NodeValue.makeString("number") ;
        if ( v.isDateTime() ) return NodeValue.makeString("dateTime") ;
        if ( v.isString() ) return NodeValue.makeString("string") ;
        
        return NodeValue.makeString("unknown") ;
    }
}
