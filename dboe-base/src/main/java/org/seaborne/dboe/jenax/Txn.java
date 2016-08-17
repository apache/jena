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

package org.seaborne.dboe.jenax;

import java.util.function.Supplier ;

import org.apache.jena.sparql.core.Transactional ;

/** Indirection to allow for modifcation for TDB2 capabilities */
public class Txn {
    public static <T extends Transactional> void execRead(T txn, Runnable r) {
        org.apache.jena.system.Txn.execRead(txn, r);
    }

    public static <T extends Transactional, X> X execReadRtn(T txn, Supplier<X> r) {
        return org.apache.jena.system.Txn.execReadRtn(txn, r);
    }

    public static <T extends Transactional> void execWrite(T txn, Runnable r) {
        org.apache.jena.system.Txn.execWrite(txn, r);
    }

    public static <T extends Transactional, X> X execWriteRtn(Transactional txn, Supplier<X> r) {
        return org.apache.jena.system.Txn.execWriteRtn(txn, r);
    }
}
