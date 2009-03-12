/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.ANY;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.BNODE;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.LITERAL;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.TERM;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.URI;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.VAR;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAny;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAnyBNode;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAnyLiteral;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAnyTerm;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAnyURI;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isAnyVar;
import static com.hp.hpl.jena.tdb.solver.reorder.PatternElements.isSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import lib.NotImplemented;
import logging.Log;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemException;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.sparql.util.Printable;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.lib.NodeConst;
import com.hp.hpl.jena.tdb.solver.reorder.PatternTriple;

/** Stats format:<pre>(stats
 *    (meta ...)
 *    ((S P O) weight)
 *    (<predicate uri> weight)
 *  )</pre>
 * where <code>S</code>, <code>P</code>, <code>O</code> is a URI, variable, 
 * literal or one of the words <code>ANY</code> (matches anything), 
 * <code>VAR</code> (matches a variable), <code>TERM</code> (matches a
 * fixed URI, or literal), <code>URI</code>, <code>BNODE</code>, 
 * <code>LITERAL</code> (matches one of these types).    
 */

public final class StatsMatcher
{
    public static final String STATS     = "stats" ; 
    public static final String META      = "meta" ; 
    public static final String COUNT     = "count" ;
    
    static public class Pattern implements Printable
    {
        Item subjItem ;
        Item predItem ;
        Item objItem ;
        double weight ; 
        
        public Pattern(double w, Item subj, Item pred, Item obj)
        {
            weight = w ;
            subjItem = subj ;
            predItem = pred ;
            objItem = obj ;
        }        
        
        @Override
        public String toString()
        {
            //return "("+subjItem+" "+predItem+" "+objItem+") ==> "+weight ;
            return PrintUtils.toString(this) ;
        }
        
        public void output(IndentedWriter out)
        {
            out.print("(") ;
            out.print("(") ;
            out.print(subjItem.toString()) ;
            out.print(" ") ;
            out.print(predItem.toString()) ;
            out.print(" ") ;
            out.print(objItem.toString()) ;
            out.print(")") ;
            out.print(" ") ;
            out.print(weight) ;
            out.print(")") ;
        }
    }

    private static class Match
    {
        double weight = -1 ;
        int exactMatches = 0 ;
        int termMatches = 0 ;
        int varMatches = 0 ;
        int anyMatches = 0 ;
    }

    // General structure
    List<Pattern> patterns = new ArrayList<Pattern>() ;
    // Map keyed by P for faster lookup (if no P available, we'll use the full list).  
    Map<Item, List<Pattern>> _patterns = new HashMap<Item,  List<Pattern>>() ;
    
    long count = -1 ;
    
    public StatsMatcher() {}
    
    public StatsMatcher(String filename)
    {
        try {
            Item stats = SSE.readFile(filename) ;
            if ( stats.isNil() )
            {
                ALog.warn(this, "Empty stats file: "+filename) ;
                return ;
            }
            if ( !stats.isTagged(STATS) )
                throw new TDBException("Not a stats file: "+filename) ;
            init(stats) ;
        } catch (ItemException ex)
        {  // Debug
            throw ex ;
        }
    }
    
    public StatsMatcher(Item stats)
    { init(stats) ; }
    
    private void init(Item stats)
    {
        if ( !stats.isTagged(STATS) )
            throw new TDBException("Not a tagged '"+STATS+"'") ;

        ItemList list = stats.getList().cdr();      // Skip tag
        
        if ( list.car().isTagged(META) )
        {        
            // Process the meta tag.
            Item elt1 = list.car(); 
            list = list.cdr();      // Move list on

            // Get count.
            Item x = Item.find(elt1.getList(), "count") ;
            if ( x != null )
                count = x.getList().get(1).asInteger() ;
        }
       
        while (!list.isEmpty()) 
        {
            
            Item elt = list.car() ;
            list = list.cdr();
            onePattern(elt) ;
            // Round and round
        }
    }
     
    private void onePattern(Item elt)
    {
        Item pat = elt.getList().get(0) ;
        
        if ( pat.isNode() )
        {
            double numProp = elt.getList().get(1).getDouble() ;
            
            if ( count < 100 )
                addPatternsSmall(pat, numProp) ;
            else
                addPatterns(pat, numProp) ;
        } else if ( pat.isSymbol() ) {
            // ****
            Log.info(this, "Symbol: "+pat.toString()) ;
        } else if ( pat.isList() && pat.getList().size() == 3 )
        {
            // It's of the form ((S P O) weight)
            Item w =  elt.getList().get(1) ;
            Pattern pattern = new Pattern(((Number)(w.getNode().getLiteralValue())).doubleValue(),
                                          intern(pat.getList().get(0)),
                                          intern(pat.getList().get(1)),
                                          intern(pat.getList().get(2))) ;
            addPattern(pattern) ;
        }
        else
        {
            Log.warn(this, "Unrecognized pattern: "+pat) ;
        }
    }
    
