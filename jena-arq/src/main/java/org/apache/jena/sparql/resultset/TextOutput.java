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
import java.io.Writer ;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.rw.ResultSetWriterText;
import org.apache.jena.riot.resultset.rw.ResultsWriter;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.serializer.SerializationContext ;

/** <p>Takes a ResultSet object and creates displayable formatted output in plain text.</p>
 *
 *  <p>Note: this is compute intensive and memory intensive.
 *  It needs to read all the results first (all the results are then in-memory)
 *  in order to find things the maximum width of a column value; then it needs
 *  to pass over the results again, turning them into output.
 *  </p>
 * @see org.apache.jena.query.ResultSetFormatter for convenience ways to call this formatter
 * @deprecated This will become an internal class. Use
 *     {@link ResultSetFormatter#output} or
 *     {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(...)}
 */
@Deprecated
public class TextOutput extends OutputBase
{
    //?? ResultSetProcessor to find column widths over a ResultSetRewindable and to output text

    private SerializationContext context = null ;

    @Deprecated
    public TextOutput(Prologue prologue)
    { context = new SerializationContext(prologue) ; }

    @Deprecated
    public TextOutput(PrefixMapping pMap)
    { context = new SerializationContext(pMap) ; }

    @Deprecated
    public TextOutput(SerializationContext cxt )
    { context = cxt ; }

    /**
     * @deprecated Use {@link ResultSetFormatter#out(OutputStream, ResultSet)} or
     *     {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);}
     */
    @Deprecated
    @Override
    public void format(OutputStream out, ResultSet resultSet) {
        ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);
    }

    /**
     * @deprecated Use {@link ResultSetFormatter#out(OutputStream, boolean)} or
     *     {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, booleanResult);}
     */
    @Deprecated
    @Override
    public void format(OutputStream out, boolean booleanResult) {
        ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, booleanResult);
    }

    /** Writer should be UTF-8 encoded - better to an OutputStream
     * @deprecated Use {@link ResultSetFormatter#out(OutputStream, ResultSet)} or
     *      {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);}
     */
    @Deprecated
    public void format(Writer out, ResultSet resultSet) {
        ResultSetWriterText.output(IO.wrap(out), resultSet, null);
    }

    /** Textual representation : default layout using " | " to separate columns.
     *  Ensure the PrintWriter can handle UTF-8.
     *  OutputStream version is preferred.
     *  @param out         A Writer
     *  @param resultSet  ResultSet
     * @deprecated Use {@link ResultSetFormatter#out(OutputStream, ResultSet)} or
     *      {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);}
     */
    @Deprecated
    public void write(Writer out, ResultSet resultSet) {
        ResultSetWriterText.output(IO.wrap(out), resultSet, null);
    }

    /** Output a result set.
     * @param out        OutputStream
     * @param resultSet  ResultSet
     * @deprecated Use {@link ResultSetFormatter#out(OutputStream, ResultSet)} or
     *      {@code ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);}
     */
    @Deprecated
    public void write(OutputStream out, ResultSet resultSet) {
        ResultsWriter.create().lang(ResultSetLang.RS_Text).write(out, resultSet);
    }

    /** Output a result set.
     * @param out        OutputStream
     * @param resultSet  ResultSet
     * @param colStart   Left column
     * @param colSep     Inter-column
     * @param colEnd     Right column
     */
    public void write(OutputStream out, ResultSet resultSet, String colStart, String colSep, String colEnd) {
        ResultSetWriterText.output(IO.wrapUTF8(out), resultSet, colStart, colSep, colEnd);
    }

    /** Textual representation : layout using given separator.
     *  Ensure the PrintWriter can handle UTF-8.
     *  @param out         Writer
     *  @param colSep      Column separator
     *  @deprecated Use an java.io.OutputStream
     */
    @Deprecated
    public void write(Writer out, ResultSet resultSet, String colStart, String colSep, String colEnd) {
        ResultSetWriterText.output(IO.wrap(out), resultSet, colStart, colSep, colEnd);
    }
}
