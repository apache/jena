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

package com.hp.hpl.jena.sparql.serializer;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap ;

/** Information needed to serialize things */

public class SerializationContext
{
    // ?? Interface : WriterContext
    
    private Prologue prologue ;
    private NodeToLabelMap bNodeMap ;
    private boolean usePlainLiterals = true;
    
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
    
    public SerializationContext(boolean usePlainLiterals)
    {
    	this((Prologue)null, null, usePlainLiterals);
    }
    
    public SerializationContext(PrefixMapping prefixMap, boolean usePlainLiterals)
    {
    	this(new Prologue(prefixMap), null);
    	this.usePlainLiterals = usePlainLiterals;
    }
    
    public SerializationContext(PrefixMapping prefixMap, NodeToLabelMap bMap, boolean usePlainLiterals)
    {
    	this(new Prologue(prefixMap), bMap);
    	this.usePlainLiterals = usePlainLiterals;
    }
    
    public SerializationContext(Prologue prologue, boolean usePlainLiterals)
    {
    	this(prologue, null, usePlainLiterals);
    }
    
    public SerializationContext(Prologue prologue, NodeToLabelMap bMap, boolean usePlainLiterals)
    {
    	this(prologue, bMap);
    	this.usePlainLiterals = usePlainLiterals;
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
    
    /**
     * Gets whether Plain Literal forms should be used for appropriate typed literals (booleans, integers, decimals and doubles)
     */
    public boolean getUsePlainLiterals()
    {
    	return usePlainLiterals;
    }
    
    /**
     * Sets whether Plain Literal forms should be used for appropriate typed literals (booleans, integers, decimals and doubles)
     */
    public void setUsePlainLiterals(boolean usePlainLiterals)
    {
    	this.usePlainLiterals = usePlainLiterals;
    }
}
