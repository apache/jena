/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.impl;

import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.graph.compose.*;
import java.util.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * @author Jeremy J. Carroll
 *  
 */
class FullnessProof implements Constants {

    static Lookup look = LookupTable.get();
    private final Vector results;
    private final Triple rootTriple;

    /**
     *  
     */
    public FullnessProof(Graph context, Graph counterExample, Node n1, Node n2,
            Vector v, Triple t) {
        this.context = context;
        this.counterExample = counterExample;
        this.n1 = n1;
        this.n2 = n2;
        this.results = v;
        this.rootTriple =t;
        check();
    }

    /**
     * The counterExample, when augmented by the category information from
     * context for n1 and n2 (both of which may be null), is in Full. Omitting
     * any triple from the counterExample, or omitting either n1 or n2 puts the
     * counterExample into DL or Lite.
     */
    final private Graph context;

    final private Graph counterExample;

    final private Node n1, n2;

    Graph minus(Graph g, Graph h) {
        return new Difference(g, h);
    }

    Graph minus(Graph g, Triple t) {
        return minus(g, asGraph(t));
    }

    Graph add(Graph g, Graph h) {
        return new DisjointUnion(g, h);
    }

    Graph add(Graph g, Triple t) {
        return add(g, asGraph(t));
    }

    static Graph asGraph(Triple t) {
        Graph rslt = Factory.createDefaultGraph(ReificationStyle.Minimal);
        rslt.add(t);
        return rslt;
    }

    /**
     * @return True if in full, false if not
     * @param xt
     *            If non-null exclude this triple from check
     * @param xn
     *            If non-null exclude this node from check.
     */
    boolean checkSubInvariant(Triple xt, Node xn) {
        if (!contextContains(xn))
            throw new IllegalArgumentException("Node not in context");
        return isFull(xt == null ? counterExample : minus(counterExample, xt),
                xn, context);
    }

    private boolean contextContains(Node n) {
        if (n==null) return true;
        ExtendedIterator it = allUses(n);
        try {
            return it.hasNext();
        } finally {
            it.close();
        }
    }

    private ExtendedIterator allUses(Node n) {
        if (n == null)
            return NiceIterator.emptyIterator();
        return

        context.find(n, Node.ANY, Node.ANY).andThen(
                context.find(Node.ANY, n, Node.ANY).andThen(
                        context.find(Node.ANY, Node.ANY, n)));
    }
    boolean isFull(Graph g, Node xn,  Graph context) {
        return isFull(g,xn,null,context);
    }
    boolean isFull(Graph g, Node xn, Node replace, Graph context) {
        SimpleChecker ctxt = new SimpleChecker(context);
        SimpleChecker s = exampleWithNodeReplaced(g, xn, replace, ctxt);
        boolean rslt = s.monotoneLevel == Levels.Full;
        return rslt;

    }

    private SimpleChecker exampleWithNodeReplaced(Graph g, Node xn,
            Node replace,
            SimpleChecker ctxt) {
        SimpleChecker s = new SimpleChecker(g);
        if (n1 != xn && n1 != null)
            apply(s, n1, ctxt);
        if (n2 != xn && n2 != null)
            apply(s, n2, ctxt);
        if (replace != null)
            apply(s, replace, ctxt);
        return s;
    }

    void apply(SimpleChecker s, Node n, SimpleChecker ctxt) {

        s.meetCat(n, ctxt.getCategory(n));
    }

    boolean checkInvariants() {
        if (!counterExample.contains(rootTriple))
            throw new BrokenException("Invariant failure in FullnessProof");
        if (!checkSubInvariant(null, null))
            throw new BrokenException("Invariant failure in FullnessProof");
        if (n1 != null && checkSubInvariant(null, n1))
            throw new BrokenException("Invariant failure in FullnessProof");
        if (n2 != null && checkSubInvariant(null, n2))
            throw new BrokenException("Invariant failure in FullnessProof");
        Iterator it = counterExample.find(Node.ANY, Node.ANY, Node.ANY);
        while (it.hasNext())
            if (checkSubInvariant((Triple) it.next(), null))
                throw new BrokenException("Invariant failure in FullnessProof");
        return true;
    }

    void check() {
        if (!checkInvariants())
            throw new BrokenException("Invariant failure in FullnessProof");
    }

    private int getCat(SimpleChecker a, SimpleChecker b, Node n) {
        int rslt = look.meet(a.getCategory(n), b.getCategory(n));
        if (rslt == Failure)
            throw new BrokenException("Logic error");
        return rslt;
    }
    
