/*
 (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: MonotonicErrorAnalyzer.java,v 1.16 2005-01-25 11:42:48 jeremy_carroll Exp $
 */
package com.hp.hpl.jena.ontology.tidy.impl;

import java.util.*;

import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.ontology.tidy.Levels;
import com.hp.hpl.jena.ontology.tidy.errors.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;

/**
 * 
 * This class looks at particular triples and tries to work out what went wrong,
 * giving a specific anaylsis.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll </a>
 *  
 */
public class MonotonicErrorAnalyzer implements Constants {

    // TODO MAX_SINGLETON_SET in Grammar is wrong.

    final static private int BLANK_PROP = 1;

    final static private int LITERAL_PROP = 2;

    final static private int LITERAL_SUBJ = 3;

    final static private int BADID_USE = 4;

    final static private int BUILTIN_NON_PRED = 5;

    final static private int BUILTIN_NON_SUBJ = 6;

    final static private int TYPE_NEEDS_ID = 10;

    final static private int TYPE_NEEDS_BLANK = 11;

    final static private int TYPE_NEEDS_ID_OR_BLANK = 12;

    final static private int TYPE_OF_CLASS_ONLY_ID = 13;

    final static private int TYPE_OF_PROPERTY_ONLY_ID = 14;

    final static private int TYPE_FOR_BUILTIN = 15;

    final static private int ST_DOMAIN_RANGE = 20;

    final static int BAD_DOMAIN = ST_DOMAIN_RANGE + 1;

    final static int BAD_RANGE = ST_DOMAIN_RANGE + 2;

    final static int BAD_DOMAIN_RANGE = ST_DOMAIN_RANGE + 3;

    final static private int DIFFERENT_CATS = 30;

    final static private int DIFFERENT_CATS_S = DIFFERENT_CATS + 1;

    final static private int DIFFERENT_CATS_P = DIFFERENT_CATS + 2;

    final static private int DIFFERENT_CATS_SP = DIFFERENT_CATS + 3;

    final static private int DIFFERENT_CATS_O = DIFFERENT_CATS + 4;

    final static private int DIFFERENT_CATS_SO = DIFFERENT_CATS + 5;

    final static private int DIFFERENT_CATS_PO = DIFFERENT_CATS + 6;

    final static private int DIFFERENT_CATS_SPO = DIFFERENT_CATS + 7;

    final static private int DC_DOM_RANGE = 40;

    final static private int INCOMPATIBLE_S = DC_DOM_RANGE + 1;

    final static private int INCOMPATIBLE_P = DC_DOM_RANGE + 2;

    final static private int INCOMPATIBLE_SP = DC_DOM_RANGE + 3;

    final static private int INCOMPATIBLE_O = DC_DOM_RANGE + 4;

    final static private int INCOMPATIBLE_SO = DC_DOM_RANGE + 5;

    final static private int INCOMPATIBLE_PO = DC_DOM_RANGE + 6;

    final static private int INCOMPATIBLE_SPO = DC_DOM_RANGE + 7;

    static private LookupTable look = (LookupTable) LookupTable.get();

    static private final int SZ = CategorySet.unsorted.size();

    MonotonicProblem rslt;

