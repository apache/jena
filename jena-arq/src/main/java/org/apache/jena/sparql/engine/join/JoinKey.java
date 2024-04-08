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

package org.apache.jena.sparql.engine.join;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var ;

/** JoinKey for hash joins */
public final class JoinKey extends ImmutableUniqueList<Var>
{
    /** Key of no variables */
    private static final JoinKey EMPTY = new JoinKey(new Var[0]);

    public static JoinKey empty() {
        return EMPTY;
    }

    /** Make a JoinKey from the intersection of two sets **/
    public static JoinKey create(Collection<Var> vars1, Collection<Var> vars2) {
        // JoinKeys and choices for keys are generally small so short loops are best.
        List<Var> intersection = new ArrayList<>() ;
        for ( Var v : vars1 ) {
            if ( vars2.contains(v) )
                intersection.add(v) ;
        }
        return create(intersection) ;
    }

    /** Make a JoinKey of single variable from the intersection of two sets. **/
    @Deprecated
    public static JoinKey createVarKey(Collection<Var> vars1, Collection<Var> vars2) {
        for ( Var v : vars1 ) {
            if ( vars2.contains(v) )
                return create(v) ;
        }
        return empty() ;
    }

    public static JoinKey create(Var var) {
        return createUnsafe(new Var[] { var });
    }

    /** The builder can emit a key every time build() is called
     * and it can be continued to be used.
     */
    public static final class Builder {

        private ImmutableUniqueList.Builder<Var> delegate;

        Builder() {
            this.delegate = newUniqueListBuilder(Var.class);
        }

        public Builder add(Var var) {
            delegate = delegate.add(var);
            return this ;
        }

        public Builder addAll(Collection<Var> vars) {
            delegate = delegate.addAll(vars);
            return this;
        }

        public Builder addAll(Var[] vars) {
            delegate = delegate.addAll(vars);
            return this;
        }

        public Builder remove(Var var) {
            delegate = delegate.remove(var);
            return this ;
        }

        public Builder clear() {
            delegate = delegate.clear();
            return this ;
        }

        public JoinKey build() {
            JoinKey result;
            // Reuse singleton empty instance when appropriate
            if (delegate.isEmpty()) {
                result = empty();
            } else {
                ImmutableUniqueList<Var> list = delegate.build();
                return new JoinKey(list.elementData);
            }
            return result;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public static JoinKey create(Collection<Var> vars) {
        return vars instanceof Set s
            ? create(s)
            : newBuilder().addAll(vars).build();
    }

    public static JoinKey create(String... varNames) {
        return create(Var.varList(Arrays.asList(varNames)));
    }

    public static JoinKey create(Var[] vars) {
        return newBuilder().addAll(vars).build();
    }

    /** Create a JoinKey directly from a Set.
     *  The set should be a {@link LinkedHashSet} because variable order matters for JoinKeys.
     *  This method does not rely on {@link #newBuilder()}. */
    public static JoinKey create(Set<Var> vars) {
        Var[] arr = new Var[vars.size()];
        arr = vars.toArray(arr);
        return createUnsafe(arr);
    }

    /**
     * Create a join key without coping the key array and without checking for duplicates.
     * The array must not be modified.
     */
    private static JoinKey createUnsafe(Var[] keys) {
        return keys.length == 0 ? empty() : new JoinKey(keys);
    }

    private JoinKey(Var[] keys) {
        super(keys);
    }

    /** Get a single variable for this key.
     *  For any one key, it always returns the same var */
    public Var getVarKey() {
        if ( elementData.length == 0 )
            return null ;
        return elementData[0] ;
    }
}
