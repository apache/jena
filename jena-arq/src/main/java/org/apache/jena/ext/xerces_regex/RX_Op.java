/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces_regex;

import java.util.Vector;

/**
 * Extracted from Apache Xerces 2.11.0
 *
 * Version Op.java svn:572108 2007-09-02 18:48:31Z mrglavas
 */
class RX_Op {
    static final int DOT = 0;
    static final int CHAR = 1;                  // Single character
    static final int RANGE = 3;                 // [a-zA-Z]
    static final int NRANGE = 4;                // [^a-zA-Z]
    static final int ANCHOR = 5;                // ^ $ ...
    static final int STRING = 6;                // literal String
    static final int CLOSURE = 7;               // X*
    static final int NONGREEDYCLOSURE = 8;      // X*?
    static final int QUESTION = 9;              // X?
    static final int NONGREEDYQUESTION = 10;    // X??
    static final int UNION = 11;                // X|Y
    static final int CAPTURE = 15;              // ( and )
    static final int BACKREFERENCE = 16;        // \1 \2 ...
    static final int LOOKAHEAD = 20;            // (?=...)
    static final int NEGATIVELOOKAHEAD = 21;    // (?!...)
    static final int LOOKBEHIND = 22;           // (?<=...)
    static final int NEGATIVELOOKBEHIND = 23;   // (?<!...)
    static final int INDEPENDENT = 24;          // (?>...)
    static final int MODIFIER = 25;             // (?ims-ims:...)
    static final int CONDITION = 26;            // (?(..)yes|no)

    static int nofinstances = 0;
    static final boolean COUNT = false;

    static RX_Op createDot() {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new RX_Op(RX_Op.DOT);
    }
    static CharOp createChar(int data) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new CharOp(RX_Op.CHAR, data);
    }
    static CharOp createAnchor(int data) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new CharOp(RX_Op.ANCHOR, data);
    }
    static CharOp createCapture(int number, RX_Op next) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        CharOp op = new CharOp(RX_Op.CAPTURE, number);
        op.next = next;
        return op;
    }
    static UnionOp createUnion(int size) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new UnionOp(RX_Op.UNION, size);
    }
    static ChildOp createClosure(int id) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new ModifierOp(RX_Op.CLOSURE, id, -1);
    }
    static ChildOp createNonGreedyClosure() {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new ChildOp(RX_Op.NONGREEDYCLOSURE);
    }
    static ChildOp createQuestion(boolean nongreedy) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new ChildOp(nongreedy ? RX_Op.NONGREEDYQUESTION : RX_Op.QUESTION);
    }
    static RangeOp createRange(RX_Token tok) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new RangeOp(RX_Op.RANGE, tok);
    }
    static ChildOp createLook(int type, RX_Op next, RX_Op branch) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        ChildOp op = new ChildOp(type);
        op.setChild(branch);
        op.next = next;
        return op;
    }
    static CharOp createBackReference(int refno) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new CharOp(RX_Op.BACKREFERENCE, refno);
    }
    static StringOp createString(String literal) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        return new StringOp(RX_Op.STRING, literal);
    }
    static ChildOp createIndependent(RX_Op next, RX_Op branch) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        ChildOp op = new ChildOp(RX_Op.INDEPENDENT);
        op.setChild(branch);
        op.next = next;
        return op;
    }
    static ModifierOp createModifier(RX_Op next, RX_Op branch, int add, int mask) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        ModifierOp op = new ModifierOp(RX_Op.MODIFIER, add, mask);
        op.setChild(branch);
        op.next = next;
        return op;
    }
    static ConditionOp createCondition(RX_Op next, int ref, RX_Op conditionflow, RX_Op yesflow, RX_Op noflow) {
        if (RX_Op.COUNT)  RX_Op.nofinstances ++;
        ConditionOp op = new ConditionOp(RX_Op.CONDITION, ref, conditionflow, yesflow, noflow);
        op.next = next;
        return op;
    }

    final int type;
    RX_Op next = null;

    protected RX_Op(int type) {
        this.type = type;
    }

    int size() {                                // for UNION
        return 0;
    }
    RX_Op elementAt(int index) {                   // for UNIoN
        throw new RuntimeException("Internal Error: type="+this.type);
    }
    RX_Op getChild() {                             // for CLOSURE, QUESTION
        throw new RuntimeException("Internal Error: type="+this.type);
    }
                                                // ModifierOp
    int getData() {                             // CharOp  for CHAR, BACKREFERENCE, CAPTURE, ANCHOR,
        throw new RuntimeException("Internal Error: type="+this.type);
    }
    int getData2() {                            // ModifierOp
        throw new RuntimeException("Internal Error: type="+this.type);
    }
    RangeToken getToken() {                     // RANGE, NRANGE
        throw new RuntimeException("Internal Error: type="+this.type);
    }
    String getString() {                        // STRING
        throw new RuntimeException("Internal Error: type="+this.type);
    }

    // ================================================================
    static class CharOp extends RX_Op {
        final int charData;
        CharOp(int type, int data) {
            super(type);
            this.charData = data;
        }
        @Override
        int getData() {
            return this.charData;
        }
    }

    // ================================================================
    @SuppressWarnings({"unchecked", "rawtypes"})
    static class UnionOp extends RX_Op {
        final Vector branches;
        UnionOp(int type, int size) {
            super(type);
            this.branches = new Vector(size);
        }
        void addElement(RX_Op op) {
            this.branches.addElement(op);
        }
        @Override
        int size() {
            return this.branches.size();
        }
        @Override
        RX_Op elementAt(int index) {
            return (RX_Op)this.branches.elementAt(index);
        }
    }

    // ================================================================
    static class ChildOp extends RX_Op {
        RX_Op child;
        ChildOp(int type) {
            super(type);
        }
        void setChild(RX_Op child) {
            this.child = child;
        }
        @Override
        RX_Op getChild() {
            return this.child;
        }
    }
    // ================================================================
    static class ModifierOp extends ChildOp {
        final int v1;
        final int v2;
        ModifierOp(int type, int v1, int v2) {
            super(type);
            this.v1 = v1;
            this.v2 = v2;
        }
        @Override
        int getData() {
            return this.v1;
        }
        @Override
        int getData2() {
            return this.v2;
        }
    }
    // ================================================================
    static class RangeOp extends RX_Op {
        final RX_Token tok;
        RangeOp(int type, RX_Token tok) {
            super(type);
            this.tok = tok;
        }
        @Override
        RangeToken getToken() {
            return (RangeToken)this.tok;
        }
    }
    // ================================================================
    static class StringOp extends RX_Op {
        final String string;
        StringOp(int type, String literal) {
            super(type);
            this.string = literal;
        }
        @Override
        String getString() {
            return this.string;
        }
    }
    // ================================================================
    static class ConditionOp extends RX_Op {
        final int refNumber;
        final RX_Op condition;
        final RX_Op yes;
        final RX_Op no;
        ConditionOp(int type, int refno, RX_Op conditionflow, RX_Op yesflow, RX_Op noflow) {
            super(type);
            this.refNumber = refno;
            this.condition = conditionflow;
            this.yes = yesflow;
            this.no = noflow;
        }
    }
}
