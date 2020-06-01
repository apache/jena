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

package org.apache.jena.sparql.serializer;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.sparql.serializer.FmtEltLib.count;
import static org.apache.jena.sparql.serializer.FmtEltLib.createTriplesListBlock;
import static org.apache.jena.sparql.serializer.FmtEltLib.rdfFirst;

import java.util.*;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryVisitor;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.PathWriter;
import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;

public class FormatterElement extends FormatterBase implements ElementVisitor {
    public static final int     INDENT                   = 2;

    /** Control whether to show triple pattern boundaries - creates extra nesting */
    public static final boolean PATTERN_MARKERS          = false;

// /** Control whether triple patterns always have a final dot - it can be dropped in some cases */
// public static boolean PATTERN_FINAL_DOT = false ;

    /** Control whether (non-triple) patterns have a final dot - it can be dropped */
    public static final boolean GROUP_SEP_DOT            = false;

    /** Control whether the first item of a group is on the same line as the { */
    public static final boolean GROUP_FIRST_ON_SAME_LINE = true;

    /** Control pretty printing */
    public static final boolean PRETTY_PRINT             = true;

    /** Control pretty printing of RDF lists */
    public static final boolean FMT_LISTS                = true;
    
    /** Control pretty printing of free standing RDF lists */
    // Do *not* set "true" - argument of property paths can be lists and these spane
    // PathBlocks and Basic Graph Patterns and this is not handled prop.
    public static final boolean FMT_FREE_STANDING_LISTS  = false;

    /**
     * Control whether disjunction has set of delimiters - as it's a group usually, these
     * aren't needed
     */
    public static final boolean UNION_MARKERS            = false;

    /** Control whether GRAPH indents in a fixed way or based on the layout size */
    public static final boolean GRAPH_FIXED_INDENT       = true;

    /**
     * Control whether NOT EXIST/EXISTS indents in a fixed way or based on the layout size
     */
    public static final boolean ELEMENT1_FIXED_INDENT    = true;

    /** Control triples pretty printing */
    public static final int     TRIPLES_SUBJECT_COLUMN   = 8;

    // Less than this => rest of triple on the same line
    // Could be smart and make it depend on the property length as well. Later.
    public static final int     TRIPLES_SUBJECT_LONG     = 12;

    public static final int     TRIPLES_PROPERTY_COLUMN  = 20;

    public static final int     TRIPLES_COLUMN_GAP       = 2;

    public FormatterElement(IndentedWriter out, SerializationContext context) {
        super(out, context);
    }

    public static void format(IndentedWriter out, SerializationContext cxt, Element el) {
        FormatterElement fmt = new FormatterElement(out, cxt);
        fmt.startVisit();
        el.visit(fmt);
        fmt.finishVisit();
    }

    public static String asString(Element el) {
        SerializationContext cxt = new SerializationContext();
        IndentedLineBuffer b = new IndentedLineBuffer();
        FormatterElement.format(b, cxt, el);
        return b.toString();
    }

    public boolean topMustBeGroup() {
        return false;
    }

    @Override
    public void visit(ElementTriplesBlock el) {
        if ( el.isEmpty() ) {
            out.println("# Empty BGP");
            return;
        }
        formatTriples(el.getPattern());
    }

    @Override
    public void visit(ElementPathBlock el) {
        // Write path block - don't put in a final trailing "."
        if ( el.isEmpty() ) {
            out.println("# Empty BGP");
            return;
        }

        // Split into BGP-path-BGP-...
        // where the BGPs may be empty.
        PathBlock pBlk = el.getPattern();
        BasicPattern bgp = new BasicPattern();
        boolean first = true;      // Has anything been output?
        for ( TriplePath tp : pBlk ) {
            if ( tp.isTriple() ) {
                bgp.add(tp.asTriple());
                continue;
            }

            if ( !bgp.isEmpty() ) {
                if ( !first )
                    out.println(" .");
                flush(bgp);
                first = false;
            }
            if ( !first )
                out.println(" .");
            // Path (no RDF list output).
            printSubject(tp.getSubject());
            out.print(" ");
            PathWriter.write(out, tp.getPath(), context.getPrologue());
            out.print(" ");
            printObject(tp.getObject());
            first = false;
        }
        // Flush any stored triple patterns.
        if ( !bgp.isEmpty() ) {
            if ( !first )
                out.println(" .");
            flush(bgp);
            first = false;
        }
    }

