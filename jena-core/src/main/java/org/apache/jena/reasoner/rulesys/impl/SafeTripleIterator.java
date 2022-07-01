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

package org.apache.jena.reasoner.rulesys.impl;

import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.TriplePattern;
import org.apache.jena.reasoner.rulesys.BackwardRuleInfGraphI;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Wrapper around triple searches using in the back chainer to attempt a restart
 * in the case of a failure such as cross-transaction query.
 */
public class SafeTripleIterator implements ClosableIterator<Triple> {

    protected ExtendedIterator<Triple> matchIterator;
    protected long offset = 0;
    protected TriplePattern goal;
    protected BackwardRuleInfGraphI infGraph;

    public SafeTripleIterator(LPInterpreter interpreter, TriplePattern goal) {
        this.infGraph = interpreter.getEngine().getInfGraph();
        this.goal = goal;
        restart();
    }

    protected void restart() {
        matchIterator = infGraph.findDataMatches(goal);
        for (int i = 0; i < offset; i++) matchIterator.next();
    }

    @Override
    public void close() {
        matchIterator.close();
    }

    @Override
    public boolean hasNext() {
        try {
            return matchIterator.hasNext();
        } catch (Exception e) {
            restart();
            return matchIterator.hasNext();
        }
    }

    @Override
    public Triple next() {
        try {
            offset++;
            return matchIterator.next();
        } catch (Exception e) {
            restart();
            return matchIterator.next();
        }
    }
}
