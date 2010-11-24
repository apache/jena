/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.mgt;

import java.util.Iterator ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpSession ;

import org.openjena.fuseki.server.DatasetRegistry ;

/** Avoid code in JSPs */
public class Functions
{
    /** Return the name of the current dataset */ 
    public static String dataset(HttpServletRequest request, String dftValue)
    {
        String ds = dataset(request) ;
        if ( ds == null )
            return dftValue ;
        return ds ;
    }
    
    /** Return the name of the current dataset */ 
    public static String dataset(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false) ;
        if ( session == null )
            return "No session";
        String ds = (String)session.getAttribute("dataset") ;
        return ds ;
    }
    
    /** Return lists of datasets */ 
    public static String datasetsAsSelectOptions(HttpServletRequest request)
    {
        StringBuilder buff = new StringBuilder() ;
        
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        for ( ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            buff.append("<option value=\""+name+"\">"+name+"</option>") ;
        }
        return buff.toString() ;
    }
    /** Return lists of datasets */ 
    public static String datasetsAsLitItems(HttpServletRequest request)
    {
        StringBuilder buff = new StringBuilder() ;
        
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        for ( ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            buff.append("  <li>"+name+"</li>") ;
        }
        return buff.toString() ;
    }
    
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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