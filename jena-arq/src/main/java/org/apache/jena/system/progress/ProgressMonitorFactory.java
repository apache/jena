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

package org.apache.jena.system.progress;

public class ProgressMonitorFactory {

    /**
     * Create a progress monitor that sends output via a {@link MonitorOutput}. If this is
     * null, then return a simple {@link ProgressMonitorBasic} that provides the counts
     * and times.
     */
    public static ProgressMonitor progressMonitor(String label, MonitorOutput output, int dataTickPoint, int dataSuperTick) {
        if ( output == null )
            return new ProgressMonitorBasic();
        ProgressMonitor monitor = ProgressMonitorOutput.create(output, label, dataTickPoint, dataSuperTick);
        return monitor;
    }
}
