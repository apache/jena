/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PrefixMappingImpl.java,v 1.1 2003-04-17 14:43:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.PrefixMapping;

import java.util.*;

/**
 	@author kers
*/
public class PrefixMappingImpl implements PrefixMapping
    {

    private Map map;
    
    public PrefixMappingImpl()
        {
        map = new HashMap();
        }
           
    public void setNsPrefix( String prefix, String uri ) 
        { map.put( prefix, uri ); }
    
    public String getNsPrefixURI( String prefix ) 
        { return (String) map.get( prefix ); }
        
    public Map getNsPrefixMap()
        { return new HashMap( map ); }

    }


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/