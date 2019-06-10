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

package org.apache.jena.dboe.transaction.txn;

/**
 * Generator of {@link TxnId}s.
 * {@code TxnId} is a identifier for a transaction.
 * A component in a transaction can use it as a unique key.
 * The {@code TxnId}
 * <ul>
 * <li>must be unique across a JVM run
 * <li>unique across JVm runs if used as a persistent name
 * <li>Must provide value equality semantics (two {@code TxnId} are {@code .equals}
 * if
 * </ul>
 * <p>
 * It is preferrable that the TxnId is global unique over time and space.
 */
public interface TxnIdGenerator {
    public TxnId generate();
}

