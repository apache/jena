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

package org.apache.jena.sparql.path;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.util.Iso ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;

public class P_ReverseLink extends P_Path0
{
    public P_ReverseLink(Node n)
    {
        super(n) ;
    }
    
    @Override
    public boolean isForward()  { return false ; }
    
    @Override
    public void visit(PathVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( path2 instanceof P_ReverseLink ) ) return false ;
        P_ReverseLink other = (P_ReverseLink)path2 ;
        return Iso.nodeIso(node, other.node, isoMap) ;
    }

    @Override
    public int hashCode()
    {
        return node.hashCode() ^ hashRevLink ;
    }
}
