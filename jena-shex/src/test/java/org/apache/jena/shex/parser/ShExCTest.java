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

package org.apache.jena.shex.parser;

import junit.framework.TestCase;
import org.apache.jena.shex.ShexSchema;
import org.apache.jena.shex.expressions.SemAct;

import java.io.StringReader;

import static org.junit.Assert.assertThrows;

public class ShExCTest extends TestCase {
    static String Base = "http://a.example/schema/1";
    static String Lang = "http://a.example/semact/lang";
    static String Text = " a b {\\%}";
    static String Pre = "PREFIX : <http://a.example/semact/lang>";
    static String Prela = "PREFIX la: <http://a.example/semact/la>";

    public void testLangText() {expectText("%<http://a.example/semact/lang>{" + Text + "%}");}
    public void test_Lang_Text() {expectText("% <http://a.example/semact/lang> {" + Text + "%}");}
    public void testFailLangText_() {expectFail("% <http://a.example/semact/lang> {" + Text + "% }");}
    public void testPreText() {expectText(Pre + "%:{" + Text + "%}");}
    public void test_PreText() {expectText(Pre + "% :{" + Text + "%}");}
    public void testPre_Text() {expectText(Pre + "%: {" + Text + "%}");}
    public void test_Pre_Text() {expectText(Pre + "% : {" + Text + "%}");}
    public void testPrelaText() {expectText(Prela + "%la:ng{" + Text + "%}");}
    public void test_PrelaText() {expectText(Prela + "% la:ng{" + Text + "%}");}
    public void testPrela_Text() {expectText(Prela + "%la:ng {" + Text + "%}");}
    public void test_Prela_Text() {expectText(Prela + "% la:ng {" + Text + "%}");}
    public void testFailP_relaText() {expectFail(Prela + "%la :ng{" + Text + "%}");}
    public void testFailPr_elaText() {expectFail(Prela + "%la: ng{" + Text + "%}");}

    private static void expectText(String shexc) {
        ShexSchema shapes = ShExC.parse(new StringReader(shexc), Base);
        SemAct semAct = shapes.getSemActs().get(0);
        assertEquals(semAct.getIri(), Lang);
        assertEquals(semAct.getCode(), Text);
    }

    private static void expectFail(String shexc) {
        Exception exception = assertThrows(ShexParseException.class, () -> {
            ShExC.parse(new StringReader(shexc), Base);
        });
        assertTrue(exception.getMessage().contains(" at line 1, column "));
    }
}
