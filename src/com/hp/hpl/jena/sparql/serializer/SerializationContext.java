/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap;

/** Information needed to serialize things */

public class SerializationContext
{
    // ?? Interface : WriterContext
    
    private Prologue prologue ;
    private NodeToLabelMap bNodeMap ;
    
    public SerializationContext(SerializationContext cxt)
    {
        prologue = cxt.prologue ;
        bNodeMap = cxt.bNodeMap ;
    }

    public SerializationContext(Prologue prologue)
    {
        this(prologue, null) ;
    }
    
    public SerializationContext(PrefixMapping prefixMap)
    {
        this(new Prologue(prefixMap)) ;
    }

    public SerializationContext()
    {
        this((Prologue)null, null) ;
    }

    public SerializationContext(PrefixMapping prefixMap, NodeToLabelMap bMap)
    {
        this(new Prologue(prefixMap), bMap) ;
    }
    
    public SerializationContext(Prologue prologue, NodeToLabelMap bMap)
    {
        this.prologue = prologue ;
        if ( this.prologue == null )
            this.prologue = new Prologue() ;
        
        bNodeMap = bMap ;
        if ( bMap == null )
            bNodeMap = new NodeToLabelMap("b", false) ;
    }
    
    /**
     * @return Returns the bNodeMap.
     */
    public NodeToLabelMap getBNodeMap()
    {
        return bNodeMap;
    }
    
    /**
     * @param nodeMap The bNodeMap to set.
     */
    public void setBNodeMap(NodeToLabelMap nodeMap)
    {
        bNodeMap = nodeMap;
    }
    
    /**
     * @return Returns the prefixMap.
     */
    public PrefixMapping getPrefixMapping()
    {
        return prologue.getPrefixMapping();
    }
    
    /**
     * @param prefixMap The prefixMap to set.
     */
    public void setPrefixMapping(PrefixMapping prefixMap)
    {
        prologue.setPrefixMapping(prefixMap) ;
    }
    
    /** @param baseIRI Set the base IRI */
    public void setBaseIRI(String baseIRI) { prologue.setBaseURI(baseIRI) ; }
    
    public String getBaseIRI() { return prologue.getBaseURI() ; }

    
    public Prologue getPrologue()
    {
        return prologue ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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