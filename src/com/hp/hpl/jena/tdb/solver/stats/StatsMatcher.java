/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.ANY;
import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.BNODE;
import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.LITERAL;
import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.TERM;
import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.URI;
import static com.hp.hpl.jena.tdb.solver.stats.PatternElements.VAR;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemException;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.sparql.util.Printable;
import com.hp.hpl.jena.tdb.TDBException;

/** Stats format:
 * <pre>(stats
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
    public static final String TAG     = "stats" ; 

    
    static class Pattern implements Printable
    {
        Item subjItem ;
        Item predItem ;
        Item objItem ;
        double weight ; 
        
        Pattern(double w, Item subj, Item pred, Item obj)
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

        public void output(IndentedWriter out, SerializationContext cxt)
        {}

        public String toString(PrefixMapping pmap)
        {
            return null ;
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

    // A better structure would be a hierarchy based on P,S,O
    // because P is often fixed.
    // Or a map keyed by P.
    List<Pattern> patterns = new ArrayList<Pattern>() ;
    
    Map<Item, List<Pattern>> _patterns = new HashMap<Item,  List<Pattern>>() ;
    
    //Map<Item, List<Pattern>> patterns = new HashMap<>() ;//new ArrayList<Pattern>() ;
    
    private StatsMatcher() {}
    
    public StatsMatcher(String filename)
    {
        try {
            Item stats = SSE.readFile(filename) ;
            if ( !stats.isTagged(TAG) )
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
        if ( !stats.isTagged(TAG) )
            throw new TDBException("Not a tagged '"+TAG+"'") ;

        ItemList list = stats.getList().cdr();      // Skip tag

        while (!list.isEmpty()) 
        {
            
            Item elt = list.car() ;
            list = list.cdr();
            
            Item pat = elt.getList().get(0) ;

            if ( elt.isTagged("meta") )
            {
                int count = -1 ;
                // Get count.
                Item x = Item.find(elt.getList(), "count") ;
                if ( x != null )
                    count = asInt(x.getList().get(1)) ;
                continue ;
            }
            
            if ( pat.isNode() )
            {
                // Generate entries: 
                addPattern(new Pattern(2,  TERM, pat, ANY)) ;     // S, P, ? : approx weight
                addPattern(new Pattern(10, ANY, pat, TERM)) ;     // ?, P, O : approx weight
                addPattern(new Pattern(((Number)(elt.getList().get(1).getNode().getLiteralValue())).doubleValue(),
                                         ANY, pat, ANY)) ;          // ?, P, ?
            }
            else
            {
                Item w =  elt.getList().get(1) ;
                Pattern pattern = new Pattern(((Number)(w.getNode().getLiteralValue())).doubleValue(),
                                              intern(pat.getList().get(0)),
                                              intern(pat.getList().get(1)),
                                              intern(pat.getList().get(2))) ;
                addPattern(pattern) ;
            }
            
            // Round and round
        }
    }
        
    private void addPattern(Pattern pattern)
    {
        patterns.add(pattern) ;
        
        List<Pattern> entry = _patterns.get(pattern.predItem) ;
        if ( entry == null )
        {
            entry = new ArrayList<Pattern>() ;
            _patterns.put(pattern.predItem, new ArrayList<Pattern>() ) ;
        }
        entry.add(pattern) ;
    }
    
    // -- More for Item
    
    public static int asInt(Item item)
    {
        if ( item.isNode() )
        { 
            if ( item.getNode().isLiteral() )
                // Ignore typing.
                return Integer.parseInt(item.getNode().getLiteralLexicalForm()) ;
        }
        if ( item.isSymbol() )
            return Integer.parseInt(item.getSymbol()) ;
        
        throw new ItemException("Not a literal or string: "+item) ;
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

    /** Return the matching weight for the first triple match found, else -1 for no match */
    
    public double match(Item subj, Item pred, Item obj)
    {
        return matchMap(subj, pred, obj) ;
    }
    
    private double matchMap(Item subj, Item pred, Item obj)
    {
        if ( isSet(subj) && isSet(pred) && isSet(obj) )
            // A set of triples ...
            return 1.0 ;
        
//        if ( ! pred.isNode() )
//            return matchLinear(patterns, subj, pred, obj) ;
        
        List<Pattern> entry = _patterns.get(pred) ;
        if ( entry == null )
            return -1 ;
        return matchLinear(entry, subj, pred, obj) ;
    }

    
    private double matchLinear(Item subj, Item pred, Item obj)
    {
        if ( isSet(subj) && isSet(pred) && isSet(obj) )
            // A set of triples ...
            return 1.0 ;
        
        // Use a map keyed by predicate to accelerate searching.
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

    private static double matchLinear(List<Pattern> patterns, Item subj, Item pred, Item obj)
    {
     // Use a map keyed by predicate to accelerate searching.
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
    
    private static boolean isSet(Item item)
    {
        if (item.isNode() && item.getNode().isConcrete() ) return true ;
        if (item.equals(TERM) ) return true ;
        if (item.equals(URI) ) return true ;
        if (item.equals(BNODE) ) return true ;
        if (item.equals(LITERAL) ) return true ;
        return false ;
    }
    
    /** Return the matching weight for the given triple, else -1 for no match */
    /*public*/private double matchLinear_(Item subj, Item pred, Item obj)
    {
        // Weighted matching.
        // Redo as weight of best, most specifc, match.  
        int matches = 0 ;
        double w = -1 ;
        
        for ( Pattern pattern : patterns )
        {
            Match match = new Match() ;
            if ( ! matchNode(subj, pattern.subjItem, match) )
                continue ;
            if ( ! matchNode(pred, pattern.predItem, match) )
                continue ;
            if ( ! matchNode(obj, pattern.objItem, match) )
                continue ;
            int m = (100*match.exactMatches)+(10*match.termMatches)+match.varMatches+match.anyMatches ;

            if ( m > matches )
            {
                w = pattern.weight ;
                matches = m ;
            }
        }
        return w ;
    }
    
    private static boolean matchNode(Item node, Item item, Match details)
    {
        if ( item.equals(ANY) )
        {
            details.anyMatches ++ ;
            return true ;
        }
        
        if ( item.equals(VAR) ) 
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
        
            if ( item.equals(TERM) )
            {
                details.termMatches ++ ;
                return true ;
            }
            
            if ( item.equals(URI) && n.isURI() )
            {
                details.termMatches ++ ;
                return true ;
            }
            if ( item.equals(LITERAL) && n.isLiteral() )
            {
                details.termMatches ++ ;
                return true ;
            }
            if ( item.equals(BNODE) && n.isBlank() )
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
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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