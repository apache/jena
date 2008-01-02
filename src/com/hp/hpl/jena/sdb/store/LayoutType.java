/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.SymbolRegistry;
import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Symbol;

public class LayoutType extends Symbol implements Named
{
    public static final LayoutType LayoutTripleNodesHash  = new LayoutType("layout2/hash") ;
    public static final LayoutType LayoutTripleNodesIndex = new LayoutType("layout2/index") ;
    public static final LayoutType LayoutSimple           = new LayoutType("layout1") ;
    public static final LayoutType LayoutRDB              = new LayoutType("layoutRDB") ;
    
    static SymbolRegistry<LayoutType> registry = new SymbolRegistry<LayoutType>() ;
    static { init() ; }
    
    public static LayoutType fetch(String layoutTypeName)
    {
        if ( layoutTypeName == null )
            throw new IllegalArgumentException("LayoutType.convert: null not allowed") ;
        
        LayoutType t = registry.lookup(layoutTypeName) ;
        if ( t != null )
            return t ;

        LogFactory.getLog(LayoutType.class).warn("Can't turn '"+layoutTypeName+"' into a layout type") ;
        throw new SDBException("Can't turn '"+layoutTypeName+"' into a layout type") ; 
    }
    
    static void init()
    {
        register(LayoutTripleNodesHash) ;
        registerName("layout2",  LayoutTripleNodesHash) ;
        
        register(LayoutTripleNodesIndex) ;
        register(LayoutSimple) ;
        register(LayoutRDB) ;
    }
    
    static public List<String> allNames() { return registry.allNames() ; }
    static public List<LayoutType> allTypes() { return registry.allSymbols() ; }
    
    static public void register(String name)
    {
        if ( name == null )
            throw new IllegalArgumentException("LayoutType.register(String): null not allowed") ;
        register(new LayoutType(name)) ; 
    }
    
    static public void register(LayoutType layoutType)
    {
        if ( layoutType == null )
            throw new IllegalArgumentException("LayoutType.register(LayoutType): null not allowed") ;
        registry.register(layoutType) ; 
    }

    static public void registerName(String layoutName, LayoutType layoutType)
    {
        if ( layoutType == null )
            throw new IllegalArgumentException("LayoutType.registerName: null not allowed") ;
        registry.register(layoutName, layoutType) ; 
    }

    private LayoutType(String layoutName)
    {
        super(layoutName) ;
    }

    public String getName()
    {
        return super.getSymbol() ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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