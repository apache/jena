/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestNodeCache.java,v 1.2 2011-03-23 13:29:20 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;

/**
    TestNodeCache - make some (minimal, driving) tests for the NodeCache used
    to reduce store turnover for repeated Node construction.

    @author kers
*/
public class TestNodeCache extends GraphTestBase 
    {
	public TestNodeCache( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNodeCache.class ); }
        
    /**
         Utility to find short strings with the same hashcode, which can be used as
         the basis for constructing nodes with the same hashcode - this is what
         was used to find the clashing strings used below.
    */
    public static void main( String [] ignoredArguments )
        {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<Integer, Set<String>> strings = new HashMap<Integer, Set<String>>();
        for (int i = 0; i < alphabet.length(); i += 1)
            for (int j = 0; j < alphabet.length(); j += 1)
                for (int k = 0; k < alphabet.length(); k += 1)
                    {
                    String s = "" + alphabet.charAt( i ) + alphabet.charAt( j ) + alphabet.charAt( k );
                    if (i == 10 && j == 10) System.err.println( s );
                    int hash = s.hashCode();
                    if (strings.get( hash ) == null) strings.put( hash, new HashSet<String>() );
                    strings.get( hash ).add( s );
                    }
        for (Map.Entry<Integer, Set<String>> e: strings.entrySet())
            {
            Set<String> clashing = e.getValue();
            if (clashing.size() > 1)
                System.err.println( "hash " + e.getKey() + " has clashing strings " + clashing );
            }
        }
    
    /**
         Visible evidence that the pairs of strings used have the same hashcode.
    */
    public void testClashettes()
        {
        assertEquals( "eg:aa".hashCode(), "eg:bB".hashCode() );
        assertEquals( "eg:ab".hashCode(), "eg:bC".hashCode() );
        assertEquals( "eg:ac".hashCode(), "eg:bD".hashCode() );
        assertEquals( "eg:Yv".hashCode(), "eg:ZW".hashCode() );
        assertEquals( "eg:Yx".hashCode(), "eg:ZY".hashCode() );
        }
    
    /**
         An array of distinct URIs as ad-hoc probes into the cache under test.
     */
    protected static String [] someURIs = inventURIs();
    
    protected static String [] inventURIs()
        {
        String [] a = { "a", "mig", "spoo-", "gilbert", "124c41+" };
        String [] b = { "b", "class", "procedure", "spindizzy", "rake" };
        String [] c = { "c", "bucket", "42", "+1", "#mark" };
        int here = 0;
        String [] result = new String[a.length * b.length * c.length];
        for (int i = 0; i < a.length; i += 1)
            for (int j = 0; j < b.length; j += 1)
                for (int k = 0; k < c.length; k += 1)
                    result[here++] = "eg:" + a[i] + b[j] + c[k];
        return result;
        }
    
    /**
        test that a new cache is empty - none of the proble URIs are bound. 
    */
    public void testNewCacheEmpty()
        {
        NodeCache c = new NodeCache();
        for (int i = 0; i < someURIs.length; i += 1) assertEquals( null, c.get( someURIs[i] ) );
        }
        
    /**
        test that an element put into the cache is immediately retrievable
    */
    public void testNewCacheUpdates()
        {
        NodeCache c = new NodeCache();
        for (int i = 0; i < someURIs.length; i += 1) 
            {
            Node it = Node.createURI( someURIs[i] );
            c.put( someURIs[i], it );
            assertEquals( it, c.get( someURIs[i] ) );
            }
        }
    
    /**
        test that labels with the same hashcode are not confused.
    */
    public void testClashing()
        {
        String A = "eg:aa", B = "eg:bB";
        assertEquals( A.hashCode(), B.hashCode() );
    /* */
        NodeCache c = new NodeCache();
        c.put( A, Node.createURI( A ) );
        assertEquals( null, c.get( B ) );
        c.put( B, Node.createURI( B ) );
        assertEquals( Node.createURI( B ), c.get( B ) );
        }
    
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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