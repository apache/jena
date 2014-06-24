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

package com.hp.hpl.jena.sparql.engine.optimizer;

import static com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternElements.* ;

import java.io.OutputStream ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternTriple ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemException ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;

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
    private static Logger log = LoggerFactory.getLogger(StatsMatcher.class) ; 
    public static final String STATS    = "stats" ; 
    public static final String META     = "meta" ; 
    public static final String COUNT    = "count" ;
    public static final Item OTHER      = Item.createSymbol("other") ;
    private static double NOMATCH       = -1 ;
    
    private static class Match
    {
        double weight = NOMATCH ;
        int exactMatches = 0 ;
        int termMatches = 0 ;
        int varMatches = 0 ;
        int anyMatches = 0 ;
    }

    // General structure
    protected List<Pattern> patterns = new ArrayList<>() ;
    // Map keyed by P for faster lookup (if no P available, we'll use the full list).  
    protected Map<Item, List<Pattern>> mapPatterns = new HashMap<>() ;
    
    // Default behaviour
    double DefaultMatch = NOMATCH ;
    
    long count = -1 ;
    
    public StatsMatcher() {}
    
    public StatsMatcher(String filename)
    {
        try {
            Item stats = SSE.readFile(filename) ;
            if ( stats.isNil() )
            {
                Log.warn(this, "Empty stats file: "+filename) ;
                return ;
            }
            if ( !stats.isTagged(STATS) )
                throw new ARQException("Not a stats file: "+filename) ;
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
            throw new ARQException("Not a tagged '"+STATS+"'") ;

        ItemList list = stats.getList().cdr();      // Skip tag
        
        if ( list.car().isTagged(META) )
        {        
            // Process the meta tag.
            Item elt1 = list.car(); 
            list = list.cdr();      // Move list on

            // Get count.
            Item x = Item.find(elt1.getList(), COUNT) ;
            if ( x != null )
                count = x.getList().get(1).asInteger() ;
        }
       
        while (!list.isEmpty()) 
        {
            Item elt = list.car() ;
            list = list.cdr();
            onePattern(elt) ;
        }
    }
     
    private void onePattern(Item elt)
    {
        Item pat = elt.getList().get(0) ;

        if (pat.isNode())
        {
            // (<uri> weight)
            Node n = pat.getNode() ;
            if (!n.isURI())
            {
                log.warn("Not a preicate URI: " + pat.toString()) ;
                return ;
            }
            addAbbreviation(elt) ;
        } 
        else if (pat.isSymbol())
        {
            if ( pat.equals(OTHER) )
            {
                double d = elt.getList().get(1).getDouble() ;
                DefaultMatch = d ;
                return ;
            }
            
            if ( pat.equals(BNODE) || pat.equals(LITERAL) )
            {
                log.warn("Not a match for a predicate URI: " + pat.toString()) ;
                return ;
            }
            if ( pat.equals(TERM) || pat.equals(VAR) || pat.equals(ANY) )
                addAbbreviation(elt) ;
            else
            {
                log.warn("Not understood: " + pat) ;
                return ;
            }
        } 
        else if (pat.isList() && pat.getList().size() == 3)
        {
            // It's of the form ((S P O) weight)
            Item w = elt.getList().get(1) ;
            Pattern pattern = new Pattern(((Number)(w.getNode().getLiteralValue())).doubleValue(),
                                          intern(pat.getList().get(0)), intern(pat.getList().get(1)),
                                          intern(pat.getList().get(2))) ;
            addPattern(pattern) ;
        } 
        else
        {
            log.warn("Unrecognized pattern: " + pat) ;
        }
    }
    
    private void addAbbreviation(Item elt)
    {
        Item predicateTerm = elt.getList().get(0) ;
        // Single node - it's a predicate abbreviate.
        double numProp = elt.getList().get(1).getDouble() ;
        
        if ( count < 100 )
            addPatternsSmall(predicateTerm, numProp) ;
        else
            addPatterns(predicateTerm, numProp) ;
    }
    
    // Knowing ?PO is quite important - it ranges from IFP (1) to
    // rdf:type rdf:Resource (potentially everything).

    public static final double weightSP = 2 ;
    public static final double weightPO = 10 ;
    public static final double weightTypeO = 1000 ; // ? rdf:type <Object> -- Avoid as can be very, very bad.
    
    public static final double weightSP_small = 2 ;
    public static final double weightPO_small = 4 ;
    public static final double weightTypeO_small = 40 ;
    
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
        wPO = Math.min(numProp, wPO) ;
        wSP = Math.min(numProp, wSP) ;
        
        if ( NodeConst.nodeRDFType.equals(predicate.getNode()) )
            // ? rdf:type <Object> -- Avoid as can be very, very bad.
            wPO = weightTypeO ;
        addPatterns(predicate, numProp, wSP, wPO) ;
    }
    
    private void addPatternsSmall(Item predicate, double numProp)
    {
        double wSP = weightSP_small ;
        double wPO = weightPO_small ;
        wPO = Math.min(numProp, wPO) ;
        wSP = Math.min(numProp, wSP) ;

        
        if ( predicate.isNode() && NodeConst.nodeRDFType.equals(predicate.getNode()) )
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
        
        List<Pattern> entry = mapPatterns.get(pattern.predItem) ;
        if ( entry == null )
        {
            entry = new ArrayList<>() ;
            mapPatterns.put(pattern.predItem, entry ) ;
        }
        entry.add(pattern) ;
    }
    
