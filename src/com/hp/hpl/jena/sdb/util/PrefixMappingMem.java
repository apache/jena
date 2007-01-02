/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.util.CollectionFactory;

public class PrefixMappingMem extends PrefixMappingImplAlt
{
    protected Map<String, String> prefixToURI ;
    protected Map<String, String> URItoPrefix ;
    
    @SuppressWarnings("unchecked")
    public PrefixMappingMem()
    {
        prefixToURI = CollectionFactory.createHashedMap();
        URItoPrefix = CollectionFactory.createHashedMap(); 
    }
    
    @Override
    public void clear() { prefixToURI.clear() ; URItoPrefix.clear() ; }
    
    @Override
    public String getByPrefix(String prefix)
    { 
        return prefixToURI.get(prefix) ;
    }

    @Override
    public String getByURI(String uri) 
    { 
        return URItoPrefix.get(uri) ;
    }

    @Override
    public void set(String prefix, String uri)
    {
        prefixToURI.put(prefix, uri) ;
        URItoPrefix.put(uri, prefix) ;
    }
    

    @Override
    public void removeByPrefix(String prefix)
    {
        String uri = getByPrefix(prefix) ;
        remove(prefix, uri) ;
    }
    

    @Override
    public void removeByURI(String uri)
    {
        String prefix = getByURI(uri) ;
        remove(prefix, uri) ;
    }

    private void remove(String prefix, String uri)
    {
        if ( prefix != null )
            prefixToURI.remove(prefix) ;
        // May have had (x, uri) , (y, uri)
        regenerateReverseMapping() ;
    }

    protected void regenerateReverseMapping()
    {
        URItoPrefix.clear();
        for ( Entry<String, String> e : prefixToURI.entrySet() )
            URItoPrefix.put( e.getValue(), e.getKey() );
    }

    @Override
    public PrefixMappingEntry findPrefixFor(String uri)
    {
        Iterator<Entry<String,String>> it = prefixToURI.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String,String> e = it.next();
            String ss = e.getValue();
            if (uri.startsWith( ss ) ) 
                return new PrefixMappingEntry(e.getKey(), e.getValue()) ;
        } 
        return null;         
    }


    @Override
    public Iterator<PrefixMappingEntry> getEntries()
    {
        List<PrefixMappingEntry> x = new ArrayList<PrefixMappingEntry>() ;
        Iterator<Entry<String,String>> it = prefixToURI.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String,String> e = it.next();
            x.add(new PrefixMappingEntry(e.getKey(), e.getValue())) ;
        } 
        return x.iterator(); 
    }
    
 
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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