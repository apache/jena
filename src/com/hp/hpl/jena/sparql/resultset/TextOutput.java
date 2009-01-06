/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.* ;
import java.io.* ;

import com.hp.hpl.jena.util.* ;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/** <p>Takes a ResultSet object and creates displayable formatted output in plain text.</p>
 *
 *  <p>Note: this is compute intensive and memory intensive.
 *  It needs to read all the results first (all the results are then in-memory)
 *  in order to find things the maximum width of a column value; then it needs
 *  to pass over the results again, turning them into output.
 *  </p>
 * @see com.hp.hpl.jena.query.ResultSetFormatter for convenience ways to call this formatter
 * 
 * @author   Andy Seaborne
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
            for ( Iterator<String> iter = rs.getResultVars().iterator() ; iter.hasNext() ; )
            {
                col++ ;
                String rVar = iter.next() ;
                String s = getVarValueAsString(rBind, rVar) ;
                if ( colWidths[col] < s.length() )
                    colWidths[col] = s.length() ;
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
            return ;
        }

        ResultSetRewindable resultSetRewindable = new ResultSetMem(resultSet) ;
        
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

    private String getVarValueAsString(QuerySolution rBind, String varName)
    {
        RDFNode obj = rBind.get(varName) ;
        
        if ( obj == null )
            return notThere ;

        return FmtUtils.stringForRDFNode(obj, context) ;
    }

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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
