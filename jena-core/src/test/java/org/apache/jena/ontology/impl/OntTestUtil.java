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

package org.apache.jena.ontology.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * Collection of utilities to assist with unit testing.
 * <p>
 * The {@code assertIterator*} methods are derived from
 * {@link org.apache.jena.reasoner.test.TestUtil} so that this package can be
 * migrated to JUnit6 independently. The {@code junit.framework.TestCase}
 * argument of the originals has been dropped: it served only to label failure
 * messages and to name the logger, both of which JUnit6 reports for itself.
 */
class OntTestUtil {

    private static final Logger LOG = LoggerFactory.getLogger( OntTestUtil.class );

    /**
     * Helper method to test an iterator against a list of objects - order independent
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     */
    static void assertIteratorValues(Iterator<?> it, Object[] vals) {
        assertIteratorValues( it, vals, 0 );
    }

    /**
     * Helper method to test an iterator against a list of objects - order independent, and
     * can optionally check the count of anonymous resources.  This allows us to test a
     * iterator of resource values which includes both URI nodes and bNodes.
     * @param it The iterator to test
     * @param vals The expected values of the iterator
     * @param countAnon If non zero, count the number of anonymous resources returned by <code>it</code>,
     * and don't check these resources against the expected <code>vals</code>.
     */
    static void assertIteratorValues(Iterator<?> it, Object[] vals, int countAnon ) {
        boolean[] found = new boolean[vals.length];
        int anonFound = 0;

        for (int i = 0; i < vals.length; i++) found[i] = false;

        while (it.hasNext()) {
            Object n = it.next();
            boolean gotit = false;

            // do bNodes separately
            if (countAnon > 0 && isAnonValue( n )) {
                anonFound++;
                continue;
            }

            for (int i = 0; i < vals.length; i++) {
                if (n.equals(vals[i])) {
                    gotit = true;
                    found[i] = true;
                }
            }
            if (!gotit) {
                LOG.debug( "found unexpected iterator value: " + n);
            }
            assertTrue( gotit, "found unexpected iterator value: " + n);
        }

        // check that no expected values were unfound
        for (int i = 0; i < vals.length; i++) {
            if (!found[i]) {
                LOG.debug( "failed to find expected iterator value: " + vals[i]);
            }
            assertTrue( found[i], "failed to find expected iterator value: " + vals[i]);
        }

        // check we got the right no. of anons
        assertEquals( countAnon, anonFound, "iterator test did not find the right number of anon. nodes" );
    }

    /**
     * Check the length of an iterator.
     */
    static void assertIteratorLength(Iterator<?> it, int expectedLength) {
        int length = 0;
        while (it.hasNext()) {
            it.next();
            length++;
        }
        assertEquals(expectedLength, length);
    }

    /**
     * For the purposes of counting, a value is anonymous if (a) it is an anonymous resource,
     * or (b) it is a statement with a bNode subject or (c) it is a statement with a bNode
     * object.  This is because we cannot check bNode identity against fixed expected data values.
     * @param n A value
     * @return True if n is anonymous
     */
    static boolean isAnonValue( Object n ) {
        return ((n instanceof Resource) && ((Resource) n).isAnon()) ||
               ((n instanceof Statement) && ((Statement) n).getSubject().isAnon()) ||
               ((n instanceof Statement) && isAnonValue( ((Statement) n).getObject() ));
    }
}
