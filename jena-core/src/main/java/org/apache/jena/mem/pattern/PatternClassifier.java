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

package org.apache.jena.mem.pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Utility class that classifies a triple match into one of the eight
 * {@link MatchPattern} values.
 * <p>
 * The classification is based on which of the subject, predicate and object
 * are <em>concrete</em> (anything that is not a variable / wildcard /
 * {@code null}) and which are wildcards. The result is used by triple-store
 * implementations to dispatch to the most efficient lookup path.
 * <p>
 * All operations are stateless; this class is not meant to be instantiated.
 *
 * @see MatchPattern
 */
public class PatternClassifier {

    private PatternClassifier() {
    }

    /**
     * Classify a triple match.
     *
     * @param tripleMatch the match triple, possibly containing wildcard nodes
     * @return the corresponding {@link MatchPattern}
     */
    public static MatchPattern classify(Triple tripleMatch) {
        if (tripleMatch.getSubject().isConcrete()
                && tripleMatch.getPredicate().isConcrete()
                && tripleMatch.getObject().isConcrete()) {
            return MatchPattern.SUB_PRE_OBJ;
        } else {
            if (tripleMatch.getSubject().isConcrete()) {
                if (tripleMatch.getPredicate().isConcrete()) {
                    return MatchPattern.SUB_PRE_ANY;
                } else {
                    if (tripleMatch.getObject().isConcrete()) {
                        return MatchPattern.SUB_ANY_OBJ;
                    } else {
                        return MatchPattern.SUB_ANY_ANY;
                    }
                }
            } else {
                if (tripleMatch.getPredicate().isConcrete()) {
                    if (tripleMatch.getObject().isConcrete()) {
                        return MatchPattern.ANY_PRE_OBJ;
                    } else {
                        return MatchPattern.ANY_PRE_ANY;
                    }
                } else {
                    if (tripleMatch.getObject().isConcrete()) {
                        return MatchPattern.ANY_ANY_OBJ;
                    } else {
                        return MatchPattern.ANY_ANY_ANY;
                    }
                }
            }
        }
    }

    /**
     * Classify a triple match given as three nodes.
     * Any {@code null} or non-concrete node is treated as a wildcard.
     *
     * @param sm subject node, or {@code null}/wildcard
     * @param pm predicate node, or {@code null}/wildcard
     * @param om object node, or {@code null}/wildcard
     * @return the corresponding {@link MatchPattern}
     */
    public static MatchPattern classify(Node sm, Node pm, Node om) {
        if (null != sm && sm.isConcrete()) {
            if (null != pm && pm.isConcrete()) {
                if (null != om && om.isConcrete()) {
                    return MatchPattern.SUB_PRE_OBJ;
                } else {
                    return MatchPattern.SUB_PRE_ANY;
                }
            } else {
                if (null != om && om.isConcrete()) {
                    return MatchPattern.SUB_ANY_OBJ;
                } else {
                    return MatchPattern.SUB_ANY_ANY;
                }
            }
        } else {
            if (null != pm && pm.isConcrete()) {
                if (null != om && om.isConcrete()) {
                    return MatchPattern.ANY_PRE_OBJ;
                } else {
                    return MatchPattern.ANY_PRE_ANY;
                }
            } else {
                if (null != om && om.isConcrete()) {
                    return MatchPattern.ANY_ANY_OBJ;
                } else {
                    return MatchPattern.ANY_ANY_ANY;
                }
            }
        }
    }
}
