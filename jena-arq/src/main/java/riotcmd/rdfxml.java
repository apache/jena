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

package riotcmd;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;

import com.hp.hpl.jena.sparql.util.Utils ;

/** Run the RDf/XML parser - and produce triples */
public class rdfxml extends CmdLangParse
{
    public static void main(String... argv)
    {
        new rdfxml(argv).mainRun() ;
    }    
    
    protected rdfxml(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getCommandName()
    {
        return Utils.classShortName(rdfxml.class) ;
    }
    
    @Override
    protected Lang selectLang(String filename  , ContentType contentType  , Lang lang  ) 
    { return RDFLanguages.RDFXML ; }
}
