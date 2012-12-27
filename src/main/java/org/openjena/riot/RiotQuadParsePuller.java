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

package org.openjena.riot;

import java.io.InputStream ;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.system.SinkRDF ;
import org.apache.jena.riot.system.SinkRDFLib ;

import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * A RiotParsePuller that operates on Quads.
 * 
 * @see RiotParsePuller
 */
public class RiotQuadParsePuller extends RiotParsePuller<Quad>
{
    public RiotQuadParsePuller(InputStream in, Lang lang, String baseIRI)
    {
        super(in, lang, baseIRI) ;
    }
    
    @Override
    protected LangRIOT createParser(Sink<Quad> sink)
    {
        SinkRDF dest = SinkRDFLib.sinkQuads(sink) ;
        return RiotReader.createParser(in, lang, baseIRI, dest) ;
    }
}

