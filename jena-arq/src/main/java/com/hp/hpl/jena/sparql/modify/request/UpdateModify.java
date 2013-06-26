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
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.util.Iso ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;

public class UpdateModify extends UpdateWithUsing
{
    private final QuadAcc deletePattern ;
    private final QuadAcc insertPattern ;
    private boolean hasInsert = false ;
    private boolean hasDelete = false ;
    private Element wherePattern ;
    
    public UpdateModify() 
    { 
        this.deletePattern = new QuadAcc() ;
        this.insertPattern = new QuadAcc() ;
        this.wherePattern = null ;
    }
    
    public void setElement(Element element)
    {
        this.wherePattern = element ; 
    }
    
    public QuadAcc getDeleteAcc()
    {
        return deletePattern ;
    }

    public QuadAcc getInsertAcc()
    {
        return insertPattern ;
    }

    public List<Quad> getDeleteQuads()
    {
        return deletePattern.getQuads() ;
    }

    public List<Quad> getInsertQuads()
    {
        return insertPattern.getQuads() ;
    }

    /** Explicit flag to indicate a INSERT clause was seen, even if it had no quads */  
    public void setHasInsertClause(boolean flag)
    {
        hasInsert = flag ;
    }
    
    /** Explicit flag to indicate a DELETE clause was seen, even if it had no quads */  
    public void setHasDeleteClause(boolean flag)
    {
        hasDelete = flag ;
    }

    public boolean hasInsertClause()
    {
        if ( hasInsert ) return true ;
        return insertPattern.getQuads().size() > 0 ;
    }
    
    public boolean hasDeleteClause()
    {
        if ( hasDelete) return true ;
        return deletePattern.getQuads().size() > 0 ;
    }
    
    public Element getWherePattern()
    {
        return wherePattern ;
    }

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
        UpdateModify other = (UpdateModify)obj ;
        if ( hasDelete != other.hasDelete )
            return false ;
        if ( hasInsert != other.hasInsert )
            return false ;
        if ( ! equalIso(other, isoMap))
            return false ;
        if ( ! Iso.isomorphicQuads(getDeleteQuads(), other.getDeleteQuads(), isoMap) ) 
            return false ;
        if ( ! Iso.isomorphicQuads(getInsertQuads(), other.getInsertQuads(), isoMap) )
            return false ;
        if ( ! wherePattern.equalTo(other.wherePattern, isoMap) )
            return false ;
        return true ;
    }
}
