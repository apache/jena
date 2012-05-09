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

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** ElementDataset - an association of an RDF Dataset 
 * (graph level version) with a query pattern.
 * Unused in parser. */

public class ElementDataset extends Element
{
    // Can keep either form - but not both.
    // Helps because models have prefixes.
    private DatasetGraph dataset = null ;
    private Element element = null ;
    
    public ElementDataset(DatasetGraph data, Element patternElement)
    {
        this.dataset = data ;
        this.element = patternElement ;
    }
    
    public DatasetGraph getDataset() { return dataset ; }
    public void setDataset(DatasetGraph ds) { dataset = ds ; }
    
    public Element getPatternElement() { return element ; }
    public void setPatternElement(Element elt) { element = elt ; }
    
    @Override
    public int hashCode()
    { 
        int x = getPatternElement().hashCode() ;
        if ( getDataset() != null )
            x ^= getDataset().hashCode() ;
        return x ;
    }
    
    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;
        if ( ! ( el2 instanceof ElementDataset ) )
            return false ;
        ElementDataset blk = (ElementDataset)el2 ;
        
        if ( ! element.equalTo(blk.getPatternElement(), isoMap) )
            return false ;
        
        // Dataset both null
        if ( getDataset() == null && blk.getDataset() == null )
            return true ;
        
        if ( getDataset() != blk.getDataset() )
            return false ;
        
        return true ;
    }
    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
