/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import java.util.HashMap;
import java.util.Map;

public class StringAbbrev
{
    Map<String, String> prefix2string = new HashMap<String, String>() ; 

    public StringAbbrev()
    { 
        prefix2string.put("_",":") ;
    }
    
    public void add(String prefix, String string)
    {
        if ( prefix.contains(":") )
            throw new IllegalArgumentException("Prefix contains ':' -- "+prefix) ;
        if ( prefix.equals("") )
            throw new IllegalArgumentException("Prefix is the empty string") ;
        prefix2string.put(prefix, string) ;
    }

    public String abbreviate(String s)
    {
        for (Map.Entry<String, String> e : prefix2string.entrySet() )
        {
            String prefix = e.getKey() ;
            String string = e.getValue() ;
            if ( s.startsWith(string) )
            {
                String s2 = ":"+prefix+":"+s.substring(string.length()) ;
                return s2 ; 
            }
        }
        
        // Should have been caught by the "_" rule.
        if ( s.startsWith(":") ) 
            s = ":"+s ;
        return s ;
    }
    
    public String expand(String s)
    {
        if ( ! s.startsWith(":") )
            return s ;
        int i = s.indexOf(":", 1) ;
        if ( i < 0 )
            return s ;
        // It's "::" which is for strings starting :  
        if ( i == 1 )
            return s.substring(1) ;
        
        String prefix = s.substring(1, i) ;
        if ( prefix == null )
        {
            return s.substring(i+1) ;
        }
        
        String start = prefix2string.get(prefix) ;
        return start+s.substring(i+1) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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