    /**
     * Preconditions
     * 
     * @param g
     *            A graph in DL or Lite
     * @param t
     *            A triple which when added to g makes it full.
     * @return
     */
    static public MonotonicProblem[] getProblem(AbsChecker g, Triple t) {

        int s = g
                .getCategory(t.getSubject());
        int p = g.getCategory(t.getPredicate());
        int o = g.getCategory(t.getObject());
        MonotonicErrorAnalyzer monotonicErrorAnalyzer = new MonotonicErrorAnalyzer();
        MonotonicProblem r = monotonicErrorAnalyzer.getErrorCode(s, 
                p,
                o,t);
        return flattenProblem(t, r);
    }
public static MonotonicProblem[] flattenProblem(Triple t, MonotonicProblem r) {
        int lg = 0;
        MonotonicProblem x = r;
        while (x!=null){
            lg++;
            x = x.nextProblem();
        }
        
        MonotonicProblem rslt[] = new MonotonicProblem[lg];
        lg = 0;
        x = r;
        while (x!=null){
            rslt[lg++] =x;
            x = x.nextProblem();
        }
        for (int i = 0; i < rslt.length; i++)
            rslt[i].setTriple(t);
        return rslt;
    }
    /*
    static int assess(Graph g, MonotonicProblem p, Graph t) {
        if (p instanceof MultipleTripleProblem) {
            MultipleTripleProblem mtp = (MultipleTripleProblem) p;
            FullnessProof fp = new FullnessProof(g, t, mtp.getNode1(), mtp
                    .getNode2());
            return fp.extend();
        }
        else
            return 1;

    }
  */  
    static public MonotonicProblem[] allProblems(AbsChecker c, Triple t) {

        Graph g = c.justForErrorMessages == null ? c.hasBeenChecked
                : new DisjointUnion(c.hasBeenChecked, c.justForErrorMessages);
        Graph tt = FullnessProof.asGraph(t);
     MonotonicProblem x[] = getProblem(c,t);
     Vector v = new Vector();
     for (int i=0;i<x.length;i++) {
         if (x[i] instanceof MultipleTripleProblem) {
             MultipleTripleProblem mtp = (MultipleTripleProblem) x[i];
             FullnessProof fp = new FullnessProof(g, tt, mtp.getNode1(), mtp
                     .getNode2(), v, t);
             fp.extend();
         } else if (x[i] instanceof SingleTripleProblem){
             v.add(x[i]);
         } else {
             throw new BrokenException("Unreachable code.");
         }
     }
     return (MonotonicProblem[])v.toArray(new MonotonicProblem[v.size()]);
    }
/*
    static public int test(AbsChecker c, Triple t) {
        MonotonicProblem p[] = getProblem(c, t);
        Graph g = c.justForErrorMessages == null ? c.hasBeenChecked
                : new DisjointUnion(c.hasBeenChecked, c.justForErrorMessages);
        Graph tt = FullnessProof.asGraph(t);
        if (p.length == 1)
            return assess(g, p[0], tt);
        else
            return assess(g, p[0], tt) + 100 * assess(g, p[1], tt);

    }
*/
    /**
     * Public for testing purposes, entry point is with Triple
     * allProblems() or getProblems()
     */
    public MonotonicProblem getErrorCode(int s, int p, int o, Triple t) {
        //  Throws array access error if not 0 <= s,p,o,sx,px,ox < SZ.
        SimpleChecker empty = new SimpleChecker();
        int sx = empty.getCategory(t.getSubject());
        int px = empty.getCategory(t
                        .getPredicate());
        int ox = empty.getCategory(t.getObject());
        int key = look.qrefine(s, p, o);
        int meetCase = 0;
        if (t!=null){
            Node sn = t.getSubject();
            Node pn = t.getPredicate();
            Node on = t.getObject();
    		meetCase = ( pn.equals(on) ? 1 : 0 ) | ( sn.equals(on) ? 2 : 0 ) | ( sn.equals(pn) ? 4 : 0 );
        }
        if (key != Failure) {
            if (t!=null){
                int meet = qrefineWithMeet(s, p, o, meetCase);
				if ( meet == Failure ) {
					if (qrefineWithMeet(sx,px,ox,meetCase)==Failure) {
					    return singleTripleMeetError(meetCase,sx,px,ox);
					} else {
					    return multiTripleMeetError(meetCase,key,s,p,o,sx,px,ox);
					}
				} 
                
            }
            throw new IllegalArgumentException("Null triple, and categories fit together.");
        }
        rslt = null;
        key = look.qrefine(sx, px, ox);
        if (key == Failure) {
            return singleTripleError(sx, px, ox);
        }
        if (meetCase != 0 &&
                qrefineWithMeet(sx,px,ox,meetCase)==Failure) {
		    singleTripleMeetError(meetCase,sx,px,ox);
		}
        int given[] = { s, p, o };
        int general[] = { look.subject(sx, key), look.prop(px, key),
                look.object(ox, key) };
        for (int i = 0; i < 3; i++) {
            if (look.meet(general[i], given[i]) == Failure) {
                IncompatibleUsageProblem r = new IncompatibleUsageProblem(i);
                catMiss(r, general[i], given[i]);
                addResult(r);
            }
        }
        if (rslt == null) {
            return difficultCase(given, general);
        }
        return rslt;

    }

