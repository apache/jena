/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** ElementDataset - an association of an RDF Dataset 
 * (graph level version) with a query pattern.
 * Unused in parser.
 * 
 * @author Andy Seaborne
 */

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

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */