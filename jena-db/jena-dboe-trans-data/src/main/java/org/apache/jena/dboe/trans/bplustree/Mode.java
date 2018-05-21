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

package org.apache.jena.dboe.trans.bplustree;

/** Tree mode - changing the mode on an existing tree is not supported.
 * The normal mode of operation is {@link Mode#TRANSACTIONAL}  
 */
public enum Mode {
    /** 
     * B+Tree changes are applied in place. MRSW applies.
     */
    MUTABLE,
    /**
     * All operations create new replicated blocks; a replicated block
     * within the operation is not replicated.
     */
    IMMUTABLE,
    /**
     * All changes create new replicated blocks; replicated blocks
     * are re-replicated.  (testing)
     */
    IMMUTABLE_ALL,
    /**
     * As above except the root alone is mutated, hence it is a fixed, known
     * id. (testing)
     */
    MUTABLE_ROOT,
    /**
     * Transactional lifecycle, where blocks below the water marks are
     * immutable.
     */
    TRANSACTIONAL,
    /**
     * Transactional lifecycle, with automatic transactions for update
     * operations outside an explicit transaction.
     */
    TRANSACTIONAL_AUTOCOMMIT
}
