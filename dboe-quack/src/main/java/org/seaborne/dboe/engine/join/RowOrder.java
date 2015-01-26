/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.join;

import org.seaborne.dboe.engine.JoinKey ;
import org.seaborne.dboe.engine.Row ;

/** The <code>RowOrder</code> of two rows assigns an ordering
 * based on a {@link JoinKey}.  This is not a stable sort order.
 * If two rows are join compatible, then RowOrder must return zero.
 * Otherwise return negative (-1) for row1 before-by-joinkey row2. 
 * This must be stable for any pair of <code>row1</code> and
 * <code>row2</code> (typically, from two different streams.
 */
public interface RowOrder<X> { int compare(JoinKey joinKey, Row<X> row1, Row<X> row2) ; }