    @Override
    public void visit(ElementDataset el) {
        // Not implemented.
//        if ( el.getDataset() != null ) {
//            DatasetGraph dsNamed = el.getDataset();
//            out.print("DATASET ");
//            out.incIndent(INDENT);
//            Iterator<Node> iter = dsNamed.listGraphNodes();
//            if ( iter.hasNext() ) {
//                boolean first = false;
//                for ( ; iter.hasNext() ; ) {
//                    if ( !first )
//                        out.newline();
//                    out.print("FROM <");
//                    Node n = iter.next();
//                    out.print(slotToString(n));
//                    out.print(">");
//                }
//            }
//            out.decIndent(INDENT);
//            out.newline();
//        }
//        if ( el.getElement() != null )
//            visitAsGroup(el.getElement());
    }

    @Override
    public void visit(ElementFilter el) {
        out.print("FILTER ");
        Expr expr = el.getExpr();
        FmtExprSPARQL v = new FmtExprSPARQL(out, context);

        // This assumes that complex expressions are bracketted
        // (parens) as necessary except for some cases:
        // Plain variable or constant

        boolean addParens = false;
        if ( expr.isVariable() )
            addParens = true;
        if ( expr.isConstant() )
            addParens = true;

        if ( addParens )
            out.print("( ");
        v.format(expr);
        if ( addParens )
            out.print(" )");
    }

    @Override
    public void visit(ElementAssign el) {
        out.print("LET (");
        out.print("?" + el.getVar().getVarName());
        out.print(" := ");
        FmtExprSPARQL v = new FmtExprSPARQL(out, context);
        v.format(el.getExpr());
        out.print(")");
    }

    @Override
    public void visit(ElementBind el) {
        out.print("BIND(");
        FmtExprSPARQL v = new FmtExprSPARQL(out, context);
        v.format(el.getExpr());
        out.print(" AS ");
        out.print("?" + el.getVar().getVarName());
        out.print(")");
    }

    @Override
    public void visit(ElementFind el) {
        out.print("FIND(");
        out.print("<< ");
        formatTriple(el.getTriple());
        out.print(" >>");
        out.print(" AS ");
        out.print("?" + el.getVar().getVarName());
        out.print(")");
    }

    @Override
    public void visit(ElementData el) {
        QuerySerializer.outputDataBlock(out, el.getVars(), el.getRows(), context);
    }

    @Override
    public void visit(ElementUnion el) {
        if ( el.getElements().size() == 0 ) {
        	// If this is a union of zero elements, do nothing.
            return ;
        }
        
        if ( el.getElements().size() == 0 ) {
            // If this is a union of one elements, put in a {}-group 
            visitAsGroup(el.getElements().get(0));
            return;
        }

        if ( UNION_MARKERS ) {
            out.print("{");
            out.newline();
            out.pad();
        }

        out.incIndent(INDENT);

        boolean first = true;
        for ( Element subElement : el.getElements() ) {
            if ( !first ) {
                out.decIndent(INDENT);
                out.newline();
                out.print("UNION");
                out.newline();
                out.incIndent(INDENT);
            }
            visitAsGroup(subElement);
            first = false;
        }

        out.decIndent(INDENT);

        if ( UNION_MARKERS ) {
            out.newline();
            out.print("}");
        }
    }

    @Override
    public void visit(ElementGroup el) {
        out.print("{");
        int initialRowNumber = out.getRow();
        out.incIndent(INDENT);
        if ( !GROUP_FIRST_ON_SAME_LINE )
            out.newline();

        int row1 = out.getRow();
        out.pad();

        boolean first = true;
        Element lastElt = null;

        for ( Element subElement : el.getElements() ) {
            // Some adjacent elements need a DOT:
            // ElementTriplesBlock, ElementPathBlock
            if ( !first ) {
                // Need to move on after the last thing printed.
                // Check for necessary DOT as separator
                if ( GROUP_SEP_DOT || needsDotSeparator(lastElt, subElement) )
                    out.print(" . ");
                out.newline();
            }
            subElement.visit(this);
            first = false;
            lastElt = subElement;
        }
        out.decIndent(INDENT);

        // Where to put the closing "}"
        int row2 = out.getRow();
        if ( row1 != row2 )
            out.newline();

        // Finally, close the group.
        if ( out.getRow() == initialRowNumber )
            out.print(" ");
        out.print("}");
    }

    private static boolean needsDotSeparator(Element el1, Element el2) {
        return needsDotSeparator(el1) && needsDotSeparator(el2);
    }

    private static boolean needsDotSeparator(Element el) {
        return (el instanceof ElementTriplesBlock) || (el instanceof ElementPathBlock);
    }

