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

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream ;
import java.io.PrintWriter ;
import java.io.Writer ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.query.ResultSetRewindable ;

/** <p>Takes a ResultSet object and creates displayable formatted output in plain text.</p>
 *
 *  <p>Note: this is compute intensive and memory intensive.
 *  It needs to read all the results first (all the results are then in-memory)
 *  in order to find things the maximum width of a column value; then it needs
 *  to pass over the results again, turning them into output.
 *  </p>
 * @see com.hp.hpl.jena.query.ResultSetFormatter for convenience ways to call this formatter
 */

public class TextOutput extends OutputBase
{
    //?? ResultSetProcessor to find column widths over a ResultSetRewindable and to output text

    protected SerializationContext context = null ;
    
    //static final String notThere = "<<unset>>" ;
    static final String notThere = " " ;
    
    public TextOutput(Prologue prologue)
    { context = new SerializationContext(prologue) ; }
    
    public TextOutput(PrefixMapping pMap)
    { context = new SerializationContext(pMap) ; }

    public TextOutput(SerializationContext cxt )
    { context = cxt ; }

    @Override
    public void format(OutputStream outs, ResultSet resultSet)
    { write(outs, resultSet) ; }

    /** Writer should be UTF-8 encoded - better to an OutputStream */ 
    public void format(Writer w, ResultSet resultSet)
    { 
        PrintWriter pw = new PrintWriter(w) ;
        write(pw, resultSet) ;
        pw.flush() ;
    }

    private int[] colWidths(ResultSetRewindable rs)
    {
        int numCols = rs.getResultVars().size() ;
        int numRows = 0 ;
        int[] colWidths = new int[numCols] ;

        // Widths at least that of the variable name.  Assumes we will print col headings.
        for ( int i = 0 ; i < numCols ; i++ )
            colWidths[i] = (rs.getResultVars().get(i)).length() ;

        // Preparation pass : find the maximum width for each column
        for ( ; rs.hasNext() ; )
        {
            numRows++ ;
            QuerySolution rBind = rs.nextSolution() ;
            int col = -1 ;
            for ( String s1 : rs.getResultVars() )
            {
                col++;
                String rVar = s1;
                String s = getVarValueAsString( rBind, rVar );
                if ( colWidths[col] < s.length() )
                {
                    colWidths[col] = s.length();
                }
            }
        }
        rs.reset() ;
        return colWidths ;
    }

    /** Textual representation : default layout using " | " to separate columns.
     *  Ensure the PrintWriter can handle UTF-8.
     *  OutputStream version is preferred.
     *  @param pw         A PrintWriter
     *  @param resultSet  ResultSet
     */
    public void write(PrintWriter pw, ResultSet resultSet)
    { write(pw, resultSet, "| ", " | ", " |") ; }
    
    /** Output a result set. 
     * @param outs       OutputStream
     * @param resultSet  ResultSet
     */
    public void write(OutputStream outs, ResultSet resultSet)
    { write(outs, resultSet, "| ", " | ", " |") ; }
    
    /** Output a result set. 
     * @param outs       OutputStream
     * @param resultSet  ResultSet
     * @param colStart   Left column
     * @param colSep     Inter-column
     * @param colEnd     Right column
     */
    public void write(OutputStream outs, ResultSet resultSet, String colStart, String colSep, String colEnd)
    {
        PrintWriter pw = FileUtils.asPrintWriterUTF8(outs) ;
        write(pw, resultSet, colStart, colSep, colEnd) ;
        pw.flush() ;
    }
    
    /** Textual representation : layout using given separator.
     *  Ensure the PrintWriter can handle UTF-8.
     *  @param pw         PrintWriter
     *  @param colSep      Column separator
     */
    public void write(PrintWriter pw, ResultSet resultSet, String colStart, String colSep, String colEnd)
    {
        if ( resultSet.getResultVars().size() == 0 )
        {
            pw.println("==== No variables ====") ;
            //return ;
        }

        ResultSetRewindable resultSetRewindable = ResultSetFactory.makeRewindable(resultSet) ; 
        
        int numCols = resultSetRewindable.getResultVars().size() ;
        int[] colWidths = colWidths(resultSetRewindable) ;

        String row[] = new String[numCols] ;
        int lineWidth = 0 ;
        for ( int col = 0 ; col < numCols ; col++ )
        {
            String rVar = resultSet.getResultVars().get(col) ;
            row[col] = rVar ;
            lineWidth += colWidths[col] ;
            if ( col > 0 )
                lineWidth += colSep.length() ;
        }
        if ( colStart != null )
            lineWidth += colStart.length() ;
        if ( colEnd != null )
            lineWidth += colEnd.length() ; 

        for ( int i = 0 ; i < lineWidth ; i++ )
            pw.print('-') ;
        pw.println() ;
        
        printRow(pw, row, colWidths, colStart, colSep, colEnd) ;

        for ( int i = 0 ; i < lineWidth ; i++ )
            pw.print('=') ;
        pw.println() ;

        for ( ; resultSetRewindable.hasNext() ; )
        {
            QuerySolution rBind = resultSetRewindable.nextSolution() ;
            for ( int col = 0 ; col < numCols ; col++ )
            {
                String rVar = resultSet.getResultVars().get(col) ;
                row[col] = this.getVarValueAsString(rBind, rVar );
            }
            printRow(pw, row, colWidths, colStart, colSep, colEnd) ;
        }
        for ( int i = 0 ; i < lineWidth ; i++ )
            pw.print('-') ;
        pw.println() ;
        resultSetRewindable = null ;
    }


    private void printRow(PrintWriter out, String[] row, int[] colWidths, String rowStart, String colSep, String rowEnd)
    {
        out.print(rowStart) ;
        for ( int col = 0 ; col < colWidths.length ; col++ )
        {
            String s = row[col] ;
            int pad = colWidths[col] ;
            StringBuffer sbuff = new StringBuffer(120) ;

            if ( col > 0 )
                sbuff.append(colSep) ;

            sbuff.append(s) ;
            for ( int j = 0 ; j < pad-s.length() ; j++ )
                sbuff.append(' ') ;

            out.print(sbuff) ;
        }
        out.print(rowEnd) ;
        out.println() ;
    }

    protected String getVarValueAsString(QuerySolution rBind, String varName)
    {
        RDFNode obj = rBind.get(varName) ;
        
        if ( obj == null )
            return notThere ;

        return FmtUtils.stringForRDFNode(obj, context) ;
    }

    @Override
    public void format(OutputStream out, boolean answer)
    {
      PrintWriter pw = FileUtils.asPrintWriterUTF8(out) ;
      if ( answer )
          pw.write("yes") ;
      else
          pw.write("no") ;
      pw.flush() ;
    }
}
