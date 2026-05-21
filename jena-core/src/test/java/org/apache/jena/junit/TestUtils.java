/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.junit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.util.CollectionFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class TestUtils {
    public static Resource getResource(Resource r, Property p) {
        if ( r == null )
            return null;
        if ( !r.hasProperty(p) )
            return null;

        RDFNode n = r.getProperty(p).getObject();
        if ( n instanceof Resource )
            return (Resource)n;

        throw new TestException("Manifest problem (not a Resource): " + n + " => " + p);
    }

    public static Collection<Resource> listResources(Resource r, Property p) {
        if ( r == null )
            return null;
        List<Resource> x = new ArrayList<>();
        StmtIterator sIter = r.listProperties(p);
        for ( ; sIter.hasNext() ; ) {
            RDFNode n = sIter.next().getObject();
            if ( !(n instanceof Resource) )
                throw new TestException("Manifest problem (not a Resource): " + n + " => " + p);
            x.add((Resource)n);
        }
        return x;
    }

    public static String getLiteral(Resource r, Property p) {
        if ( r == null )
            return null;
        if ( !r.hasProperty(p) )
            return null;

        RDFNode n = r.getProperty(p).getObject();
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm();

        throw new TestException("Manifest problem (not a Literal): " + n + " => " + p);
    }

    public static String getLiteralOrURI(Resource r, Property p) {
        if ( r == null )
            return null;

        if ( !r.hasProperty(p) )
            return null;

        RDFNode n = r.getProperty(p).getObject();
        if ( n instanceof Literal )
            return ((Literal)n).getLexicalForm();

        if ( n instanceof Resource ) {
            Resource r2 = (Resource)n;
            if ( !r2.isAnon() )
                return r2.getURI();
        }

        throw new TestException("Manifest problem: " + n + " => " + p);
    }

    public static String safeName(String s) {
        // Safe from Eclipse
        s = s.replace('(', '[');
        s = s.replace(')', ']');
        return s;

    }

    /**
     * Answer a Set formed from the elements of the List <code>L</code>.
     */
    public static <T> Set<T> listToSet(List<T> L) {
    	return CollectionFactory.createHashedSet(L);
    }

    /**
     * Answer a List of the substrings of <code>s</code> that are separated by
     * spaces.
     */
    public static List<String> listOfStrings(String s) {
    	List<String> result = new ArrayList<>();
    	StringTokenizer st = new StringTokenizer(s);
    	while (st.hasMoreTokens())
    		result.add(st.nextToken());
    	return result;
    }

    /**
     * Answer a Set of the substrings of <code>s</code> that are separated by
     * spaces.
     */
    public static Set<String> setOfStrings(String s) {
    	Set<String> result = new HashSet<>();
    	StringTokenizer st = new StringTokenizer(s);
    	while (st.hasMoreTokens())
    		result.add(st.nextToken());
    	return result;
    }

    /**
     * Answer a list containing the single object <code>x</code>.
     */
    public static <T> List<T> listOfOne(T x) {
    	List<T> result = new ArrayList<>();
    	result.add(x);
    	return result;
    }

    /**
     * Answer a Set containing the single object <code>x</code>.
     */
    public static <T> Set<T> setOfOne(T x) {
    	Set<T> result = new HashSet<>();
    	result.add(x);
    	return result;
    }

    /**
     * Answer a fresh list which is the concatenation of <code>L</code> then
     * <code>R</code>. Neither <code>L</code> nor <code>R</code> is updated.
     */
    public static <T> List<T> append(List<? extends T> L, List<? extends T> R) {
    	List<T> result = new ArrayList<>(L);
    	result.addAll(R);
    	return result;
    }

    /**
     * Answer an iterator over the space-separated substrings of <code>s</code>.
     */
    protected static ExtendedIterator<String> iteratorOfStrings(String s) {
    	return WrappedIterator.create(listOfStrings(s).iterator());
    }

    /**
     * Answer the constructor of the class <code>c</code> which takes arguments
     * of the type(s) in <code>args</code>, or <code>null</code> if there isn't
     * one.
     */
    public static Constructor<?> getConstructor(Class<?> c, Class<?>[] args) {
    	try {
    		return c.getConstructor(args);
    	} catch (NoSuchMethodException e) {
    		return null;
    	}
    }

    /**
     * Answer true iff <code>subClass</code> is the same class as
     * <code>superClass</code>, if its superclass <i>is</i>
     * <code>superClass</code>, or if one of its interfaces hasAsInterface that
     * class.
     */
    public static boolean hasAsParent(Class<?> subClass, Class<?> superClass) {
    	if (subClass == superClass || subClass.getSuperclass() == superClass)
    		return true;
    	Class<?>[] is = subClass.getInterfaces();
    	for (int i = 0; i < is.length; i += 1)
    		if (hasAsParent(is[i], superClass))
    			return true;
    	return false;
    }

    static URL getURL(String fn) {
    	URL u = TestUtils4.class.getClassLoader().getResource(fn);
    	if (u == null) {
    		throw new RuntimeException(new FileNotFoundException(fn));
    	}
    	return u;
    }

    public static String getFileName(String fn) {
    
    	try {
    		return getURL(fn).toURI().toString();
    	} catch (URISyntaxException e) {
    		throw new RuntimeException(e);
    	}
    }

    public static InputStream getInputStream(String fn) throws IOException {
    	return getURL(fn).openStream();
    }
}
