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

package org.apache.jena.ontapi.model;

import org.apache.jena.rdf.model.Model;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A technical interface that describes model I/O operations.
 * Contains overridden read/write methods inherited from {@link Model}.
 *
 * @param <R> - subtype of {@link Model}, the model to return
 * @see <a href="http://jena.apache.org/documentation/io/index.html">"Reading and Writing RDF in Apache Jena"</a>
 */
interface IOModel<R extends Model> extends Model {

    @Override
    R read(String url);

    @Override
    R read(InputStream in, String base);

    @Override
    R read(InputStream in, String base, String lang);

    @Override
    R read(Reader reader, String base);

    @Override
    R read(String url, String lang);

    @Override
    R read(Reader reader, String base, String lang);

    @Override
    R read(String url, String base, String lang);

    @Override
    R write(Writer writer);

    @Override
    R write(Writer writer, String lang);

    @Override
    R write(Writer writer, String lang, String base);

    @Override
    R write(OutputStream out);

    @Override
    R write(OutputStream out, String lang);

    @Override
    R write(OutputStream out, String lang, String base);

}
