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

package org.apache.jena.sparql.exec.tracker;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.util.Context;

public class DatasetGraphWithExecTracker
    extends DatasetGraphWrapper
    implements DatasetGraphWrapperView
{
    public static DatasetGraph wrap(DatasetGraph dsg) {
        DatasetGraph result;
        if (dsg instanceof DatasetGraphWithExecTracker) {
            result = dsg;
        } else {
            // Put an exec tracker into the dataset's context.
            Context context = dsg.getContext();
            ExecTracker.ensureTracker(context);
            result = new DatasetGraphWithExecTracker(dsg);
        }
        return result;
    }

    protected DatasetGraphWithExecTracker(DatasetGraph dsg) {
        super(dsg);
    }
}
