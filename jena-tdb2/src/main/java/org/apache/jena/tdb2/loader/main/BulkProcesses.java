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

package org.apache.jena.tdb2.loader.main;

import java.util.List;
import java.util.ListIterator;

import org.apache.jena.tdb2.loader.BulkLoaderException;
import org.apache.jena.tdb2.loader.base.BulkStartFinish;

public class BulkProcesses {

    public static void start(List<BulkStartFinish> list) {
        list.forEach(x->x.startBulk());
    }

    /**
     * Finish every process, in reverse order, even if one of them fails, so that no process is still running
     * when this returns. The first failure is rethrown, later ones are added to it as suppressed.
     */
    public static void finish(List<BulkStartFinish> list) {
        Throwable failure = null;
        for ( ListIterator<BulkStartFinish> iter = list.listIterator(list.size()) ; iter.hasPrevious() ; ) {
            BulkStartFinish item = iter.previous();
            try {
                item.finishBulk();
            } catch (Throwable th) {
                if ( failure == null )
                    failure = th;
                else if ( th != failure )
                    failure.addSuppressed(th);
            }
        }
        if ( failure != null ) {
            if ( failure instanceof RuntimeException ex )
                throw ex;
            if ( failure instanceof Error err )
                throw err;
            throw new BulkLoaderException(failure);
        }
    }
}
