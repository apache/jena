/**
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

package org.apache.jena.query.text.changes;

import org.apache.jena.graph.Node ;

/** Count changes, or simply note if a change has been made.
 */
public class ChangesCounter implements TextDatasetChanges
{
    public long countStart    = 0 ;
    public long countFinish   = 0 ;

    public long countAdd      = 0 ;
    public long countDelete   = 0 ;
    public long countNoAdd    = 0 ;
    public long countNoDelete = 0 ;

    @Override
    public void start() {
        countStart++ ;
    }

    @Override
    public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) {
        if ( qaction == null )
            throw new NullPointerException() ;
        switch (qaction) {
            case ADD:       countAdd++ ; break ;
            case DELETE:    countDelete++ ; break ;
            case NO_ADD:    countNoAdd++ ; break ;
            case NO_DELETE: countNoDelete++ ; break ;
            //default : break ;
        }
    }

    @Override
    public void finish() {
        countFinish++ ;
    }

    public boolean hasChanged() {
        return countAdd > 0 || countDelete > 0 ;
    }

    @Override
    public void reset() {
        countStart    = 0 ;
        countFinish   = 0 ;

        countAdd      = 0 ;
        countDelete   = 0 ;
        countNoAdd    = 0 ;
        countNoDelete = 0 ;
    }
}