    /**
     * @param meetCase
     * @param key
     * @param s
     * @param p
     * @param o
     * @param sx
     * @param px
     * @param ox
     * @return
     */
    private MonotonicProblem multiTripleMeetError(int meetCase, int k, int s, int p, int o, int sx, int px, int ox) {
        if ( (meetCase & 1) != 0) {
            if ( p != o )
                throw new IllegalArgumentException("Meet stuff not honoured.");
            int key = look.qrefine(sx,p,o);
            if (key==Failure)
                throw new IllegalArgumentException("Called for too easy a case.");
            int pp = look.prop(p,key);
            int oo = look.object(o,key);
            if ( look.meet(pp,oo)==Failure ) {
                MultiTripleDuplicateNodeProblem problem = new MultiTripleDuplicateNodeProblem(1);
//     name in this context:
                CategorySetNames.symmetricNames(problem.wantedGiven(0),
                        nonPseudoCats(pp),nonPseudoCats(oo)
                        );
//     distinguish this context
                int kk = look.qrefine(sx,px,ox);
                int okm = look.meet(look.prop(px,kk),look.object(ox,kk));
                if (okm==Failure)
                    throw new IllegalArgumentException("Called for too easy a case.");
                CategorySetNames.symmetricNames(problem.wantedGiven(1),
                        nonPseudoCats(okm),nonPseudoCats(p));
                addResult(problem);
            }
        }
        if ( (meetCase & 2) != 0) {
            if ( s != o )
                throw new IllegalArgumentException("Meet stuff not honoured.");
            int key = look.qrefine(s,px,o);
            if (key==Failure)
                throw new IllegalArgumentException("Called for too easy a case.");
            int ss = look.prop(s,key);
            int oo = look.object(o,key);
            if ( look.meet(ss,oo)==Failure ) {
                MultiTripleDuplicateNodeProblem problem = new MultiTripleDuplicateNodeProblem(2);
//     name in this context:
                CategorySetNames.symmetricNames(problem.wantedGiven(0),
                        nonPseudoCats(ss),nonPseudoCats(oo)
                        );
//     distinguish this context
                int kk = look.qrefine(sx,px,ox);
                int okm = look.meet(look.prop(sx,kk),look.object(ox,kk));
                if (okm==Failure)
                    throw new IllegalArgumentException("Called for too easy a case.");
                CategorySetNames.symmetricNames(problem.wantedGiven(1),
                        nonPseudoCats(okm),nonPseudoCats(s));
                addResult(problem);
            }
        }
        if ( (meetCase & 4) != 0) {
            if ( s != p )
                throw new IllegalArgumentException("Meet stuff not honoured.");
            int key = look.qrefine(s,p,ox);
            if (key==Failure)
                throw new IllegalArgumentException("Called for too easy a case.");
            int ss = look.prop(s,key);
            int pp = look.object(p,key);
            if ( look.meet(ss,pp)==Failure ) {
                MultiTripleDuplicateNodeProblem problem = new MultiTripleDuplicateNodeProblem(2);
//     name in this context:
                CategorySetNames.symmetricNames(problem.wantedGiven(0),
                        nonPseudoCats(ss),nonPseudoCats(pp)
                        );
//     distinguish this context
                int kk = look.qrefine(sx,px,ox);
                int okm = look.meet(look.prop(sx,kk),look.object(px,kk));
                if (okm==Failure)
                    throw new IllegalArgumentException("Called for too easy a case.");
                CategorySetNames.symmetricNames(problem.wantedGiven(1),
                        nonPseudoCats(okm),nonPseudoCats(s));
                addResult(problem);
            }
        }
        if (rslt==null)
        
        addResult(new DeepMultiTripleDuplicateNodeProblem(meetCase));
        // TODO more work here
        return rslt;
    }
        /**
         * @param meetCase
         * @param sx
         * @param px
         * @param ox
         * @return
         */
        private MonotonicProblem singleTripleMeetError(int meetCase, int sx, int px, int ox) {
            // meetCase is 2.
            SingleTripleDuplicateNodeProblem problem = new SingleTripleDuplicateNodeProblem(meetCase);
            CategorySetNames.symmetricNames(problem,
                    nonPseudoCats(sx),nonPseudoCats(ox)
                    );
            addResult(problem);
            return rslt;
        }
    private int qrefineWithMeet(int s, int p, int o, int meetCase) {
        int key2 = look.qrefine(s, p, o);
        int s0 = look.subject(s, key2);
        int p0 = look.prop(p, key2);
        int o0 = look.object(o, key2);
        int meet;
        switch (meetCase) {
        	case 0:
        	  meet = 0;
        	break;
        	case 1:
        	 meet= p0 = o0 = look.meet(p0,o0);
        	 break;
        	case 2:
        	meet= s0 = o0 = look.meet(s0,o0);
        	break;
        	case 4:
        	meet= s0 = p0 = look.meet(s0,p0);
        	break;
        	case 7:
        	int msp = look.meet(s0,p0);
        	
        	meet= s0 = p0 = o0 = 
        	    (msp==Failure) ? Failure :
        	    
        	    look.meet(msp , o0 );
        	break;
        	default:
        	throw new BrokenException("Impossible meetcase");
        }
        return meet;
    }
    static private int spo(int f, int old, int key) {
        switch (f) {
        case 0:
            return look.subject(old, key);

        case 1:
            return look.prop(old, key);

        case 2:
            return look.object(old, key);
        }
        throw new BrokenException("Illegal argument to spo: " + f);
    }