    // Knowing ?PO is quite important - it ranges from IFP (1) to
    // rdf:type rdf:Resource (potentially everything).

    static final double weightSP = 2 ;
    static final double weightPO = 10 ;
    static final double weightTypeO = 1000 ; // ? rdf:type <Object> -- Avoid as can be very, very bad.
    
    static final double weightSP_small = 2 ;
    static final double weightPO_small = 4 ;
    static final double weightTypeO_small = 40 ;
    
    /** Add patterns based solely on the predicate count and some guessing */  
    public void addPatterns(Node predicate, double numProp)
    {
        addPatterns(Item.createNode(predicate),  numProp) ;
    }
    
    /** Add patterns based solely on the predicate count and some guessing for a small graph
     * (less than a few thousand triples)
     */  
    public void addPatternsSmall(Node predicate, double numProp)
    {
        addPatternsSmall(Item.createNode(predicate),  numProp) ;
    }
    
    private void addPatterns(Item predicate, double numProp)
    {
        double wSP = weightSP ;
        double wPO = weightPO ;
        
        if ( NodeConst.nodeRDFType.equals(predicate.getNode()) )
            // ? rdf:type <Object> -- Avoid as can be very, very bad.
            wPO = weightTypeO ;
        addPatterns(predicate, numProp, wSP, wPO) ;
    }
    
    private void addPatternsSmall(Item predicate, double numProp)
    {
        double wSP = weightSP_small ;
        double wPO = weightPO_small ;
        
        if ( NodeConst.nodeRDFType.equals(predicate.getNode()) )
            wPO = weightTypeO_small ;
        addPatterns(predicate, numProp, wSP, wPO) ;
    }

    private void addPatterns(Item predicate, double wP, double wSP, double wPO)
    {
        addPattern(new Pattern(wSP, TERM, predicate, ANY)) ;     // S, P, ? : approx weight
        addPattern(new Pattern(wPO,  ANY, predicate, TERM)) ;    // ?, P, O : approx weight
        addPattern(new Pattern(wP,   ANY, predicate, ANY)) ;     // ?, P, ?
    }

    public void addPattern(Pattern pattern)
    {
        // Check for named variables whch should not appear in a Pattern
        check(pattern) ;
        
        patterns.add(pattern) ;
        
        List<Pattern> entry = _patterns.get(pattern.predItem) ;
        if ( entry == null )
        {
            entry = new ArrayList<Pattern>() ;
            _patterns.put(pattern.predItem, entry ) ;
        }
        entry.add(pattern) ;
    }
    
    public void addPattern(Triple triple)
    {
        if ( triple.getSubject().isVariable() )
        {
            // PO, P and O
        }
        else
        {
            //SPO, SP and SO
        }
        throw new NotImplemented("StatsMatcher.addPattern") ;
    }
    
    private static void check(Pattern pattern)
    {
        check(pattern.subjItem) ;
        check(pattern.predItem) ;
        check(pattern.objItem) ;
    }

    private static void check(Item item)
    {
        if ( Var.isVar(item.getNode()) )
            throw new TDBException("Explicit variable used in a pattern (use VAR): "+item.getNode()) ;
    }

    private Item intern(Item item)
    {
        if ( item.sameSymbol(ANY.getSymbol()) )         return ANY ;
        if ( item.sameSymbol(VAR.getSymbol()) )         return VAR ;
        if ( item.sameSymbol(TERM.getSymbol()) )        return TERM ;
        if ( item.sameSymbol(URI.getSymbol()) )         return URI ;
        if ( item.sameSymbol(LITERAL.getSymbol()) )     return LITERAL ;
        if ( item.sameSymbol(BNODE.getSymbol()) )       return BNODE ;
        return item ;
    }
    
    public double match(Triple t)
    {
        return match(Item.createNode(t.getSubject()),
                     Item.createNode(t.getPredicate()),
                     Item.createNode(t.getObject())) ;
    }

    public double match(PatternTriple pTriple)
    {
        return match(pTriple.subject, pTriple.predicate, pTriple.object) ;
    }
    
    /** Return the matching weight for the first triple match found, else -1 for no match */
    
