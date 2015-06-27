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

package org.apache.jena.sparql.modify.request;

import java.util.List ;

import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Iso ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;
import org.apache.jena.update.Update ;

public class UpdateDeleteWhere extends Update
{
    private final QuadAcc pattern ;

    public UpdateDeleteWhere(QuadAcc pattern) { this.pattern = pattern ; }
    
    public List<Quad> getQuads() { return pattern.getQuads() ; }
    
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
        UpdateDeleteWhere other = (UpdateDeleteWhere)obj ;
        return Iso.isomorphicQuads(getQuads(), other.getQuads(), isoMap) ;
    }
}
