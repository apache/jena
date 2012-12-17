/**
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

package org.apache.jena.riot;

import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.rdf.model.RDFReaderF ;

public class RDFReaderFactoryRIOT implements RDFReaderF
{

    // Map language to base.
    // .. crudely ... 
    
    @Override
    public RDFReader getReader()
    {
        return new RDFReaderRIOT() ;
    }

    @Override
    public RDFReader getReader(String langname)
    {
        // If RDF/XML, then return an ARP 
        
        Lang2 lang = RDFLanguages.nameToLang(langname) ;
        if ( RDFLanguages.RDFXML.equals(lang) )
            return new RDFReaderRIOT_ARP() ;
        return new RDFReaderRIOT(langname) ; 
    }

    @Override
    public String setReaderClassName(String lang, String className)
    {
        return null ;
    }
}

