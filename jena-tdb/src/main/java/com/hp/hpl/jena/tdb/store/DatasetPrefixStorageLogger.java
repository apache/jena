/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    public void removeFromPrefixMap(String graphName, String prefix)
    {
        info("removeFromPrefixMap("+graphName+", "+prefix+")") ;
        other.removeFromPrefixMap(graphName, prefix) ;
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
