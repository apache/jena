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

package org.apache.jena.riot.adapters;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;

import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.rdf.model.RDFReaderF ;

/** Adapter to old style Jena reader factory */ 
public class RDFReaderFactoryRIOT implements RDFReaderF
{
    public RDFReaderFactoryRIOT() {}
    
    @Override
    public RDFReader getReader()
    {
        return new RDFReaderRIOT() ;
    }

    @Override
    public RDFReader getReader(String langname)
    {
        // If RDF/XML, then return an ARP 
        
        Lang lang = RDFLanguages.nameToLang(langname) ;
        if ( RDFLanguages.RDFXML.equals(lang) )
            return new RDFReaderRIOT_ARP() ;
        return new RDFReaderRIOT(langname) ; 
    }

    @Override
    public String setReaderClassName(String lang, String className)
    {
        return null ;
    }

	@Override
	public void resetRDFReaderF() {
		// does nothing as the reader can not be modified.
		
	}

	@Override
	public String removeReader(String lang) throws IllegalArgumentException {
		return null;
	}
}

