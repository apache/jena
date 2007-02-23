/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1;

import java.io.OutputStream;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;


public class PlanFormatter
{
//    public final static String startMarker  = "[" ; 
//    public final static String finishMarker = "]" ;
    
    // Cursor policy : leave at end of line when finished - index corrected.
    
    static public void out(IndentedWriter w, PlanElement pElt)
    {
        out(w, (SerializationContext)null, pElt) ; 
    }
    
    static public void out(OutputStream outStream, PlanElement pElt)
    {
        out(outStream, (SerializationContext)null, pElt) ;
    }


    static public void out(OutputStream outStream, PrefixMapping pmap, PlanElement pElt)
    {
        IndentedWriter w = new IndentedWriter(outStream) ;
        out(w, pmap, pElt) ;
    }

    static public void out(OutputStream outStream, Query query, PlanElement pElt)
    {
        IndentedWriter w = new IndentedWriter(outStream) ;
        out(w, query, pElt) ;
    }
    
    static public void out(OutputStream outStream, SerializationContext pmap, PlanElement pElt)
    {
        IndentedWriter w = new IndentedWriter(outStream) ;
        out(w, pmap, pElt) ;
    }
    
    
    // Worker 1
    
    static public void out(IndentedWriter w, Query query, PlanElement pElt)
    {
        PrefixMapping pmap = null ;
        if ( query != null && query.getPrefixMapping() != null )
            pmap = query.getPrefixMapping() ;
        out(w, pmap, pElt) ;
    }
        
    static public void out(IndentedWriter w, PrefixMapping pmap, PlanElement pElt)
    {
        SerializationContext sCxt = new SerializationContext(pmap) ;
        out(w, sCxt, pElt) ;
    }
    
    // Worker 2
    
    static public void out(IndentedWriter w, SerializationContext sCxt, PlanElement pElt)
    {
        if ( sCxt == null )
            sCxt = new SerializationContext() ;
        PlanFormatterVisitor fmt = new PlanFormatterVisitor(w, sCxt) ;
        fmt.startVisit() ;
        pElt.visit(fmt) ;
        fmt.finishVisit() ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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