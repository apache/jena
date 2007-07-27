/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Symbol;

public class LayoutType2 extends Symbol implements Named
{
    static Set<LayoutType2> registeredTypes = new HashSet<LayoutType2>() ;
    
    
    
    public static final LayoutType2 LayoutHash = new LayoutType2("layout2") ;
    
    public static LayoutType2 convert(String s)
    {
        if ( s.equalsIgnoreCase(LayoutRDB.getName()) ) return LayoutRDB ;
        if ( s.equalsIgnoreCase("layoutRDB") ) return LayoutRDB ;
        
        if ( s.equalsIgnoreCase(LayoutSimple.getName()) ) return LayoutSimple ;
        if ( s.equalsIgnoreCase("layout1") ) return LayoutSimple ;
        
        if ( s.equalsIgnoreCase("layout2") ) return LayoutTripleNodesHash ;
        if ( s.equalsIgnoreCase("layout2/hash") ) return LayoutTripleNodesHash ;
        
        if ( s.equalsIgnoreCase(LayoutTripleNodesIndex.getName()) ) return LayoutTripleNodesIndex ;
        if ( s.equalsIgnoreCase("layout2/index") ) return LayoutTripleNodesIndex ;
        
        
        return null ;
    }
    
    public static final LayoutType2 LayoutTripleNodesHash           = new LayoutType2("layout2/hash") ;
    public static final LayoutType2 LayoutTripleNodesIndex           = new LayoutType2("layout2/index") ;
    public static final LayoutType2 LayoutSimple           = new LayoutType2("layoutRDB") ;
    public static final LayoutType2 LayoutRDB           = new LayoutType2("layout1") ;
    
    static void init()
    {
        register(LayoutTripleNodesHash) ;
        register(LayoutTripleNodesIndex) ;
        register(LayoutSimple) ;
        register(LayoutRDB) ;
    }
    
    static public void register(String name)
    {
        register(new LayoutType2(name)) ; 
    }
    
    static public void register(LayoutType2 layoutType)
    {
        registeredTypes.add(layoutType) ; 
    }

    private LayoutType2(String layoutName)
    {
        super(layoutName) ;
    }

    public String getName()
    {
        return super.getSymbol() ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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