/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.sdb.shared.Named;

/** 
 * @author Andy Seaborne
 * @version $Id: Layout.java,v 1.3 2006/05/07 19:19:24 andy_seaborne Exp $
 */

public enum LayoutType implements Named {
    // The Jena2 database layout
    LayoutRDB          { public String getName() { return "RDB" ; } } ,          
    // A database layout that uses a single triple table, with entries being SPARQL-syntax RDF-terms
    LayoutSimple       { public String getName() { return "Layout1" ; } } ,   
    // The Triple table/Node table layout 
    LayoutTripleNodes  { public String getName() { return "TriplesNodes" ; } } , 
    ;
    
    public static LayoutType create(String s)
    {
        if ( s.equalsIgnoreCase(LayoutRDB.getName()) ) return LayoutRDB ;
        
        if ( s.equalsIgnoreCase(LayoutSimple.getName()) ) return LayoutSimple ;
        if ( s.equalsIgnoreCase("layout1") ) return LayoutSimple ;
        
        if ( s.equalsIgnoreCase(LayoutTripleNodes.getName()) ) return LayoutTripleNodes ;
        if ( s.equalsIgnoreCase("layout2") ) return LayoutTripleNodes ;
        return null ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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