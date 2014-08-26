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

package org.apache.jena.riot.writer;

import static org.apache.jena.riot.out.CharSpace.ASCII ;
import static org.apache.jena.riot.out.CharSpace.UTF8 ;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Iterator ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.Context ;

public class NTriplesWriter extends WriterGraphRIOTBase
{
    public static void write(OutputStream out, Iterator<Triple> iter)
    {
        write(out, iter, CharSpace.UTF8);
    }
    
    public static void write(OutputStream out, Iterator<Triple> iter, CharSpace charSpace)
    {
        StreamRDF s = StreamRDFLib.writer(out, charSpace) ;
        write$(s, iter) ;
    }
    
    public static void write(Writer out, Iterator<Triple> iter)
    {
        write(out, iter, CharSpace.UTF8);
    }
    
    public static void write(Writer out, Iterator<Triple> iter, CharSpace charSpace)
    {
        StreamRDF s = StreamRDFLib.writer(out, charSpace) ;
        write$(s, iter) ;
    }

    private static void write$(StreamRDF s, Iterator<Triple> iter)
    {
        s.start() ;
        StreamOps.sendTriplesToStream(iter, s) ;
        s.finish();
    }

    private final CharSpace charSpace ;

    public NTriplesWriter()
    { this(UTF8); }  
    
    public NTriplesWriter(CharSpace charSpace)
    { 
        this.charSpace = charSpace ;
    }

    @Override
    public Lang getLang()
    {
        return Lang.NTRIPLES ;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        Iterator<Triple> iter = graph.find(null, null, null) ;
        if ( charSpace == UTF8 )
            write(out, iter) ;
        else
        {
            StreamRDF s = new WriterStreamRDFPlain(IO.wrap(out), ASCII) ;
            write$(s, iter) ;
        }
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        Iterator<Triple> iter = graph.find(null, null, null) ;
        if ( charSpace == UTF8 )
            write(out, iter) ;
        else
        {
            StreamRDF s = new WriterStreamRDFPlain(IO.wrapASCII(out), ASCII) ;
            write$(s, iter) ;
        }
     
    }
}
