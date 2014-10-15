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

package org.apache.jena.jdbc.results.metadata.columns;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Column information for short integer columns, note that no XSD types directly
 * correspond to a {@link Types#SMALLINT} so this is not really used but merely
 * provided for completeness.
 * 
 */
public class ShortIntegerColumn extends NumericColumn {

    /**
     * Creates new integer column information
     * 
     * @param label
     *            Label
     * @param nullable
     *            Nullability
     * @param signed
     *            Whether the integer is signed
     * @throws SQLException
     */
    public ShortIntegerColumn(String label, int nullable, boolean signed) throws SQLException {
        super(label, Types.SMALLINT, nullable, Short.class, 0, Short.toString(Short.MAX_VALUE).length(), signed);
    }

}
