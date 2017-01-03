/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.jenax;

import java.util.function.Supplier ;

import org.apache.jena.sparql.core.Transactional ;

/** Indirection to allow for modification for TDB2 capabilities */
public class Txn {
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        org.apache.jena.system.Txn.executeRead(txn, r);
    }

    public static <T extends Transactional, X> X calculateRead(T txn, Supplier<X> r) {
        return org.apache.jena.system.Txn.calculateRead(txn, r);
    }

    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        org.apache.jena.system.Txn.executeWrite(txn, r);
    }

    public static <T extends Transactional, X> X calculateWrite(T txn, Supplier<X> r) {
        return org.apache.jena.system.Txn.calculateWrite(txn, r);
    }
}