    private MonotonicProblem difficultCase(int given[], int general[]) {

        int keyWithIGeneral[] = { look.qrefine(general[0], given[1], given[2]),
                look.qrefine(given[0], general[1], given[2]),
                look.qrefine(given[0], given[1], general[2]) };

        int keyWithIGiven[] = { look.qrefine(given[0], general[1], general[2]),
                look.qrefine(general[0], given[1], general[2]),
                look.qrefine(general[0], general[1], given[2]) };

        int cats[][] = new int[3][];
        int failures = 0;

        for (int i = 0; i < 3; i++)
            cats[i] = nonPseudoCats(given[i]);

        for (int i = 0; i < 3; i++) {
            if (keyWithIGiven[i] == Failure)
                throw new BrokenException("logic error");
        }
        for (int i = 0; i < 3; i++) {
            if (keyWithIGeneral[i] == Failure) {
                // the two that are not i have conflicting given values
                failures |= (1 << i);
                ComplexIncompatibleUsageProblem r = new ComplexIncompatibleUsageProblem(
                        i);
                computeGivenWanted(r, i, general, keyWithIGiven, cats);
                addResult(r);
            }
        }
        if (rslt == null)
            throw new BrokenException("logic error");

        return rslt;
    }

    /**
     * 
     * @param unchanged
     *            The other two keys conflict with one another
     * @param general
     *            The categories given by this triple alone
     * @param keyWithIGiven
     *            The result of qrefine with i being given by the other triples,
     *            the other two from this triple alone
     * 
     * @param givenCats
     *            The categories from the other triples
     */
    private static void computeGivenWanted(ComplexIncompatibleUsageProblem r,
            int unchanged, int[] general, int[] keyWithIGiven, int[][] givenCats) {
        int ix = 0;
        for (int i = 0; i < 3; i++)
            if (i != unchanged) {
                int want = spo(i, general[i], keyWithIGiven[3 - i - unchanged]);
                int wantC[] = nonPseudoCats(want);
                CategorySetNames.symmetricNames(r.wantedGiven(i), wantC,
                        givenCats[i]);

            }

    }

    public static boolean isLiteral(int o) {
        return o == Grammar.literal || o == Grammar.liteInteger
                || o == Grammar.dlInteger || o == Grammar.userTypedLiteral;
    }

    private void addResult(MonotonicProblem p) {
        if (rslt == null)
            rslt = p;
        else
            rslt.addNext(p);
    }

