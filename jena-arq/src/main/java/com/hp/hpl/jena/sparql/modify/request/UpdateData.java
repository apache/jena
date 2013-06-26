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

import java.util.List ;

import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;

public abstract class UpdateData extends Update
{
    protected QuadDataAcc quadData ;
    
    public UpdateData(QuadDataAcc quadData) { this.quadData = quadData ; } 
    
    public List<Quad> getQuads() { return quadData.getQuads() ; }
    
    @Override
    final
    public boolean equalTo(Update obj, NodeIsomorphismMap isoMap) {
        if (this == obj)
            return true ;
        if (getClass() != obj.getClass())
            return false ;
        UpdateData other = (UpdateData)obj ;
        List<Quad> quads1 = getQuads() ;
        List<Quad> quads2 = other.getQuads() ;
        return Iso.isomorphicQuads(quads1, quads2, isoMap) ;
    }

}
