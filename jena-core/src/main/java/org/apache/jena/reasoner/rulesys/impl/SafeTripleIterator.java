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
