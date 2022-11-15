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

package org.apache.jena.rdfpatch.filelog.rotate;

import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 *  An {@link OutputStream} that implements {@link #close} as call a {@code Consumer<OutputStream>},
 * for example, returns itself to a pool when closed.
 */
class OutputStreamManaged extends FilterOutputStream implements Closeable {

    private Consumer<OutputStream> onClose;

    OutputStreamManaged(OutputStream output, Consumer<OutputStream> onClose) {
        super(output);
        this.onClose = onClose;
   }

    @Override
    public void close() throws IOException {
        onClose.accept(super.out);
    }
}
