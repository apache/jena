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

package org.apache.jena.riot.checker;

import org.apache.jena.riot.system.ErrorHandler ;

import com.hp.hpl.jena.graph.Node ;

public class CheckerBlankNodes implements NodeChecker
{
    private ErrorHandler handler ;

    public CheckerBlankNodes(ErrorHandler handler)
    {
        this.handler = handler ;
    }
    
    @Override
    public boolean check(Node node, long line, long col)
    { return node.isBlank() && checkBlank(node, line, col) ; }
    
    public boolean checkBlank(Node node, long line, long col)
    {
        String x =  node.getBlankNodeLabel() ;
        if ( x.indexOf(' ') >= 0 )
        {
            handler.error("Illegal blank node label (contains a space): "+node, line, col) ;
            return false ; 
        }
        return true ;
    }
}
