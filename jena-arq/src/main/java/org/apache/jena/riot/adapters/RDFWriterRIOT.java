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

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.Locale ;
import java.util.Map ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFErrorHandler ;
import org.apache.jena.rdf.model.RDFWriterI ;
import org.apache.jena.rdf.model.impl.RDFDefaultErrorHandler ;
import org.apache.jena.riot.* ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;

/**
 * This class is used for indirecting all model.write calls to RIOT. It
 * implements Jena core {@link RDFWriterI} can calls {@link WriterGraphRIOT}.
 * <p>
 * For RDF/XML, that {@link WriterGraphRIOT} is a {@link AdapterRDFWriter} that
 * calls the old style {@link RDFWriterI} interface.
 * <p>
 * {@link AdapterRDFWriter} is a {@link WriterGraphRIOT} over a
 * {@link RDFWriterI}.
 */
public class RDFWriterRIOT implements RDFWriterI
{
    // ---- Compatibility
    private final String basename ;
    private final String jenaName ;
    private Context context = null;
    private Map<String, Object> properties = null;
    private RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    public RDFWriterRIOT(String jenaName) {
        this.basename = (jenaName==null)
                ? "org.apache.jena.riot.writer.generic"
                : "org.apache.jena.riot.writer." + jenaName.toLowerCase(Locale.ROOT) ;

        this.jenaName = jenaName;

    }

    protected RDFWriterBuilder writer() {
        if ( jenaName == null )
            throw new IllegalArgumentException("Jena writer name is null");
        // For writing via model.write(), use any old names for jena writers.
        RDFWriterBuilder builder = org.apache.jena.riot.RDFWriter.create();
        Lang lang = RDFLanguages.nameToLang(jenaName);
        if ( lang != null )
            return builder.lang(lang);
        throw new RiotException("No graph writer for '" + jenaName + "'");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void write(Model model, Writer out, String base) {
        if ( base != null && base.equals("") )
            base = null;
        writer().source(model).context(context).base(base).build().output(out);
    }

    @Override
    public void write(Model model, OutputStream out, String base) {
        if ( base != null && base.equals("") )
            base = null;
        writer().source(model).context(context).base(base).output(out);
    }

    @Override
    public Object setProperty(String propName, Object propValue) {
        if ( context == null ) {
            context = RIOT.getContext().copy();
            properties = new HashMap<>() ;
            context.put(SysRIOT.sysRdfWriterProperties, properties);
        }
        Symbol sym = Symbol.create(basename + "#" + propName);
        Object oldObj = properties.get(propName);
        properties.put(propName, propValue) ;
        return oldObj;
    }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = errorHandler;
        errorHandler = errHandler;
        return old;
    }
}
