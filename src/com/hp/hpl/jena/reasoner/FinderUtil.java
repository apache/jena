/******************************************************************
 * File:        FinderUtil.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: FinderUtil.java,v 1.5 2003-06-23 15:49:41 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.util.iterator.*;

/**
 * Some simple helper methods used when working with Finders,
 * particularly to compose them into cascade sequences.
 * The cascades are designed to cope with null Finders as well.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-06-23 15:49:41 $
 */
public class FinderUtil {
    
    /**
     * Create a continuation object which is a cascade of two
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second) {
        if (first == null || (first instanceof FGraph && ((FGraph)first).getGraph() == null)) return second;
        if (second == null || (second instanceof FGraph && ((FGraph)second).getGraph() == null)) return first;
        return new Cascade(first, second);
    }
    
    /**
     * Create a continuation object which is a cascade of three
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     * @param third the third Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second, Finder third) {
        return new Cascade(first, cascade(second, third));
    }
    
    /**
     * Create a continuation object which is a cascade of four
     * continuation objects.
     * @param first the first Graph/Finder to try
     * @param second the second Graph/Finder to try
     * @param third the third Graph/Finder to try
     * @param fourth the third Graph/Finder to try
     */
    public static Finder cascade(Finder first, Finder second, Finder third, Finder fourth) {
        return new Cascade(first, cascade(second, cascade(third, fourth)));
    }
    
    /**
     * Inner class used to implement cascades of two continuation objects
     */
    private static class Cascade implements Finder {
        /** the first Graph/Finder to try */
        Finder first;
        
        /** the second Graph/Finder to try */
        Finder second;
        
        /**
         * Constructor 
         */
        Cascade(Finder first, Finder second) {
            this.first = first;
            this.second = second;
        }
        
        /**
         * Basic pattern lookup interface.
         * @param pattern a TriplePattern to be matched against the data
         * @return a ClosableIterator over all Triples in the data set
         *  that match the pattern
         */
        public ExtendedIterator find(TriplePattern pattern) {
            if (second == null) {
                return first.find(pattern);
            } else if (first == null) {
                return second.find(pattern);
            } else {
                return first.findWithContinuation(pattern, second);
            }
        }
        
        /**
         * Extended find interface used in situations where the implementator
         * may or may not be able to answer the complete query. It will
         * attempt to answer the pattern but if its answers are not known
         * to be complete then it will also pass the request on to the nested
         * Finder to append more results.
         * @param pattern a TriplePattern to be matched against the data
         * @param continuation either a Finder or a normal Graph which
         * will be asked for additional match results if the implementor
         * may not have completely satisfied the query.
         */
        public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
            return (FinderUtil.cascade(first, second, continuation)).find(pattern);
        }

        /**
         * Return true if the given pattern occurs somewhere in the find sequence.
         */
        public boolean contains(TriplePattern pattern) {
            ClosableIterator it = find(pattern);
            boolean result = it.hasNext();
            it.close();
            return result;
        }

    }
}

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

