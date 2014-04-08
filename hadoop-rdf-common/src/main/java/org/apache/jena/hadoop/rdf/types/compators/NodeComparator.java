/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.apache.jena.hadoop.rdf.types.compators;

import java.io.IOException;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

/**
 * A comparator for nodes that provides an efficient binary comparison
 * 
 * @author rvesse
 * 
 */
public class NodeComparator extends WritableComparator {

    private DataInputBuffer buffer = new DataInputBuffer();

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        try {
            // Read info for first node
            buffer.reset(b1, s1, l1);
            int type1 = WritableUtils.readVInt(buffer);
            int length1 = WritableUtils.readVInt(buffer);

            // Read info for second node
            buffer.reset(b2, s2, l2);
            int type2 = WritableUtils.readVInt(buffer);
            int length2 = WritableUtils.readVInt(buffer);

            // Are types different?
            if (type1 < type2) {
                return -1;
            } else if (type1 > type2) {
                return 1;
            }

            // Otherwise compare lexicographically
            int adj1 = WritableUtils.getVIntSize(type1) + WritableUtils.getVIntSize(length1);
            int adj2 = WritableUtils.getVIntSize(type2) + WritableUtils.getVIntSize(length2);
            return WritableComparator.compareBytes(b1, s1 + adj1, l1 - adj1, b2, s2 + adj2, l2 - adj2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
