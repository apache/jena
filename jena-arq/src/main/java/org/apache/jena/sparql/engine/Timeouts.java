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

package org.apache.jena.sparql.engine;

import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** Processing timeout strings. */
public class Timeouts {

    private static final long UNSET_AMOUNT       = -1;

    public static Pair<Long, Long> parseTimeoutStr(String str, TimeUnit unit) {
        try {
            if ( str.contains(",") ) {
                String[] a = str.split(",");
                if ( a.length > 2 ) {
                    return null;
                }
                long x1 = Long.parseLong(a[0]);
                x1 = unit.toMillis(x1);
                long x2 = Long.parseLong(a[1]);
                x2 = unit.toMillis(x2);
                return Pair.create(x1, x2);
            } else {
                long x = Long.parseLong(str);
                x = unit.toMillis(x);
                // Overall timeout
                return Pair.create(-1L, x);
            }
        } catch (Exception ex) {
            Log.warn(Timeouts.class, "Failed to parse timeout string: "+str+": "+ex.getMessage());
            return null;
        }
    }

    public static record DurationWithUnit(long amount, TimeUnit unit) {
        public static DurationWithUnit UNSET = new DurationWithUnit(UNSET_AMOUNT, TimeUnit.MILLISECONDS);

        public boolean isSet() {
            return amount >= 0;
        }

        public long asMillis() {
            return (amount < 0) ? amount : unit.toMillis(amount);
        }

        /** Create an instance with normalized values: negative amounts become -1 and a null unit is turned into milliseconds. */
        public static DurationWithUnit of(long amount, TimeUnit unit) {
            return new DurationWithUnit(amount < 0 ? -1 : amount, nullToMillis(unit));
        }
    }

    public static record Timeout(DurationWithUnit initialTimeout, DurationWithUnit overallTimeout) {
        public static Timeout UNSET = new Timeout(UNSET_AMOUNT, UNSET_AMOUNT);

        public Timeout(long initialTimeout, TimeUnit initialTimeoutUnit, long overallTimeout, TimeUnit overallTimeoutUnit) {
            this(DurationWithUnit.of(initialTimeout, initialTimeoutUnit), DurationWithUnit.of(overallTimeout, overallTimeoutUnit));
        }

        public Timeout(long initialTimeout, long overallTimeout) {
            this(initialTimeout, TimeUnit.MILLISECONDS, overallTimeout, TimeUnit.MILLISECONDS);
        }

        public boolean hasInitialTimeout() {
            return initialTimeout().isSet();
        }

        public long initialTimeoutMillis() {
            return initialTimeout().asMillis();
        }

        public boolean hasOverallTimeout() {
            return overallTimeout().isSet();
        }

        public long overallTimeoutMillis() {
            return overallTimeout().asMillis();
        }

        public boolean hasTimeout() {
            return hasInitialTimeout() || hasOverallTimeout();
        }
    }

    // TimeoutBuilder reserved as a possible super-interface for {Query, Update}Exec(ution)Builder.
    public static class TimeoutBuilderImpl {
        protected long     initialTimeout     = UNSET_AMOUNT;
        protected TimeUnit initialTimeoutUnit = null;
        protected long     overallTimeout     = UNSET_AMOUNT;
        protected TimeUnit overallTimeoutUnit = null;

        /** Overwrite this builder's state with that of the argument. */
        public TimeoutBuilderImpl timeout(Timeout timeout) {
            initialTimeout(timeout.initialTimeout().amount(), timeout.initialTimeout().unit());
            overallTimeout(timeout.overallTimeout().amount(), timeout.overallTimeout().unit());
            return this;
        }

        public TimeoutBuilderImpl timeout(long value, TimeUnit timeUnit) {
            initialTimeout(UNSET_AMOUNT, null);
            overallTimeout(value, timeUnit);
            return this;
        }

        public TimeoutBuilderImpl initialTimeout(long value, TimeUnit timeUnit) {
            this.initialTimeout = value < 0 ? UNSET_AMOUNT : value ;
            this.initialTimeoutUnit = timeUnit;
            return this;
        }

        public boolean hasInitialTimeout() {
            return initialTimeout >= 0;
        }

