/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.*;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
;

/**
 * @author Andy Seaborne
 */

public class FormatterARQ extends FormatterBase
    implements FormatterElement
{
    /** Control whether to show triple pattern boundaries - creates extra nesting */
    public static boolean PATTERN_MARKERS = false ;
    
//    /** Control whether triple patterns always have a final dot - it can be dropped in some cases */
//    public static boolean PATTERN_FINAL_DOT = false ;

    /** Control whether (non-triple) patterns have a final dot - it can be dropped */
    public static boolean GROUP_SEP_DOT = false ;
    
    /** Control whether the first item of a group is on the same line as the { */
    public static boolean GROUP_FIRST_ON_SAME_LINE = false ;

    /** Control pretty printing */
    public static boolean PRETTY_PRINT = true  ;

    /** Control whether disjunction has set of delimiters - as it's a group usually, these aren't needed */
    public static boolean UNION_MARKERS = false ;
    
    /** Control whether a group of one is unnested - changes the query syntax tree */ 
    public static boolean GROUP_UNNEST_ONE = false ; 

    /** Control whether GRAPH indents in a fixed way or based on the layout size */
    public static boolean GRAPH_FIXED_INDENT = true ;
    
    /** Control whether UNSAID indents in a fixed way or based on the layout size */
    public static boolean UNSAID_FIXED_INDENT = true ;
    
    /** Control triples pretty printing */
    public static int TRIPLES_SUBJECT_COLUMN = 8 ;
    
    // Less than this => rest of triple on the same line
    // Could be smart and make it depend on the property length as well.  Later.
    public static int TRIPLES_SUBJECT_LONG = 12 ;     

    public static int TRIPLES_PROPERTY_COLUMN = 20;
    
    public static int TRIPLES_COLUMN_GAP = 2 ;

    public FormatterARQ(IndentedWriter out, SerializationContext context)
    {
        super(out, context) ;
    }
    
    public static void format(IndentedWriter out, SerializationContext cxt, Element el)
    {
        FormatterARQ fmt = new FormatterARQ(out, cxt) ;
        fmt.startVisit() ;
        el.visit(fmt) ;
        fmt.finishVisit() ;
    }
    
    
    public static String asString(Element el)
    {
        SerializationContext cxt = new SerializationContext() ;
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        FormatterARQ.format(b.getIndentedWriter(), cxt, el) ;
        return b.toString() ;
    }

    public boolean topMustBeGroup() { return true ; }
    
    public void visit(ElementTriplesBlock el)
    {
        if ( el.isEmpty() )
        {
            out.println("# Empty BGP") ;
            return ;
        }
        formatTriples(el.getTriples()) ;
    }
    
    public void visit(ElementDataset el)
    {
//        if ( el.getDataset() != null)
//        {
//            DatasetGraph dsNamed = el.getDataset() ;
//            out.print("DATASET ") ;
//            out.incIndent(INDENT) ;
//            Iterator iter = dsNamed.listNames() ;
//            if ( iter.hasNext() )
//            {
//                boolean first = false ;
//                for ( ; iter.hasNext() ; )
//                {
//                    if ( ! first )
//                        out.newline() ;
//                    out.print("FROM <") ;
//                    String s = (String)iter.next() ; 
//                    out.print(s) ;
//                    out.print(">") ;
//                }
//            }
//            out.decIndent(INDENT) ;
//            out.newline() ;
//        }
        if ( el.getPatternElement() != null )
            visitAsGroup(el.getPatternElement()) ;
    }

    public void visit(ElementFilter el)
    {
        out.print("FILTER ") ;
        Expr expr = el.getExpr() ;
        FmtExprARQ v = new FmtExprARQ(out, context) ;
        
        // This assumes that complex expressions are bracketted
        // (parens) as necessary except for some cases:
        //   Plain variable or constant
        
        boolean addParens = false ;
        if ( expr.isVariable() )
            addParens = true ;
        if ( expr.isConstant() )
            addParens = true ;
        
        if ( addParens )
            out.print("( ") ;
        v.format(expr) ;
        if ( addParens )
            out.print(" )") ;
    }

    public void visit(ElementUnion el)
    {
        if ( el.getElements().size() == 1 )
        {
            // If this is an element of just one, just do it inplace
            // Can't happen from a parsed query.
            Element e = (Element)el.getElements().get(0) ;
            visitAsGroup(e) ;
            return ;
        }

        if ( UNION_MARKERS )
        {
            out.print("{") ;
            out.newline() ;
            out.pad() ;
        }
            
        out.incIndent(INDENT) ;
        
        boolean first = true ;
        for ( Iterator iter = el.getElements().listIterator() ; iter.hasNext() ;)
        {
            if ( ! first )
            {
                out.decIndent(INDENT) ;
                out.newline() ;
                out.print("UNION") ;
                out.newline() ;
                out.incIndent(INDENT) ;
            }
            Element subElement = (Element)iter.next() ;
            visitAsGroup(subElement) ;
            first = false ;
        }
        
        out.decIndent(INDENT) ;

        if ( UNION_MARKERS )
        {
            out.newline() ;
            out.print("}") ;
        }
    }

    
    public void visit(ElementGroup el)
    {
        if ( GROUP_UNNEST_ONE && el.getElements().size() == 1 )
        {
            // If this is an element of just one, we can remove the {} if it is a group.
            Element e = (Element)el.getElements().get(0) ;
            visitAsGroup(e) ;
            return ;
        }
        
        out.print("{") ;
        out.incIndent(INDENT) ;
        if ( GROUP_FIRST_ON_SAME_LINE )
            out.newline() ;  
        
        int row1 = out.getRow() ;
        out.pad() ;
    
        boolean first = true ;
        
        for ( Iterator iter = el.getElements().listIterator() ; iter.hasNext() ;)
        {
            Element subElement = (Element)iter.next() ;
           if ( ! first )
            {
                // Need to move on after the last thing printed.
                if ( GROUP_SEP_DOT )
                    out.print(" . ") ;
                out.newline() ;    
            }
            subElement.visit(this) ;
            first = false ;
        }
        out.decIndent(INDENT) ;
        
        // Where to put the closing "}"
        int row2 = out.getRow() ;
        if ( row1 != row2 )
            out.newline() ;
        
        // Finally, close the group.
        out.print("}") ;
    }

    public void visit(ElementOptional el)
    {
        out.print("OPTIONAL") ;
        out.incIndent(INDENT) ;
        out.newline() ;
        visitAsGroup(el.getOptionalElement()) ;
        out.decIndent(INDENT) ;
    }


    public void visit(ElementNamedGraph el)
    {
        visitNodePattern("GRAPH", el.getGraphNameNode(), el.getElement()) ;
    }

    public void visit(ElementService el)
    {
        visitNodePattern("SERVICE", el.getServiceNode(), el.getElement()) ;
    }
    
    private void visitNodePattern(String label, Node node, Element subElement)
    {
        int len = label.length() ;
        out.print(label) ;
        out.print(" ") ;
        String nodeStr = ( node == null ) ? "*" : slotToString(node) ;
        out.print(nodeStr) ;
        len += nodeStr.length() ;
        if ( GRAPH_FIXED_INDENT )
        {
            out.incIndent(INDENT) ;
            out.newline() ; // NB and newline
        }
        else
        {
            out.print(" ") ;
            len++ ;
            out.incIndent(len) ;
        }
        visitAsGroup(subElement) ;
        
        if ( GRAPH_FIXED_INDENT )
            out.decIndent(INDENT) ;
        else
            out.decIndent(len) ;
    }

    public void visit(ElementUnsaid el)
    {
        String s = "UNSAID " ;
        int len = s.length() ;
        out.print(s) ;
        len += s.length() ;
        if ( UNSAID_FIXED_INDENT )
        {
            out.incIndent(INDENT) ;
            out.newline() ; // NB and newline
        }
        else
        {
            out.print(" ") ;
            len++ ;
            out.incIndent(len) ;
        }
        visitAsGroup(el.getElement()) ;
        if ( UNSAID_FIXED_INDENT )
            out.decIndent(INDENT) ;
        else
            out.decIndent(len) ;
    }
    
    public void visit(ElementSubQuery el)
    {
        out.print("{ ") ;
        out.incIndent(INDENT) ;
        // Messy - prefixes.
        el.getQuery().serialize(out) ;
        out.decIndent(INDENT) ;
        out.print("}") ;
    }

    // Visit an element, ensuring it is always surround by {} as a group.
    
    private void visitAsGroup(Element el)
    {
        boolean isGroup = ( el instanceof ElementGroup ) ;
        
        if ( ! isGroup )
        {
            out.print("{ ") ;
            out.incIndent(INDENT) ;
        }
        el.visit(this) ;
        
        if ( ! isGroup )
        {
            out.decIndent(INDENT) ;
            out.print("}") ;
        }
    }

    // Work variables.
    // Assumes not threaded
    
    int subjectWidth = -1 ;
    int predicateWidth = -1 ;
    
    //@Override
    protected void formatTriples(BasicPattern triples)
    {
        if ( ! PRETTY_PRINT )
        {
            super.formatTriples(triples) ;
            return ;
        }
        
        int x = + 
        3 ;
        
        // TODO RDF Collections - spot the parsers pattern 
        if ( triples.isEmpty() )
            return ;

        setWidths(triples) ;
        if ( subjectWidth > TRIPLES_SUBJECT_COLUMN )
            subjectWidth = TRIPLES_SUBJECT_COLUMN ;
        if ( predicateWidth > TRIPLES_PROPERTY_COLUMN )
            predicateWidth = TRIPLES_PROPERTY_COLUMN ;
        
        // Loops:
        List subjAcc = new ArrayList() ;    // Accumulate all triples with the same subject.  
        Node subj = null ;                  // Subject being accumulated
        
        boolean first = true ;             // Print newlines between blocks.
        
        int indent = -1 ;
        for ( Iterator iter = triples.iterator() ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            if ( subj != null && t.getSubject().equals(subj) )
            {
                subjAcc.add(t) ;
                continue ;
            }
            
            if ( subj != null )
            {
                if ( ! first )
                    out.println() ;
                formatSameSubject(subj, subjAcc) ;
                first = false ;
                // At end of line of a block of triples with same subject.
                // Drop through and start new block of same subject triples.
            }

            // New subject
            subj = t.getSubject() ;
            subjAcc.clear() ;
            subjAcc.add(t) ;
        }
        
        // Flush accumulator
        if ( subj != null && subjAcc.size() != 0 )
        {
            if ( ! first )
                out.println() ;
            first = false ;
            formatSameSubject(subj, subjAcc) ;
        }
    }
    
    private void formatSameSubject(Node subject, List triples)
    {
        if ( triples == null || triples.size() == 0 )
            return ;
        
        // Do the first triple.
        Iterator iter = triples.iterator() ;
        Triple t1 = (Triple)iter.next() ; 

//        int indent = TRIPLES_SUBJECT_COLUMN+TRIPLES_COLUMN_GAP ;
//        // Long subject => same line.  Works for single triple as well.
//        int s1_len = printSubject(t1.getSubject()) ;
//        //int x = out.getCol() ;

        int indent = subjectWidth + TRIPLES_COLUMN_GAP ;
        int s1_len = printSubject(t1.getSubject()) ;

        if ( s1_len > TRIPLES_SUBJECT_LONG )
        {
            // Too long - start a new line.
            out.incIndent(indent) ;
            out.println() ;
        }
        else
        {
            printGap() ;
            out.incIndent(indent) ;
        }

        // Remained of first triple
        printProperty(t1.getPredicate()) ;
        printGap() ;
        printObject(t1.getObject()) ;
        
        // Do the rest
        
        for (  ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            out.println(" ;") ;
            printProperty(t.getPredicate()) ;
            printGap() ;
            printObject(t.getObject()) ;
            continue ;
            // print property list
        }

        // Finish off the block.
        out.decIndent(indent) ;
        out.print(" .") ;
    }
    
    private void setWidths(BasicPattern triples)
    {
        subjectWidth = -1 ;
        predicateWidth = -1 ;

        for ( Iterator iter = triples.iterator() ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            String s = slotToString(t.getSubject()) ;
            if ( s.length() > subjectWidth )
                subjectWidth = s.length() ;
            
            String p = slotToString(t.getPredicate()) ;
            if ( p.length() > predicateWidth )
                predicateWidth = p.length() ;
        }
    }
    
    private void printGap()
    {
        out.print(' ', TRIPLES_COLUMN_GAP) ;
    }
    
    // Indent must be set first.
    private int printSubject(Node s)
    {
        String str = slotToString(s) ;
        out.print(str) ;
        //out.pad(TRIPLES_SUBJECT_COLUMN) ;
        out.pad(subjectWidth) ;
        return str.length() ; 
    }

    // Assumes the indent is TRIPLES_SUBJECT_COLUMN+GAP
    private int printProperty(Node p)
    {
        String str = slotToString(p) ;
        out.print(str) ;
        //out.pad(TRIPLES_PROPERTY_COLUMN) ;
        out.pad(predicateWidth) ;
        return str.length() ; 
    }
    
    private int printObject(Node obj)
    {
        return printNoCol(obj) ;
    }
    
    private int printNoCol(Node node)
    {
        String str = slotToString(node) ; 
        out.print(str) ;
        return str.length() ;
        
    }
    
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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