    private MonotonicProblem singleTripleError(int sx, int px, int ox) {
        if (isBlank(px))
            addResult(new BlankPropertyProblem());
        if (isLiteral(px))
            addResult(new LiteralPropertyProblem());
        if (isLiteral(sx))
            addResult(new LiteralSubjectProblem());

        if (sx == Grammar.badID)
            addResult(new BadURIProblem("%s"));
        if (px == Grammar.badID)
            addResult(new BadURIProblem("%p"));
        if (ox == Grammar.badID)
            addResult(new BadURIProblem("%o"));

        if (rslt != null)
            return rslt;

        if (!look.canBeSubj(sx)) {
            addResult(new IllegalSubjectProblem());
        }

        // The intent here is that we are only testing for built-in
        // non-predicates, but a few cases are both 'built-in'
        // and not isBuiltin[], e.g. rdf:Bag, which in much
        // of the code is just a URIref, but can only be a classID
        // and not a propID. The syntactic category given to rdf:Bag
        // initially is { notype, classID } which is not an isBuiltin[] like
        // owlClass.

        if (//isBuiltin[px]&&
        !look.canBeProp(px)) {
            addResult(new IllegalPredicateProblem());
        }

        if (rslt != null)
            return rslt;
        
        int sxx[] = nonPseudoCats(sx);
        int oxx[] = nonPseudoCats(ox);
        int pxx[] = nonPseudoCats(px);
        if (!inDomain(pxx, sxx)) {
            DomainRangeProblem r = new IllegalSubjectForPredicateProblem();
            CategorySetNames.symmetricNames(r, domain(pxx), sxx);
            addResult(r);
        }
        if (!inRange(pxx, oxx)) {
            DomainRangeProblem r = new IllegalObjectForPredicateProblem();
            CategorySetNames.symmetricNames(r, range(pxx), oxx);
            addResult(r);
        }
        if (rslt != null)
            return rslt;

        if (px == Grammar.rdftype) {
            return handleRDFType(sx, ox);
        }
        throw new BrokenException("Unreachable code.");
    }

    static private boolean inRange(int px[], int[] ox) {
        for (int i = 0; i < px.length; i++)
            if (Q.intersect(ox, look.range(px[i])))
                return true;

        return false;
    }

    static private boolean inDomain(int px[], int[] sx) {
        for (int i = 0; i < px.length; i++)
            if (Q.intersect(sx, look.domain(px[i])))
                return true;

        return false;
    }

    static private int[] domain(int p[]) {
        if (p.length == 1)
            return look.domain(p[0]);
        Set d = new TreeSet();
        for (int i = 0; i < p.length; i++) {
            int dd[] = look.domain(p[i]);
            for (int j = 0; j < dd.length; j++)
                d.add(new Integer(dd[j]));
        }
        int rslt[] = new int[d.size()];
        Iterator it = d.iterator();
        int ix = 0;
        while (it.hasNext())
            rslt[ix++] = ((Integer) it.next()).intValue();
        return rslt;
    }

    static private int[] range(int p[]) {
        if (p.length == 1)
            return look.range(p[0]);
        Set d = new TreeSet();
        for (int i = 0; i < p.length; i++) {
            int dd[] = look.range(p[i]);
            for (int j = 0; j < dd.length; j++)
                d.add(new Integer(dd[j]));
        }
        int rslt[] = new int[d.size()];
        Iterator it = d.iterator();
        int ix = 0;
        while (it.hasNext())
            rslt[ix++] = ((Integer) it.next()).intValue();
        return rslt;
    }

