/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Map ;
import java.util.Set ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;

public class DatasetPrefixStorageLogger implements DatasetPrefixStorage
{
    private final DatasetPrefixStorage other ;
    private String label = null ;
    private final static Logger log = LoggerFactory.getLogger(DatasetPrefixStorage.class) ;

    public DatasetPrefixStorageLogger(DatasetPrefixStorage other)
    {
        this.other = other ;
    }

    @Override
    public void close()     { info("close") ; }

    @Override
    public void sync()      { info("sync") ; }

    @Override
    public Set<String> graphNames()
    {
        Set<String> x = other.graphNames() ;
        info("graphNames:"+x) ;
        return x ;
    }

    @Override
    public String readPrefix(String graphName, String prefix)
    {
        String x = other.readPrefix(graphName, prefix) ;
        info("readPrefix("+graphName+", "+prefix+") -> "+x) ;
        return x ;
    }

    @Override
    public String readByURI(String graphName, String uriStr)
    {
        String x = other.readByURI(graphName, uriStr) ;
        info("readByURI("+graphName+", "+uriStr+") -> "+x) ;
        return x ;
    }

    @Override
    public Map<String, String> readPrefixMap(String graphName)
    {
        Map<String, String> x = other.readPrefixMap(graphName) ;
        info("readPrefixMap("+graphName+") -> "+x) ;
        return x ;
    }

    @Override
    public void insertPrefix(String graphName, String prefix, String uri)
    {
        info("insertPrefix("+graphName+", "+prefix+", "+uri+")") ;
        other.insertPrefix(graphName, prefix, uri) ;
    }

    @Override
    public void loadPrefixMapping(String graphName, PrefixMapping pmap)
    {
        info("loadPrefixMapping("+graphName+", "+pmap+")") ;
        other.loadPrefixMapping(graphName, pmap) ;
    }

    @Override
    public void removeFromPrefixMap(String graphName, String prefix, String uri)
    {
        info("removeFromPrefixMap("+graphName+", "+prefix+", "+uri+")") ;
        other.removeFromPrefixMap(graphName, prefix, uri) ;
    }

    @Override
    public PrefixMapping getPrefixMapping()
    {
        PrefixMapping x = other.getPrefixMapping() ;
        info("getPrefixMapping() -> "+x) ;
        return x ;
    }

    @Override
    public PrefixMapping getPrefixMapping(String graphName)
    {
        PrefixMapping x = other.getPrefixMapping(graphName) ;
        info("getPrefixMapping("+graphName+") -> "+x) ;
        return x ;
    }

    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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