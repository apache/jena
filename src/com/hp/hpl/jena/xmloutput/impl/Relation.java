/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 *
 * $Id: Relation.java,v 1.4 2003-08-27 13:11:16 andy_seaborne Exp $
 *
 */

package com.hp.hpl.jena.xmloutput.impl;

import java.util.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 * A sparse 2 dimensional array of boolean indexed by Object.
 *
 * Complete with transitive closure algorithm.
 * @author jjc
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.4 $' Date='$Date: 2003-08-27 13:11:16 $'
 */
class Relation {
    final private Map rows;
    final private Map cols;
    final private Set index;
    /** The empty Relation.
     */
    public Relation() {
        rows = new HashMap();
        cols = new HashMap();
        index = new HashSet();
    }
    /** <code>a</code> is now related to <code>b</code>
     *
     */
    synchronized public void set(Object a, Object b) {
        index.add(a);
        index.add(b);
        innerAdd(rows, a, b);
        innerAdd(cols, b, a);
    }
    /** Uniquely <code>a</code> is now related to uniquely <code>b</code>.
     *
     *  When this is called any other <code>a</code> related to this <code>b</code> is removed.
     *  When this is called any other <code>b</code> related to this <code>a</code> is removed.
     *
     */
    synchronized public void set11(Object a, Object b) {
        clearX(a, forward(a));
        clearX(backward(b), b);
        set(a, b);
    }
    /** Uniquely <code>a</code> is now related to <code>b</code>.
     *  Many <code>b</code>'s can be related to each <code>a</code>.
     *  When this is called any other <code>a</code> related to this <code>b</code> is removed.
     */
    synchronized public void set1N(Object a, Object b) {
        clearX(backward(b), b);
        set(a, b);
    }
    /** <code>a</code> is now related to uniquely <code>b</code>.
     *  Many <code>a</code>'s can be related to each <code>b</code>.
     *  When this is called any other <code>b</code> related to this <code>a</code> is removed.
     */
    synchronized public void setN1(Object a, Object b) {
        clearX(a, forward(a));
        set(a, b);
    }
    /** <code>a</code> is now related to <code>b</code>
     *
     */
    synchronized public void setNN(Object a, Object b) {
        set(a, b);
    }
    /** <code>a</code> is now <em>not</em> related to <code>b</code>
     *
     */
    synchronized public void clear(Object a, Object b) {
        innerClear(rows, a, b);
        innerClear(cols, b, a);
    }
    private void clearX(Set s, Object b) {
        if (s == null)
            return;
        Iterator it = s.iterator();
        while (it.hasNext())
            clear(it.next(), b);
    }
    private void clearX(Object a, Set s) {
        if (s == null)
            return;
        Iterator it = s.iterator();
        while (it.hasNext())
            clear(a, it.next());
    }
    static private void innerAdd(Map s, Object a, Object b) {
        Set vals = (Set) s.get(a);
        if (vals == null) {
            vals = new HashSet();
            s.put(a, vals);
        }
        vals.add(b);
    }
    static private void innerClear(Map s, Object a, Object b) {
        Set vals = (Set) s.get(a);
        if (vals != null) {
            vals.remove(b);
        }
    }
    /** Is <code>a</code> related to <code>b</code>?
     *
     */
    public boolean get(Object a, Object b) {
        Set vals = (Set) rows.get(a);
        return vals != null && vals.contains(b);
    }
    /**
     * Takes this to its transitive closure.
    See B. Roy. <b>Transitivité et connexité.</b> <i>C.R. Acad. Sci.</i> Paris <b>249</b>, 1959 pp 216-218.
    or
    S. Warshall, <b>A theorem on Boolean matrices</b>, <i>Journal of the ACM</i>, <b>9</b>(1), 1962, pp11-12
    
    
     */
    synchronized public void transitiveClosure() {
        Iterator j = index.iterator();
        while (j.hasNext()) {
            Object oj = j.next();
            Set si = (Set) cols.get(oj);
            Set sk = (Set) rows.get(oj);
            if (si != null && sk != null) {
                Iterator i = si.iterator();
                while (i.hasNext()) {
                    Object oi = i.next();
                    if (oi != oj) {
                        Iterator k = sk.iterator();
                        while (k.hasNext()) {
                            Object ok = k.next();
                            if (ok != oj)
                                set(oi, ok);
                        }
                    }
                }
            }
        }
    }
    /**
     * The set of <code>a</code> such that <code>a</code> is related to <code>a</code>.
     *
     */
    synchronized public Set getDiagonal() {
        Set rslt = new HashSet();
        Iterator it = index.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (get(o, o))
                rslt.add(o);
        }
        return rslt;
    }
    /**
     * The set of <code>b</code> such that <code>a</code> is related to <code>b</code>.
     *
     */
    public Set forward(Object a) {
        return (Set) rows.get(a);
    }
    /**
     * The set of <code>a</code> such that <code>a</code> is related to <code>b</code>.
     *
     */
    public Set backward(Object b) {
        return (Set) cols.get(b);
    }
    /**
     * An Iterator over the pairs of the Relation.
     * Each pair is returned as a java.util.Map.Entry.
     * The first element is accessed through <code>getKey()</code>,
     * the second through <code>getValue()</code>.
     *@see java.util.Map.Entry
     */
    public Iterator iterator() {
        return new IteratorIterator(new Map1Iterator(new Map1() {
            // Convert a Map.Entry into an iterator over Map.Entry
            public Object map1(Object o) {
                Map.Entry pair = (Map.Entry) o;
                final Object a = pair.getKey();
                Set bs = (Set) pair.getValue();
                return new Map1Iterator(
                // Converts a b into a Map.Entry pair.
                new Map1() {
                    public Object map1(Object b) {
                        return new PairEntry(a, b);
                    }
                }, bs.iterator());
            }
        }, rows.entrySet().iterator()));
    }
    
    synchronized public Relation copy() {
        Relation rslt = new Relation();
        Iterator it = iterator();
        while ( it.hasNext() ) {
            Map.Entry e = (Map.Entry)it.next();
            rslt.set(e.getKey(),e.getValue());
        }
        return rslt;
    }
}