    public double match(Item subj, Item pred, Item obj)
    {
        if ( isSet(subj) && isSet(pred) && isSet(obj) )
            // A set of triples ...
            return 1.0 ;
        
        // A predicate can be :
        //   A URI      - search on that URI, the TERM and ANY chains.
        //   A variable - search on that VAR and ANY chains.
        
        if ( pred.isNodeURI() )
        {
            double w = -1 ;
            w = search(pred, subj, pred, obj, w) ;
            w = search(TERM, subj, pred, obj, w) ;  //?? 
            w = search(ANY, subj, pred, obj, w) ;   //??
            return w ;
        }
        
        if ( pred.isVar() )
        {
            double w = -1 ;
            w = search(VAR, subj, pred, obj, w) ;
            w = search(ANY, subj, pred, obj, w) ;
            return w ;
        }
        
        if ( pred.equals(TERM) )
        {
            double w = -1 ;
            w = search(TERM, subj, pred, obj, w) ;
            w = search(ANY, subj, pred, obj, w) ;
            return w ;
        }
        
        if ( pred.equals(ANY) )
        {
            throw new TDBException("Predicate is ANY") ;
            
            //double w = matchLinear(patterns, subj, pred, obj) ;
//            double w = -1 ;
//            w = search(VAR, subj, pred, obj, w) ;
//            w = search(ANY, subj, pred, obj, w) ;
//            return w ;
        }
        
        throw new TDBException("Unidentified predicate: "+pred+" in ("+subj+" "+pred+" "+obj+")") ;
        
        //return matchLinear(subj, pred, obj) ;
    }
    

    private double search(Item key, Item subj, Item pred, Item obj, double oldMin)
    {
        List<Pattern> entry = _patterns.get(key) ;
        if ( entry == null )
            return oldMin ;
        double w = matchLinear(entry, subj, pred, obj) ;
        return minPos(w, oldMin) ;
    }
    
    //Minimum respecting -1 for "not known"
    private static double minPos(double x, double y)
    {
        if ( x == -1.0 ) return y ;
        if ( y == -1.0 ) return x ;
        return Math.min(x, y) ;
    }
    
    private static double matchLinear(List<Pattern> patterns, Item subj, Item pred, Item obj)
    {
        for ( Pattern pattern : patterns )
        {
            Match match = new Match() ;
            if ( ! matchNode(subj, pattern.subjItem, match) )
                continue ;
            if ( ! matchNode(pred, pattern.predItem, match) )
                continue ;
            if ( ! matchNode(obj, pattern.objItem, match) )
                continue ;
            // First match.
            return pattern.weight ;
        }
        return -1 ;
    }
    
    private static boolean matchNode(Item node, Item item, Match details)
    {
        if ( isAny(item) )
        {
            details.anyMatches ++ ;
            return true ;
        }
        
        if ( isAnyVar(item) ) 
        {
            details.varMatches ++ ;
            return true ;
        }

        if ( node.isSymbol() )
        {
            //TERM in the thing to be matched means something concrete will be there.
            if ( node.equals(TERM) )
            {
                if ( item.equals(TERM) )
                {
                    details.termMatches ++ ;
                    return true ;
                }
                // Does not match LITERAL, URI, BNODE and VAR/ANY were done above.
                return false ;
            }

            throw new TDBException("StatsMatcher: unexpected slot type: "+node) ; 
        }
        
        if ( ! node.isNode() )
            return false ;
       
        Node n = node.getNode() ;
        if (  n.isConcrete() )
        {
            if ( item.isNode() && item.getNode().equals(n) )
            {
                details.exactMatches ++ ;
                return true ;
            }
        
            if ( isAnyTerm(item) )
            {
                details.termMatches ++ ;
                return true ;
            }
            
            if ( isAnyURI(item) && n.isURI() )
            {
                details.termMatches ++ ;
                return true ;
            }
            if ( isAnyLiteral(item) && n.isLiteral() )
            {
                details.termMatches ++ ;
                return true ;
            }
            if ( isAnyBNode(item) && n.isBlank() )
            {
                details.termMatches ++ ;
                return true ;
            }
        }
        return false ;
    }
    
    @Override
    public String toString()
    {
        String $ = "" ;
        for ( Pattern p : patterns )
            $ = $+p+"\n" ;
        return $ ;
    }
    
    public void printSSE(PrintStream ps)
    {
        IndentedWriter out = new IndentedWriter(ps) ;
        out.println("(stats") ;
        out.incIndent() ;
        for ( Pattern p : patterns )
        {
            p.output(out) ;
            out.println();
        }
        out.decIndent() ;
        out.println(")") ;
        out.flush();
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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