    private boolean extendWith(Triple t, Node n, MonotonicProblem mp) {
        if (mp instanceof MultipleTripleProblem) {
            MultipleTripleProblem mtp = (MultipleTripleProblem)mp;
            mtp.setTriple(t);
            Node nn1 = mtp.getNode1();
            Node nn2 = mtp.getNode2();
            if (n==nn1) 
                nn1=nn2;
            else if (n!=nn2) {
                // TODO log shouldn't happen
                throw new BrokenException("Logic error");
                //return false;
            }
            // now replace n with nn1
            boolean rslt = saveIfFull(add(counterExample, t), 
                    n, nn1, 
                    minus(context, t));
            if (!rslt) {
                // TODO log shouldn't happen

                throw new BrokenException("Logic error");
            }
            return rslt;
            
        }
        // TODO log shouldn't happen
        throw new BrokenException("Logic error");
        //return false;
    }

    private boolean oneTripleExtend(Triple t, Node n) {

        SimpleChecker ctxt = new SimpleChecker(minus(context, t));
        SimpleChecker T = new SimpleChecker(Factory
                .createDefaultGraph(ReificationStyle.Minimal));
        SimpleChecker counter = exampleWithNodeReplaced(counterExample, n,null, ctxt);

        int s = getCat(ctxt, counter, t.getSubject());
        int p = getCat(ctxt, counter, t.getPredicate());
        int o = getCat(ctxt, counter, t.getObject());

        int sx = T.getCategory(t.getSubject());
        int px = T.getCategory(t.getPredicate());
        int ox = T.getCategory(t.getObject());

        if (look.qrefine(s, p, o) != Failure)
            return false;

        MonotonicProblem mp = new MonotonicErrorAnalyzer().getErrorCode(s, p,
                o, sx, px, ox);
        boolean ok = extendWith(t,n,mp);
        if (mp.nextProblem()!=null)
            ok = extendWith(t,n,mp.nextProblem()) || ok;
        return ok;
    }

    private boolean oneTripleSolve(Triple t, Node n) {
        return saveIfFull(add(counterExample, t), n, minus(context, t));
        
    }

    private boolean saveIfFull(Graph counterEx, Node n, Graph ctxt){
        return saveIfFull(counterEx,n,null,ctxt);
    }
    private boolean saveIfFull(Graph counterEx, Node n, Node replace, Graph ctxt){
        boolean rslt = isFull(counterEx, n, replace, ctxt);
        if (rslt) {
        FullnessProof nxt = new
        FullnessProof(ctxt, counterEx, n==n1?replace:n1, 
               n==n2?replace:n2,
                results, rootTriple);
        nxt.extend();
        }
        return rslt;
    }
    private boolean multiTripleSolve(Node n) {
        if (n == null)
            return false;
        Graph g = Factory.createDefaultGraph(ReificationStyle.Minimal);
        Iterator it = allUses(n);
        while (it.hasNext())
            g.add((Triple) it.next());
        if (isFull(add(counterExample, g), n, minus(context, g))) {
            
            Triple at[] = new Triple[g.size()];
            it = g.find(Node.ANY,Node.ANY,Node.ANY);
            int i = 0;
            while (it.hasNext())
                at[i++] = (Triple)it.next();
            for (i=0;i<at.length;i++) {
                g.delete(at[i]);
                if (!isFull(add(counterExample, g), n, minus(context, g)))
                    g.add(at[i]);
            }
            boolean rslt = saveIfFull(add(counterExample, g),n,minus(context, g));
            if (!rslt) {
                // TODO log message

                throw new BrokenException("Logic error");
            }
                
            return rslt;
        }
        return false;
    }

    private void saveResult() {
        SimpleChecker chk = new SimpleChecker(minus(counterExample,rootTriple));
        MonotonicProblem mp[] = MonotonicErrorAnalyzer.getProblem(chk, rootTriple);
        for (int i=0; i<mp.length;i++) {
            ((MultipleTripleProblem)mp[i]).setGraph(counterExample);
            results.add(mp[i]);
        }
    }
    public void extend() {
        if (n1 == null && n2 == null) {
            saveResult();
        } else {
            ExtendedIterator it = allUses(n1);
            while (it.hasNext())
                if (oneTripleSolve((Triple) it.next(), n1))
                    return;
            it = allUses(n2);
            while (it.hasNext())
                if (oneTripleSolve((Triple) it.next(), n2))
                    return;
            if (multiTripleSolve(n1))
                return;
            if (multiTripleSolve(n2))
                return;
            it = allUses(n1);
            while (it.hasNext()) {
                if( oneTripleExtend((Triple) it.next(), n1))
                    return;
            }

            it = allUses(n2);
            while (it.hasNext()) {
                if( oneTripleExtend((Triple) it.next(), n2))
                    return;
            }
        }
    }

}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

