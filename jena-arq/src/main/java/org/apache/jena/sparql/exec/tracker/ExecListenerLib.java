package org.apache.jena.sparql.exec.tracker;

import java.util.Objects;

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ExecListenerLib {
    // --- ARQ Integration ---

    public static final Symbol symExecListener = SystemARQ.allocSymbol("queryExecListener");

    public static ExecListener getExecListener(Context context) {
        return context.get(symExecListener);
    }

    public static ExecListener requireExecListener(Context context) {
        ExecListener result = getExecListener(context);
        Objects.requireNonNull("No ExecListener registered in context");
        return result;
    }

//    public static ExecTracker ensureExecListener(Context context) {
//        ExecTracker result = context.computeIfAbsent(symExecListener, sym -> new ExecTracker());
//        return result;
//    }
}
