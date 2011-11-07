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

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class P_Alt extends P_Path2
{
    public P_Alt(Path p1, Path p2)
    {
        super(p1, p2) ;
    }

    @Override
    public void visit(PathVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( path2 instanceof P_Alt ) ) return false ;
        return equalsIso((P_Path2)path2, isoMap) ;
    }

    @Override
    public int hashSeed() { return hashAlt ; }
}
