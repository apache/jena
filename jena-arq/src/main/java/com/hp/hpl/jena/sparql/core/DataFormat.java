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

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.TranslationTable ;
import com.hp.hpl.jena.util.FileUtils ;


public class DataFormat extends Symbol
{
    public static final DataFormat langNTriples
                    = new DataFormat(FileUtils.langNTriple) ;
    
    public static final DataFormat langXML
                    = new DataFormat(FileUtils.langXML ) ;
    
    public static final DataFormat langXMLAbbrev
                    = new DataFormat(FileUtils.langXMLAbbrev) ;
    
    public static final DataFormat langTurtle
                    = new DataFormat(FileUtils.langTurtle ) ;
    
    public static final DataFormat langN3
                    = new DataFormat(FileUtils.langN3 ) ;

    public static TranslationTable<DataFormat> dataSyntaxNames = new TranslationTable<>(true) ;
    static {
        dataSyntaxNames.put("nt",           langNTriples ) ;
        dataSyntaxNames.put("n3",           langN3 ) ;
        dataSyntaxNames.put("n-triples",    langNTriples ) ;
        dataSyntaxNames.put("n-triple",     langNTriples ) ;
        dataSyntaxNames.put("xml",          langXML ) ;
        dataSyntaxNames.put("rdf",          langXML ) ; 
        dataSyntaxNames.put("rdf/xml",      langXML ) ; 
        dataSyntaxNames.put("turtle",       langTurtle ) ;
    }
    
    protected DataFormat(String s) { super(s) ; }
    protected DataFormat(DataFormat s) { super(s) ; }
    
    /** Short name to proper name (symbol)
     * 
     */
    public static DataFormat lookup(String s)
    {
        return dataSyntaxNames.lookup(s) ;
    }
}
