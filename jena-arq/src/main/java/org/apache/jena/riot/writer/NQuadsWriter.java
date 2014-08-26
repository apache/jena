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

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Iterator ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.out.CharSpace ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;

public class NQuadsWriter extends WriterDatasetRIOTBase
{
    public static void write(OutputStream out, Iterator<Quad> iter)
    {
        write(out, iter, CharSpace.UTF8);
    }
    
    public static void write(OutputStream out, Iterator<Quad> iter, CharSpace charSpace)
    {
        StreamRDF s = StreamRDFLib.writer(out, charSpace) ;
        write$(s, iter) ;
    }
    
    public static void write(Writer out, Iterator<Quad> iter)
    {
        write(out, iter, CharSpace.UTF8);
    }
    
    public static void write(Writer out, Iterator<Quad> iter, CharSpace charSpace)
    {
        StreamRDF s = StreamRDFLib.writer(out, charSpace) ;
        write$(s, iter) ;
    }

    private static void write$(StreamRDF s, Iterator<Quad> iter)
    {
        s.start() ;
        StreamOps.sendQuadsToStream(iter, s) ;
        s.finish();
    }
    
    private final CharSpace charSpace ;
    
    public NQuadsWriter()
    { this(CharSpace.UTF8); }  
    
    public NQuadsWriter(CharSpace charSpace)
    { 
        this.charSpace = charSpace ;
    }
    
    @Override
    public Lang getLang()
    {
        return Lang.NQUADS ;
    }

    @Override
    public void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context)
    {
        write(out, dataset.find(null, null, null, null), this.charSpace) ;
    }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context)
    {
        write(out, dataset.find(null, null, null, null), this.charSpace) ;
    }
}
