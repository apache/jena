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

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note(1):</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(Writer, String, String) writeAll }.
     * <strong>Note(2):</strong> it is often better to use an {@code OutputStream} rather than a {@code Writer},
     * since this will avoid character encoding errors.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param writer the output writer
     * @return this model
     */
    @Deprecated
    @Override
    R write(Writer writer);

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note(1):</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(Writer, String) writeAll }.
     * <strong>Note(2):</strong> it is often better to use an {@code OutputStream} rather than a {@code Writer},
     * since this will avoid character encoding errors.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param writer the output writer
     * @param lang   the language in which the RDF should be written
     * @return this model
     */
    @Override
    R write(Writer writer, String lang);

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note(1):</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(Writer, String, String) writeAll }.
     * <strong>Note(2):</strong> it is often better to use an {@code OutputStream} rather than a {@code Writer},
     * since this will avoid character encoding errors.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param writer the output writer
     * @param lang   the language in which the RDF should be written
     * @param base   the base URI for relative URI calculations;
     *               {@code null} means use only absolute URI's.
     * @return this model
     */
    @Override
    R write(Writer writer, String lang, String base);

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note:</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(OutputStream, String, String) writeAll }.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param out tre output stream to which the RDF is written
     * @return this model
     */
    @Override
    R write(OutputStream out);

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note:</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(OutputStream, String) writeAll }.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param out  tre output stream to which the RDF is written
     * @param lang the language in which the RDF should be written
     * @return this model
     */
    @Override
    R write(OutputStream out, String lang);

    /**
     * Writes a serialized representation of a model in a specified language.
     * <strong>Note:</strong> this method is adapted for the ontology
     * model to write out only the base model (which contains the asserted data).
     * To write all triples, including imported data and inferred triples, use
     * {@link #writeAll(OutputStream, String, String) writeAll }.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param out  tre output stream to which the RDF is written
     * @param lang the language in which the RDF should be written
     * @param base the base URI for relative URI calculations;
     *             {@code null} means use only absolute URI's.
     * @return this model
     */
    @Override
    R write(OutputStream out, String lang, String base);

    /**
     * Writes a serialized representation of all the model's contents,
     * including inferred statements and statements imported from other documents.
     * To write only the data asserted in the base model, use
     * {@link #write(Writer, String, String) write}.
     * <strong>Note:</strong> it is often better to use an {@code OutputStream} rather than a {@code Writer},
     * since this will avoid character encoding errors.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param writer the output writer
     * @param lang   the language in which the RDF should be written
     * @param base   the base URI for relative URI calculations;
     *               {@code null} means use only absolute URI's.
     * @return this model
     */
    R writeAll(Writer writer, String lang, String base);

    /**
     * Writes a serialized representation of all the model's contents,
     * including inferred statements and statements imported from other documents.
     * To write only the data asserted in the base model, use
     * {@link #write(Writer, String) write}.
     * <strong>Note:</strong> it is often better to use an {@code OutputStream} rather than a {@code Writer},
     * since this will avoid character encoding errors.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param writer the output writer
     * @param lang   the language in which the RDF should be written
     * @return this model
     */
    R writeAll(Writer writer, String lang);

    /**
     * Writes a serialized representation of all the model's contents,
     * including inferred statements and statements imported from other documents.
     * To write only the data asserted in the base model, use
     * {@link #write(OutputStream, String, String) write}.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param out  tre output stream to which the RDF is written
     * @param lang the language in which the RDF should be written
     * @param base the base URI for relative URI calculations;
     *             {@code null} means use only absolute URI's.
     * @return this model
     */
    R writeAll(OutputStream out, String lang, String base);

    /**
     * Writes a serialized representation of all the model's contents,
     * including inferred statements and statements imported from other documents.
     * To write only the data asserted in the base model, use
     * {@link #write(OutputStream, String) write}.
     * <p>
     * The language in which to write the model is specified by the {@code lang} argument.
     * Some of the supported formats are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE".
     * See {@link org.apache.jena.riot.Lang Lang} for all supported formats.
     * The default value, represented by {@code null}, is "RDF/XML".
     *
     * @param out  tre output stream to which the RDF is written
     * @param lang the language in which the RDF should be written
     * @return this model
     */
    R writeAll(OutputStream out, String lang);
}
