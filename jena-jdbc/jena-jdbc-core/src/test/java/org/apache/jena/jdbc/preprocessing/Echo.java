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

package org.apache.jena.jdbc.preprocessing;

import java.util.Properties;

import org.apache.jena.query.Query ;
import org.apache.jena.update.UpdateRequest ;

/**
 * A trivial command pre-processor that simply returns the input
 * 
 */
public class Echo implements CommandPreProcessor {

    private Properties props;

    @Override
    public void initialize(Properties props) {
        // No initialization needed
        this.props = props;
    }

    /**
     * Gets the properties that was passed to the
     * {@link #initialize(Properties)} method
     * 
     * @return Properties
     */
    public Properties getProperties() {
        return this.props;
    }

    @Override
    public String preProcessCommandText(String text) {
        return text;
    }

    @Override
    public Query preProcessQuery(Query q) {
        return q;
    }

    @Override
    public UpdateRequest preProcessUpdate(UpdateRequest u) {
        return u;
    }

}
