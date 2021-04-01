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

package org.apache.jena.riot.resultset.rw;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;

public class ResultSetWriterText implements ResultSetWriter {

    public static ResultSetWriterFactory factory = lang -> {
        if ( !Objects.equals(lang, ResultSetLang.RS_Text) )
            throw new ResultSetException("ResultSetWriter for Text asked for a " + lang);
        return new ResultSetWriterText();
    };

    private static final String NL = "\n" ;
    private static final String notThere = " " ;

    private ResultSetWriterText() {}

    @Override
    public void write(OutputStream out, ResultSet resultSet, Context context) {
        output(IO.wrapUTF8(out), resultSet, context);
    }

    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        output(IO.wrap(out), resultSet, context);
    }

    @Override
    public void write(OutputStream out, boolean result, Context context) {
        output(IO.wrapUTF8(out), result, context);
    }

    private void output(AWriter out, boolean result, Context context) {
        if ( result )
            out.write("yes");
        else
           out.write("no");
        out.write(NL);
        out.flush();
    }

    private static int[] colWidths(ResultSetRewindable rs, SerializationContext context) {
        int numCols = rs.getResultVars().size() ;
        int numRows = 0 ;
        int[] colWidths = new int[numCols] ;

        // Widths at least that of the variable name.  Assumes we will print col headings.
        for ( int i = 0 ; i < numCols ; i++ )
            colWidths[i] = (rs.getResultVars().get(i)).length() ;

        // Preparation pass : find the maximum width for each column
        for ( ; rs.hasNext() ; ) {
            numRows++ ;
            QuerySolution rBind = rs.nextSolution() ;
            int col = -1 ;
            for ( String s1 : rs.getResultVars() )
            {
                col++;
                String rVar = s1;
                String s = getVarValueAsString( rBind, rVar, context);
                if ( colWidths[col] < s.length() )
                {
                    colWidths[col] = s.length();
                }
            }
        }
        rs.reset() ;
        return colWidths ;
    }

    public static void output(AWriter out, ResultSet resultSet, String colStart, String colSep, String colEnd) {
        output(out, resultSet, colStart, colSep, colEnd, null);
    }


    public static void output(AWriter out, ResultSet resultSet, Context cxt) {
        output(out, resultSet, "| ", " | ", " |", cxt);
    }

    public static void output(AWriter out, ResultSet resultSet, String colStart, String colSep, String colEnd, Context cxt) {
        output$(out, resultSet, colStart, colSep, colEnd, cxt);
    }

    private static void output$(AWriter out, ResultSet resultSet, String colStart, String colSep, String colEnd, Context cxt) {
        Prologue prologue = choosePrologue(resultSet, cxt);
        SerializationContext context = new SerializationContext(prologue) ;

        try {
            if ( resultSet.getResultVars().size() == 0 ) {
                out.println("==== No variables ====");
            }

            ResultSetRewindable resultSetRewindable = resultSet.rewindable();

            int numCols = resultSetRewindable.getResultVars().size();
            int[] colWidths = colWidths(resultSetRewindable, context);

            String row[] = new String[numCols];
            int lineWidth = 0;
            for ( int col = 0 ; col < numCols ; col++ ) {
                String rVar = resultSet.getResultVars().get(col);
                row[col] = rVar;
                lineWidth += colWidths[col];
                if ( col > 0 )
                    lineWidth += colSep.length();
            }
            if ( colStart != null )
                lineWidth += colStart.length() ;
            if ( colEnd != null )
                lineWidth += colEnd.length() ;

            for ( int i = 0 ; i < lineWidth ; i++ )
                out.print('-') ;
            out.println() ;

            printRow(out, row, colWidths, colStart, colSep, colEnd) ;

            for ( int i = 0 ; i < lineWidth ; i++ )
                out.print('=') ;
            out.println() ;

            for ( ; resultSetRewindable.hasNext() ; ) {
                QuerySolution rBind = resultSetRewindable.nextSolution();
                for ( int col = 0 ; col < numCols ; col++ ) {
                    String rVar = resultSet.getResultVars().get(col);
                    row[col] = getVarValueAsString(rBind, rVar, context);
                }
                printRow(out, row, colWidths, colStart, colSep, colEnd);
            }
            for ( int i = 0 ; i < lineWidth ; i++ )
                out.print('-') ;
            out.println() ;
            resultSetRewindable = null ;
        } finally { out.flush(); }
    }

    private static void printRow(AWriter out, String[] row, int[] colWidths, String rowStart, String colSep, String rowEnd) {
        out.print(rowStart);
        for ( int col = 0 ; col < colWidths.length ; col++ ) {
            String s = row[col];
            int pad = colWidths[col];
            StringBuffer sbuff = new StringBuffer(120);

            if ( col > 0 )
                sbuff.append(colSep);

            sbuff.append(s);
            for ( int j = 0 ; j < pad - s.length() ; j++ )
                sbuff.append(' ');
            out.print(sbuff.toString());
        }
        out.print(rowEnd);
        out.println();
    }

    private static String getVarValueAsString(QuerySolution rBind, String varName, SerializationContext context) {
        RDFNode obj = rBind.get(varName);

        if ( obj == null )
            return notThere;

        return FmtUtils.stringForRDFNode(obj, context);
    }

    /** Establish a prologue for formatting output.  Return "null" for none found. */
    private static Prologue choosePrologue(ResultSet resultSet, Context context) {
        try {
            if ( context != null && context.get(ARQConstants.symPrologue) != null )
                return context.get(ARQConstants.symPrologue);
            Model m = resultSet.getResourceModel();
            if ( m != null )
                return new Prologue(m);
        } catch (Exception ex) {
            FmtLog.warn(ARQ.getExecLogger(), "Failed to establish a 'Prologue' for text output: %s", ex.getMessage());
        }
        return null;
    }


}