    @Override
    public void visit(ElementOptional el) {
        out.print("OPTIONAL");
        out.incIndent(INDENT);
        out.newline();
        visitAsGroup(el.getOptionalElement());
        out.decIndent(INDENT);
    }

    @Override
    public void visit(ElementNamedGraph el) {
        visitNodePattern("GRAPH", el.getGraphNameNode(), el.getElement());
    }

    @Override
    public void visit(ElementService el) {
        String x = "SERVICE";
        if ( el.getSilent() )
            x = "SERVICE SILENT";
        visitNodePattern(x, el.getServiceNode(), el.getElement());
    }

    private void visitNodePattern(String label, Node node, Element subElement) {
        int len = label.length();
        out.print(label);
        out.print(" ");
        String nodeStr = (node == null) ? "*" : slotToString(node);
        out.print(nodeStr);
        len += nodeStr.length();
        if ( GRAPH_FIXED_INDENT ) {
            out.incIndent(INDENT);
            out.newline(); // NB and newline
        } else {
            out.print(" ");
            len++;
            out.incIndent(len);
        }
        visitAsGroup(subElement);

        if ( GRAPH_FIXED_INDENT )
            out.decIndent(INDENT);
        else
            out.decIndent(len);
    }

    private void visitElement1(String label, Element1 el) {

        int len = label.length();
        out.print(label);
        len += label.length();
        if ( ELEMENT1_FIXED_INDENT ) {
            out.incIndent(INDENT);
            out.newline(); // NB and newline
        } else {
            out.print(" ");
            len++;
            out.incIndent(len);
        }
        visitAsGroup(el.getElement());
        if ( ELEMENT1_FIXED_INDENT )
            out.decIndent(INDENT);
        else
            out.decIndent(len);
    }

    @Override
    public void visit(ElementExists el) {
        visitElement1("EXISTS", el);
    }

    @Override
    public void visit(ElementNotExists el) {
        visitElement1("NOT EXISTS", el);
    }

    @Override
    public void visit(ElementMinus el) {
        out.print("MINUS");
        out.incIndent(INDENT);
        out.newline();
        visitAsGroup(el.getMinusElement());
        out.decIndent(INDENT);
    }

    @Override
    public void visit(ElementSubQuery el) {
        out.print("{ ");
        out.incIndent(INDENT);
        Query q = el.getQuery();

        // Serialize with respect to the existing context
        QuerySerializerFactory factory = SerializerRegistry.get().getQuerySerializerFactory(Syntax.syntaxARQ);
        QueryVisitor serializer = factory.create(Syntax.syntaxARQ, context, out);
        q.visit(serializer);

        out.decIndent(INDENT);
        out.print("}");
    }

    public void visitAsGroup(Element el) {
        boolean needBraces = !((el instanceof ElementGroup) || (el instanceof ElementSubQuery));

        if ( needBraces ) {
            out.print("{ ");
            out.incIndent(INDENT);
        }

        el.visit(this);

        if ( needBraces ) {
            out.decIndent(INDENT);
            out.print("}");
        }
    }
    
    // -------- Formatting a basic graph pattern
    // Triple order is preserved.

    int subjectWidth   = -1;
    int predicateWidth = -1;

    @Override
    protected void formatTriples(BasicPattern triples) {
        if ( !PRETTY_PRINT ) {
            super.formatTriples(triples);
            return;
        }

        if ( triples.isEmpty() )
            return;

        // Lists in this BasicPattern.
        // TriplesListBlock is a record of lists in this BGP.
        // Formatting is off for list if there is an empty TriplesListBlock.
        // This is cautionsly spotting the triples from RDF lists as generated by the
        // parser.

        TriplesListBlock block = FMT_LISTS 
            ? createTriplesListBlock(triples) 
            : new TriplesListBlock();
        
        Set<Node> freeStanding = new HashSet<>();
        for ( Node head : block.listElementsMap.keySet() ) {
            // Check for suitablity to print.
            // See also FmtEltLib#collectList
            //
            // Subject-list : inCount = 0, outCount = 3
            // Object-list :  inCount = 1, outCount = 2
            // Free-standing list :  inCount = 0, outCount = 2
            //     Free-standing list is handled as a special case. 
            int inCount = count(triples.getList(), ANY, ANY, head);
            int outCount = count(triples.getList(), head, ANY, ANY);
            if ( inCount == 0 && outCount == 2 )
                // Free standing.
                freeStanding.add(head);
        }

        setWidths(triples);
        if ( subjectWidth > TRIPLES_SUBJECT_COLUMN )
            subjectWidth = TRIPLES_SUBJECT_COLUMN;
        if ( predicateWidth > TRIPLES_PROPERTY_COLUMN )
            predicateWidth = TRIPLES_PROPERTY_COLUMN;

        // Accumulate all triples with the same subject.
        List<Triple> subjAcc = new ArrayList<>();
        // Subject being accumulated
        Node subj = null;
        // Print newlines between blocks.
        boolean first = true;

        int indent = -1;
        for ( Triple t : triples ) {
            if ( block.triplesInLists.contains(t) ) {
                if ( rdfFirst.equals(t.getPredicate()) ) {
                    if ( freeStanding.contains(t.getSubject()) )
                        printNodeOrList(t.getSubject(), block.listElementsMap);
                }
                continue;
            }

            if ( subj != null && t.getSubject().equals(subj) ) {
                subjAcc.add(t);
                continue;
            }

            if ( subj != null ) {
                if ( !first )
                    out.println(" .");
                formatSameSubject(subj, subjAcc, block.listElementsMap);
                first = false;
                // At end of line of a block of triples with same subject.
                // Drop through and start new block of same subject triples.
            }

            // New subject
            subj = t.getSubject();
            subjAcc.clear();
            subjAcc.add(t);
        }

        // Flush accumulator
        if ( subj != null && subjAcc.size() != 0 ) {
            if ( !first )
                out.println(" .");
            first = false;
            formatSameSubject(subj, subjAcc, block.listElementsMap);
        }
    }