//    public void addPattern(Triple triple)
//    {
//        if ( triple.getSubject().isVariable() )
//        {
//            // PO, P and O
//        }
//        else
//        {
//            //SPO, SP and SO
//        }
//        throw new NotImplementedException("StatsMatcher.addPattern") ;
//    }
    
    private static void check(Pattern pattern)
    {
        check(pattern.subjItem) ;
        check(pattern.predItem) ;
        check(pattern.objItem) ;
    }

    private static void check(Item item)
    {
        if ( Var.isVar(item.getNode()) )
            throw new ARQException("Explicit variable used in a pattern (use VAR): "+item.getNode()) ;
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
    
    /** Return the matching weight for the first triple match found, 
     * else apply default value for fixed, unknnown predciate,
     * else return NOMATCH
     */
    public double match(Item subj, Item pred, Item obj)
    {
        double m = matchWorker(subj, pred, obj) ;
        if ( m == NOMATCH && pred.isNodeURI() )
            m = DefaultMatch ;
        //System.out.println("("+subj+" "+pred+" "+obj+") => "+m) ;
        return m ;
    }
    
    private double matchWorker(Item subj, Item pred, Item obj)
    {
        if ( isSet(subj) && isSet(pred) && isSet(obj) )
            // A set of triples ...
            return 1.0 ;
        
        // A predicate can be :
        //   A URI      - search on that URI, the TERM and ANY chains.
        //   A variable - search on that VAR and ANY chains.
        
        if ( pred.isNodeURI() )
        {
            double w = NOMATCH ;
            w = search(pred, subj, pred, obj, w) ;
            w = search(TERM, subj, pred, obj, w) ;  //?? 
            w = search(ANY, subj, pred, obj, w) ;   //??
            return w ;
        }
        
        if ( pred.isVar() )
        {
            double w = NOMATCH ;
            w = search(VAR, subj, pred, obj, w) ;
            w = search(ANY, subj, pred, obj, w) ;
            return w ;
        }
        
        if ( pred.equals(TERM) )
        {
            double w = NOMATCH ;
            w = search(TERM, subj, pred, obj, w) ;
            w = search(ANY, subj, pred, obj, w) ;
            return w ;
        }
        
        if ( pred.equals(ANY) )
        {
            throw new ARQException("Predicate is ANY") ;
            
            //double w = matchLinear(patterns, subj, pred, obj) ;
//            double w = NOMATCH ;
//            w = search(VAR, subj, pred, obj, w) ;
//            w = search(ANY, subj, pred, obj, w) ;
//            return w ;
        }
        
        throw new ARQException("Unidentified predicate: "+pred+" in ("+subj+" "+pred+" "+obj+")") ;
        
        //return matchLinear(subj, pred, obj) ;
    }
    

    private double search(Item key, Item subj, Item pred, Item obj, double oldMin)
    {
        List<Pattern> entry = mapPatterns.get(key) ;
        if ( entry == null )
            return oldMin ;
        double w = matchLinear(entry, subj, pred, obj) ;
        return minPos(w, oldMin) ;
    }
    
    //Minimum respecting NOMATCH for "not known"
    private static double minPos(double x, double y)
    {
        if ( x == NOMATCH ) return y ;
        if ( y == NOMATCH ) return x ;
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
            if ( ! matchNode(obj,  pattern.objItem,  match) )
                continue ;
            // First match.
            return pattern.weight ;
        }
        return NOMATCH ;
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

            throw new ARQException("StatsMatcher: unexpected slot type: "+node) ; 
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
    
    public void printSSE(OutputStream ps)
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
