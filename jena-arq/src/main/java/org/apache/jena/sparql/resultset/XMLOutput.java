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

package org.apache.jena.sparql.resultset;

import java.io.OutputStream ;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.rw.ResultSetWriterXML;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.sparql.util.Context;

public class XMLOutput extends OutputBase
{
    String stylesheetURL = null ;
    boolean includeXMLinst = true ;
    
    public XMLOutput() {}

    public XMLOutput(String stylesheetURL) {
        setStylesheetURL(stylesheetURL);
    }

    public XMLOutput(boolean includeXMLinst) {
        setIncludeXMLinst(includeXMLinst);
    }
    
    public XMLOutput(boolean includeXMLinst, String stylesheetURL) {
        setStylesheetURL(stylesheetURL);
        setIncludeXMLinst(includeXMLinst);
    }

    @Override
    public void format(OutputStream out, ResultSet resultSet) {
        Context cxt = ARQ.getContext().copy();
        if ( stylesheetURL != null )
            cxt.set(ResultSetWriterXML.xmlStylesheet, stylesheetURL);
        cxt.set(ResultSetWriterXML.xmlInstruction, includeXMLinst);
        ResultsWriter.create()
            .context(cxt)
            .lang(ResultSetLang.SPARQLResultSetXML)
            .write(out, resultSet);
    }

    /** @return Returns the includeXMLinst. */
    public boolean getIncludeXMLinst()
    { return includeXMLinst ; }
    
    /** @param includeXMLinst The includeXMLinst to set. */
    public void setIncludeXMLinst(boolean includeXMLinst)
    { this.includeXMLinst = includeXMLinst ; }

    /** @return Returns the stylesheetURL. */
    public String getStylesheetURL()
    { return stylesheetURL ; }
    
    /** @param stylesheetURL The stylesheetURL to set. */
    public void setStylesheetURL(String stylesheetURL)
    { this.stylesheetURL = stylesheetURL ; }
    
    @Override
    public void format(OutputStream out, boolean booleanResult) {
        Context cxt = ARQ.getContext().copy();
        if ( stylesheetURL != null )
            cxt.set(ResultSetWriterXML.xmlStylesheet, stylesheetURL);
        cxt.set(ResultSetWriterXML.xmlInstruction, includeXMLinst);
        ResultsWriter.create()
            .context(cxt)
            .lang(ResultSetLang.SPARQLResultSetXML)
            .build()
            .write(out, booleanResult);
    }
}
