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

package com.hp.hpl.jena.sparql.modify.request;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;

public class UpdateLoad extends Update
{
    private final String source ;
    private final Node dest ;
    private boolean silent ;
    

    public UpdateLoad(String source, String dest)
    {
        this(source, NodeFactory.createURI(dest), false) ;
    }
    
    public UpdateLoad(String source, String dest, boolean silent)
    {
        this(source, NodeFactory.createURI(dest), silent) ;
    }

    public UpdateLoad(String source, Node dest)
    {
        this(source, dest, false) ;
    }

    public UpdateLoad(String source, Node dest, boolean silent)
    {
        this.source = source ;
        this.dest = dest ;
        this.silent = silent ;
    }

    public String  getSource()      { return source ; }
    public Node    getDest()        { return dest ; }
    public boolean getSilent()      { return silent ; }

    @Override
    public void visit(UpdateVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Update obj, NodeIsomorphismMap isoMap) {
        if (this == obj)
            return true ;
        if (obj == null)
            return false ;
        if (getClass() != obj.getClass())
            return false ;
        UpdateLoad other = (UpdateLoad)obj ;
        return 
            silent == other.silent &&
            Lib.equal(source, other.source) &&
            Iso.nodeIso(dest, other.dest, isoMap) ;
    }
}
