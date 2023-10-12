/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.expiring_map;

import org.junit.After;
import org.junit.AfterClass;

import static org.apache.jena.ext.io.github.galbiston.expiring_map.MapDefaultValues.MAP_CLEANER_INTERVAL;
import static org.apache.jena.ext.io.github.galbiston.expiring_map.MapDefaultValues.MINIMUM_MAP_CLEANER_INTERVAL;
import static org.apache.jena.ext.io.github.galbiston.expiring_map.MapDefaultValues.UNLIMITED_EXPIRY;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ExpiringMapTest {

    public ExpiringMapTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry() throws InterruptedException {
        //System.out.println("expiry");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 5, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);
        instance.put("key5", "value5"); //Should be rejected.
        instance.put("key6", "value6");
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(halfExpiryInterval + cleanerInterval);
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 1;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_none() throws InterruptedException {
        //System.out.println("expiry_none");

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test");

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(1000);
        instance.put("key5", "value5");
        instance.put("key6", "value6");

        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(1000);
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 6;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testRefresh() throws InterruptedException {
        //System.out.println("refresh");

        long expiryInterval = 2000l;
        long halfExpiryInterval = expiryInterval / 2;
        long quarterExpiryInterval = expiryInterval / 3 * 4;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 5, expiryInterval, halfExpiryInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + quarterExpiryInterval);
        instance.put("key1", "value1");
        instance.put("key2", "value2");
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(halfExpiryInterval);
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 2;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testEmpty() throws InterruptedException {
        //System.out.println("empty");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 5, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval);
        instance.put("key1", "value1");
        instance.put("key2", "value2");
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(expiryInterval + cleanerInterval);
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 0;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of setCleanerInterval method, of class ExpiringMap.
     */
    @Test
    public void testSetCleanerInterval() {
        //System.out.println("setCleanerInterval");
        long cleanerInterval = 2000L;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setCleanerInterval(cleanerInterval);
        long expResult = cleanerInterval;
        long result = instance.getCleanerInterval();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of setCleanerInterval method, of class ExpiringMap.
     */
    @Test
    public void testSetCleanerInterval_minimum() {
        //System.out.println("setCleanerInterval_minimum");
        long cleanerInterval = 0L;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setCleanerInterval(cleanerInterval);
        long expResult = MINIMUM_MAP_CLEANER_INTERVAL;
        long result = instance.getCleanerInterval();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of setExpiryInterval method, of class ExpiringMap.
     */
    @Test
    public void testSetExpiryInterval() {
        //System.out.println("setExpiryInterval");
        long expiryInterval = 2000L;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setExpiryInterval(expiryInterval);
        long expResult = expiryInterval;
        long result = instance.getExpiryInterval();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of setExpiryInterval method, of class ExpiringMap.
     */
    @Test
    public void testSetExpiryInterval_minimum() {
        //System.out.println("setExpiryInterval_minimum");
        long expiryInterval = 0L;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setExpiryInterval(expiryInterval);
        long expResult = MAP_CLEANER_INTERVAL + 1;
        long result = instance.getExpiryInterval();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isExpiring method, of class ExpiringMap.
     */
    @Test
    public void testIsExpiring() {
        //System.out.println("isExpiring");
        long expiryInterval = 1000L;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setExpiryInterval(expiryInterval);
        boolean expResult = true;
        boolean result = instance.isExpiring();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isExpiring method, of class ExpiringMap.
     */
    @Test
    public void testIsExpiring_false() {
        //System.out.println("isExpiring_false");
        long expiryInterval = UNLIMITED_EXPIRY;
        ExpiringMap<?,?> instance = new ExpiringMap<>("Test", 5);
        instance.setExpiryInterval(expiryInterval);
        boolean expResult = false;
        boolean result = instance.isExpiring();

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_change_max_size() throws InterruptedException {
        //System.out.println("expiry_change_max_size");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 5, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);
        instance.setMaxSize(10);
        instance.put("key5", "value5");
        instance.put("key6", "value6");
        ////System.out.println("Size Before: " + instance.size());
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 6;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_change_max_size_reduced() throws InterruptedException {
        //System.out.println("expiry_change_max_size_reduced");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 10, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);

        instance.put("key5", "value5");
        instance.put("key6", "value6");
        instance.setMaxSize(5);
        instance.put("key7", "value7"); //Should be rejected.
        ////System.out.println("Size Before: " + instance.size());
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 6;  //Over max size but don't remove until expired.

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_change_expiry_interval() throws InterruptedException {
        //System.out.println("expiry_change_expiry_interval");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 10, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);
        instance.put("key5", "value5");
        instance.put("key6", "value6");
        instance.setExpiryInterval(4000l);
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(halfExpiryInterval + cleanerInterval); //No cleaning should have taken place.
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 6;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_change_expiry_interval_reduced() throws InterruptedException {
        //System.out.println("expiry_change_expiry_interval_reduced");

        long expiryInterval = 4000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 10, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);
        instance.put("key5", "value5");
        instance.put("key6", "value6");
        instance.setExpiryInterval(2000l);
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(halfExpiryInterval + cleanerInterval); //All should have been removed.
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 0;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class ExpiringMap.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testExpiry_set_cleaner() throws InterruptedException {
        //System.out.println("expiry_set_cleaner");

        long expiryInterval = 2000l;
        long cleanerInterval = 1000l;
        long halfExpiryInterval = expiryInterval / 2;

        ExpiringMap<String, String> instance = new ExpiringMap<>("Test", 5, expiryInterval, cleanerInterval);

        instance.put("key1", "value1");
        instance.put("key2", "value2");
        instance.put("key3", "value3");
        instance.put("key4", "value4");
        instance.startExpiry();
        Thread.sleep(halfExpiryInterval + 100);
        instance.setCleanerInterval(500l); //No obvious effect should occur.
        instance.put("key5", "value5"); //Should be rejected.
        instance.put("key6", "value6");
        ////System.out.println("Size Before: " + instance.size());
        Thread.sleep(halfExpiryInterval + cleanerInterval);
        instance.stopExpiry();
        ////System.out.println("Size After: " + instance.size());
        int result = instance.size();
        int expResult = 1;

        ////System.out.println("Exp: " + expResult);
        ////System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

}
