/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import java.util.Properties ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

public class EnvTDB
{
    public static void processGlobalSystemProperties()
    {
        Context context = processProperties(System.getProperties()) ;
        TDB.getContext().setAll(context) ;
    }
    
    static final String prefix = SystemTDB.tdbSymbolPrefix+":" ;
    public static Context processProperties(Properties properties)
    {
        Context context = new Context() ;
        Set<Object> keys = properties.keySet() ;
        for ( Object key : keys )
        {
            if ( key instanceof String )
            {
                String keyStr = (String)key ;
                if ( keyStr.startsWith(prefix) )
                    keyStr = SystemTDB.symbolNamespace+keyStr.substring(prefix.length()) ;
                
                
                if ( ! keyStr.startsWith(SystemTDB.symbolNamespace) )
                    continue ;
                
                Object value = properties.get(key) ;
                
                Symbol symbol = Symbol.create(keyStr) ;
                
                context.set(symbol, value) ;
            }
        }
        return context ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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