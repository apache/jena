/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.core_ttl;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;

import org.apache.jena.core_ttl.parser.ParserTurtle;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.irix.IRIs;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.*;
import org.apache.jena.util.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * A jena-core style RDFReaderI for the jena-core test-only Turtle parser.
 */
public class TurtleTestReader implements RDFReaderI {
    protected RDFErrorHandler errorHandler = null;

    public TurtleTestReader() {}

    @Override
    final public void read(Model model, Reader r, String base) {
        checkReader(r);
        readImpl(model, r, base);
    }

    @Override
    final public void read(Model model, java.lang.String url) {
        try {
            URLConnection conn = new URI(url).toURL().openConnection();
            String encoding = conn.getContentEncoding();
            read(model, new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8), url);
        } catch (JenaException e) {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e);
        } catch (Exception ex) {
            if ( errorHandler == null )
                throw new JenaException(ex);
            errorHandler.error(ex);
        }
    }

    @Override
    final public void read(Model model, InputStream in, String base) {
        readImpl(model, FileUtils.asBufferedUTF8(in), base);
    }

    @Override
    final public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = errorHandler;
        errorHandler = errHandler;
        return old;
    }

    @Override
    final public Object setProperty(String propName, Object propValue) {
        return null;
    }

    protected void checkReader(Reader r) {
        if ( r instanceof FileReader ) {
            FileReader f = (FileReader)r;
            if ( f.getEncoding().equalsIgnoreCase(StandardCharsets.UTF_8.name()) )
                LoggerFactory.getLogger(this.getClass()).warn("FileReader is not UTF-8");
        }
    }

    private void readImpl(Model model, Reader reader, String base) {
        // The reader has been checked, if possible, by now or
        // constructed correctly by code here.
        if ( base != null )
            base = IRIs.resolve(base);
        try {
            model.notifyEvent(GraphEvents.startRead);
            readWorker(model, reader, base);
        } catch (JenaException e) {
            if ( errorHandler == null )
                throw e;
            errorHandler.error(e);
        } catch (Exception ex) {
            if ( errorHandler == null )
                throw new JenaException(ex);
            errorHandler.error(ex);
        } finally {
            model.notifyEvent(GraphEvents.finishRead);
        }
    }

    protected void readWorker(Model model, Reader reader, String base)
    {
        ParserTurtle p =  new ParserTurtle();
        p.parse( model.getGraph(), base, reader );
    }
}