    /**
     * @param sx
     * @param ox
     */
    private static MonotonicProblem handleRDFType(int sx, int ox) {
        if (maybeBuiltinID(sx))
            return new BuiltinURIGivenTypeProblem();

        if (isPropertyOnly(sx)) {

            switch (ox) {
            case Grammar.rdfsDatatype:
            case Grammar.rdfsClass:
            case Grammar.owlClass:
            case Grammar.owlOntology:
            case Grammar.owlDeprecatedClass:
            case Grammar.owlOntologyProperty: // not permitted!! See
                // http://www.w3.org/TR/owl-semantics/mapping.html#separated_vocabulary
                return new PropertyOnlyURIWithOtherTypeProblem();

            }
            if (isUserID(ox) || isBlank(ox))
                return new PropertyOnlyURIWithOtherTypeProblem();
        }
        switch (ox) {
        case Grammar.owlOntology:
            if (isClassOnly(sx))
                return new ClassOnlyURIWithOtherTypeProblem();
            if (!isUserID(sx) && !isBlank(sx))
                return new TypeNeedsBlankOrUserIDSubjectProblem();
            break;
        case Grammar.rdfsDatatype:
        case Grammar.rdfProperty:
        case Grammar.owlAnnotationProperty:
        case Grammar.owlDatatypeProperty:
        case Grammar.owlFunctionalProperty:
        case Grammar.owlInverseFunctionalProperty:
        case Grammar.owlObjectProperty:
        case Grammar.owlOntologyProperty:
        case Grammar.owlSymmetricProperty:
        case Grammar.owlTransitiveProperty:
        case Grammar.owlDeprecatedProperty:
            if (isClassOnly(sx))
                return new ClassOnlyURIWithOtherTypeProblem();
            if (!isUserID(sx))
                return new TypeNeedsUserIDSubjectProblem();
            break;
        case Grammar.owlDeprecatedClass:
            if (!isUserID(sx))
                return new TypeNeedsUserIDSubjectProblem();
            break;
        case Grammar.rdfList:
        case Grammar.owlAllDifferent:
        case Grammar.owlDataRange:
        case Grammar.owlRestriction:
            if (!isBlank(sx))
                return new TypeNeedsBlankSubjectProblem();
            break;
        default:
            if (isBlank(ox) || isUserID(ox)) {
                if (isClassOnly(sx))
                    return new ClassOnlyURIWithOtherTypeProblem();
            }
        }
        throw new BrokenException("Unreachable code. "
                + CategorySet.catString(sx) + " rdf:type "
                + CategorySet.catString(ox));
    }

    public static boolean maybeBuiltinID(int sx) {
        boolean builtin;
        switch (sx) {
        case Grammar.annotationPropID:// rdfs:seeAlso ??
        case Grammar.datatypeID: //xsd:int
        case Grammar.dataRangeID: //rdfs:Literal
        case Grammar.dataAnnotationPropID: // rdfs:label
        case Grammar.classID: // owl:Thing
        case Grammar.ontologyPropertyID: // owl:priorVersion
            builtin = true;
            break;
        default:
            builtin = false;
            break;
        }
        return builtin;
    }

    public static boolean isClassOnly(int sx) {
        return look.meet(sx, Grammar.classOnly) == sx;
    }

    public static boolean isPropertyOnly(int sx) {
        return look.meet(sx, Grammar.propertyOnly) == sx;
    }

    public static boolean isBlank(int sx) {
        return look.meet(sx, Grammar.blank) == sx;
    }

    public static boolean isUserID(int sx) {
        return look.meet(sx, Grammar.userID) == sx;
    }

    /**
     * categories without pseudo-categories. Note the pseuds (if any) all appear
     * at the beginning, by (a) sorting and (b) arrangement of the grammar.
     */
    static private int[] nonPseudoCats(int c) {
        int cats[] = CategorySet.getSet(c);
        int i;
        for (i = 0; i < cats.length; i++) {
            if (!Grammar.isPseudoCategory(cats[i]))
                break;
        }
        if (i == 0)
            return cats;

        int rslt[] = new int[cats.length - i];
        System.arraycopy(cats, i, rslt, 0, rslt.length);
        return rslt;

    }

    /**
     * precondition !Q.intersect(wantedC,givenC)
     * 
     * @param wantedC
     *            from this triple only
     * @param givenC
     *            from other triples
     * @return
     */
    static private void catMiss(WantedGiven w, int wantedC, int givenC) {
        if (isBlank(wantedC) != isBlank(givenC))
            throw new BrokenException("Logic error");
        CategorySetNames.symmetricNames(w, nonPseudoCats(wantedC),
                nonPseudoCats(givenC));
    }
}
/*
 * (c) Copyright 2003-2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