        public TimeoutBuilderImpl overallTimeout(long value, TimeUnit timeUnit) {
            this.overallTimeout = value;
            this.overallTimeoutUnit = timeUnit;
            return this;
        }

        public boolean hasOverallTimeout() {
            return overallTimeout >= 0;
        }

        public Timeout build() {
            return new Timeout(initialTimeout, nullToMillis(initialTimeoutUnit), overallTimeout, nullToMillis(overallTimeoutUnit));
        }
    }

    /** Update any unset timeout in the builder from the specification object. */
    public static void applyDefaultTimeout(TimeoutBuilderImpl builder, Timeout timeout) {
        if (timeout != null) {
            if ( !builder.hasInitialTimeout() )
                builder.initialTimeout(timeout.initialTimeout().amount(), timeout.initialTimeout().unit());
            if ( !builder.hasOverallTimeout() )
                builder.overallTimeout(timeout.overallTimeout().amount(), timeout.overallTimeout().unit());
        }
    }

    public static Timeout extractQueryTimeout(Context cxt) {
        return extractTimeout(cxt, ARQ.queryTimeout);
    }

    public static Timeout extractUpdateTimeout(Context cxt) {
        return extractTimeout(cxt, ARQ.updateTimeout);
    }

    public static Timeout extractTimeout(Context cxt, Symbol symbol) {
        Object obj = cxt.get(symbol);
        return parseTimeout(obj);
    }

    /** Creates a timeout instance from the object. Never returns null. */
    public static Timeout parseTimeout(Object obj) {
        Timeout result = Timeout.UNSET;
        if ( obj != null ) {
            try {
                if ( obj instanceof Timeout to ) {
                    result = to;
                } else if ( obj instanceof Number n ) {
                    long x = n.longValue();
                    result = new Timeout(UNSET_AMOUNT, x);
                } else if ( obj instanceof String str ) {
                    Pair<Long, Long> pair = Timeouts.parseTimeoutStr(str, TimeUnit.MILLISECONDS);
                    if ( pair == null ) {
                        Log.warn(Timeouts.class, "Bad timeout string: "+str);
                        return result;
                    }
                    result = new Timeout(pair.getLeft(), pair.getRight());
                } else
                    Log.warn(Timeouts.class, "Can't interpret timeout: " + obj);
            } catch (Exception ex) {
                Log.warn(Timeouts.class, "Exception setting timeouts (context) from: "+obj, ex);
            }
        }
        return result;
    }

    public static void setQueryTimeout(Context cxt, Timeout timeout) {
        setTimeout(cxt, ARQ.queryTimeout, timeout);
    }

    public static void setUpdateTimeout(Context cxt, Timeout timeout) {
        setTimeout(cxt, ARQ.updateTimeout, timeout);
    }

    public static void setTimeout(Context cxt, Symbol symbol, Timeout timeout) {
        Object obj = toContextValue(timeout);
        cxt.set(symbol, obj);
    }

    /** Inverse function of {@link #parseTimeout(Object)}. */
    public static Object toContextValue(Timeout timeout) {
        Object result = timeout == null
            ? null
            : timeout.hasInitialTimeout()
                ? toString(timeout)
                : timeout.hasOverallTimeout()
                    ? timeout.overallTimeoutMillis()
                    : null;
        return result;
    }

    /** Inverse function of {@link #parseTimeout(Object)}. */
    public static String toString(Timeout timeout) {
       String result = timeout.hasInitialTimeout()
            ? timeout.initialTimeoutMillis() + "," + timeout.overallTimeoutMillis()
            : timeout.hasOverallTimeout()
                ? Long.toString(timeout.overallTimeoutMillis())
                : null;
       return result;
    }

    // Set times from context if not set directly. e..g Context provides default values.
    // Contrast with SPARQLQueryProcessor where the context is limiting values of the protocol parameter.
    public static void applyDefaultQueryTimeoutFromContext(TimeoutBuilderImpl builder, Context cxt) {
        Timeout queryTimeout = extractQueryTimeout(cxt);
        applyDefaultTimeout(builder, queryTimeout);
    }

    /** Returns milliseconds if the given time unit is null. */
    private static TimeUnit nullToMillis(TimeUnit unit) {
        return unit != null ? unit : TimeUnit.MILLISECONDS;
    }
}
