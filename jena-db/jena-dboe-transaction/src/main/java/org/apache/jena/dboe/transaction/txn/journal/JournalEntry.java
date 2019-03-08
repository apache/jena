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

package org.apache.jena.dboe.transaction.txn.journal;

import java.nio.ByteBuffer;

import org.apache.jena.atlas.lib.ByteBufferLib;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.ComponentIds;

public class JournalEntry
{
//    static public final JournalEntry Redo       = new JournalEntry(JournalEntryType.REDO);
//    static public final JournalEntry Undo       = new JournalEntry(JournalEntryType.UNDO);

    // Zero payload JournalEntry - create once.
    static public final JournalEntry COMMIT     = new JournalEntry(JournalEntryType.COMMIT, ComponentIds.idSystem);
    static public final JournalEntry ABORT      = new JournalEntry(JournalEntryType.ABORT, ComponentIds.idSystem);

    private long position = -1;                // Location in the Journal (if known).
    private long endPosition = -1;             // End location in the Journal: offset of next entry start.

    private final JournalEntryType type;
    private final ComponentId componentId;
    private final ByteBuffer data;

    private JournalEntry(JournalEntryType type, ComponentId id) {
        this(type, id, null);
    }

    public JournalEntry(JournalEntryType type, ComponentId componentId, ByteBuffer bytes) {
        this.type = type;
        this.componentId = componentId;
        this.data = bytes;
    }

    void setPosition(long posn)             { position = posn; }
    void setEndPosition(long endPosn)       { endPosition = endPosn; }

    public long getPosition()               { return position; }
    long getEndPosition()                   { return endPosition; }

    public JournalEntryType getType()       { return type; }
    public ComponentId getComponentId()     { return componentId; }
    public ByteBuffer getByteBuffer()       { return data; }

    @Override
    public String toString() {
        return "JournalEntry: "+type+" "+componentId;
    }

    static public String format(JournalEntry entry) {
        StringBuilder sbuff = new StringBuilder();

        sbuff.append("Entry: ");
        sbuff.append("  "+entry.type);
        if ( entry.componentId != null ) {
            String label = entry.componentId.label();
            if ( label != null )
                sbuff.append(label);
            sbuff.append(" [..");
            int z = Bytes.getInt(entry.componentId.getBytes(), entry.componentId.getBytes().length-4);
            sbuff.append(Integer.toHexString(z));
            sbuff.append("]");
        }
        if ( entry.data != null )
            sbuff.append("  "+ByteBufferLib.details(entry.data));
        return sbuff.toString();
    }
}
