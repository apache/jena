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

import java.io.PrintStream;
import java.util.Objects;

import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;

public class MonitorOutputs {

    /** Output to the nothing. */
    public static MonitorOutput nullOutput() {
        return (x, y) -> {};
    }

    /** {@link MonitorOutput} to a logger. */
    public static MonitorOutput outputToLog(Logger logger) {
        Objects.requireNonNull(logger);
        return (fmt, args) -> {
            if ( logger.isInfoEnabled() )
                FmtLog.info(logger, fmt, args);
        };
    }

    /** {@link MonitorOutput} to a PrintStream. */
    public static MonitorOutput outputTo(PrintStream output) {
        Objects.requireNonNull(output);
        return (fmt, args) -> {
            if ( fmt.endsWith("\n") || fmt.endsWith("\r") )
                output.print(String.format(fmt, args));
            else
                output.println(String.format(fmt, args));
        };
    }
}