    // ----

    private void flush(BasicPattern bgp) {
        formatTriples(bgp);
        bgp.getList().clear();
    }

    private void formatSameSubject(Node subject, List<Triple> triples, Map<Node, List<Node>> lists) {

        if ( triples == null || triples.size() == 0 )
            return;

        // Do the first triple.
        Iterator<Triple> iter = triples.iterator();
        Triple t1 = iter.next();

        int indent = subjectWidth + TRIPLES_COLUMN_GAP;

        int s1_len = printNodeOrList(subject, lists);
        if ( s1_len > TRIPLES_SUBJECT_LONG ) {
            out.incIndent(indent);
            out.println();
        } else {
            printGap();
            out.incIndent(indent);
        }

        // Remainder of first triple
        printProperty(t1.getPredicate());
        printGap();

        printNodeOrList(t1.getObject(), lists);

        // Do the rest
        for ( ; iter.hasNext() ; ) {
            Triple t = iter.next();
            out.println(" ;");
            printProperty(t.getPredicate());
            printGap();
            printNodeOrList(t.getObject(), lists);
            continue;
        }

        // Finish off the block.
        out.decIndent(indent);
        // out.print(" .") ;
    }

    private int printNodeOrList(Node node, Map<Node, List<Node>> lists) {
        if ( lists.containsKey(node) )
            return printList(lists.get(node), lists);
        else
            return printNoCol(node);
    }

    private void setWidths(BasicPattern triples) {
        subjectWidth = -1;
        predicateWidth = -1;

        for ( Triple t : triples ) {
            String s = slotToString(t.getSubject());
            if ( s.length() > subjectWidth )
                subjectWidth = s.length();

            String p = slotToStringProperty(t.getPredicate());
            if ( p.length() > predicateWidth )
                predicateWidth = p.length();
        }
    }

    private void printGap() {
        out.print(' ', TRIPLES_COLUMN_GAP);
    }

    // printSubject, printObject - used in ElementPathBlock.
    private int printSubject(Node s) {
        return printNoCol(s);
    }

    // Unadorned "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"
    private static String RDFTYPE = FmtUtils.stringForNode(RDF.Nodes.type, new SerializationContext());

    private int printProperty(Node p) {
        String str = slotToStringProperty(p);
        out.print(str);
        out.pad(predicateWidth);
        return out.getCol();
    }

    // String for property - includes the rule for whether to use "a" or not. 
    private String slotToStringProperty(Node p) {
        String str = slotToString(p);
        // If rdf:type but no rdf: then str is full URI and we use the "a" form 
        if ( str.equals(RDFTYPE) )
            return "a";
        return str;
    }

    private int printObject(Node obj) {
        return printNoCol(obj);
    }

    private int printList(List<Node> list, Map<Node, List<Node>> lists) {
        if ( list.isEmpty() ) {
            out.print("()");
            return 2;
        }
            
        int col0 = out.getCol();
        out.print("( ");
        for ( Node n : list ) {
            printNodeOrList(n, lists);
            out.print(" ");
        }
        out.print(")");
        int col1 = out.getCol();
        return col1 - col0;
    }

    private int printNoCol(Node node) {
        String str = slotToString(node);
        out.print(str);
        return str.length();
    }
}
