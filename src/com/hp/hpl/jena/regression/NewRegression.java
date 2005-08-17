/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegression.java,v 1.4 2005-08-17 14:02:16 chris-dollin Exp $
*/

package com.hp.hpl.jena.regression;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.regression.Regression.*;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;

public class NewRegression extends ModelTestBase
    {
    public NewRegression( String name )
        { super( name ); }

    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite( NewRegression.class ); 
        result.addTest( NewRegressionLiterals.suite() );
        result.addTest( NewRegressionResources.suite() );
        result.addTest( NewRegressionStatements.suite() );
        result.addTest( NewRegressionContainers.suite() );
        result.addTest( NewRegressionAddAndContains.suite() );
        result.addTest( NewRegressionGet.suite() );
        result.addTest( NewRegressionObjects.suite() );
        result.addTest( NewRegressionStatements.suite() );
        return result;
        }
    
    public void testNothing()
        {}

    }

//    /** test add and remove sets and models
//     * @param m the model implementation under test
//     */
//    public void test7(Model m1, Model m2) {
//        String  test = "Test7";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                n++; iter = m1.listStatements();
//                n++; m2.add(iter); iter.close();
//                n++; if (! (m1.size() == m2.size())) error(test,n);
//                n++; iter = m1.listStatements();
//                n=110;
//                while (iter.hasNext()) {
//                    n++;    if (! m2.contains(iter.nextStatement())) error(test, n);
//                }
//                n=200;
//                iter = m2.listStatements();
//                while (iter.hasNext()) {
//                    n++;    if (! m1.contains(iter.nextStatement())) error(test,n);
//                }
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//
//            try {
//                n=300;
//                m1.add(m1.createResource(),
//                       RDF.value,
//                       m1.createResource());
//                m1.add(m1.createResource(),
//                       RDF.value,
//                       m1.createResource());
//                m1.add(m1.createResource(),
//                       RDF.value,
//                       m1.createResource());
//                n++; iter = m1.listStatements();
//                n++; m2.remove(iter.nextStatement());
//                n++; m2.remove(iter); iter.close();
//                n++; if (! (m2.size() == 0)) error(test,n);
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//
//            try {
//                n=400;
//                n++; m2.add(m1);
//                n++; if (! (m1.size() == m2.size())) error(test,n);
//                n++; iter = m1.listStatements();
//                n=410;
//                while (iter.hasNext()) {
//                    n++;    if (! m2.contains(iter.nextStatement())) error(test, n);
//                }
//                n=500;
//                iter = m2.listStatements();
//                while (iter.hasNext()) {
//                    n++;    if (! m1.contains(iter.nextStatement())) error(test,n);
//                }
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//
//            try {
//                n=600;
//                // System.err.println( "| m2.size() = " + m2.size() );
//                n++; m2.remove(m1);
//                n++; if (! (m2.size() == 0)) error(test,n);
//                // System.err.println( "| after: m2.size = " + m2.size() );
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test list subjects with methods
//     * @param m the model implementation under test
//     */
//    public void test8(Model m) {
//        String  test = "Test8";
//        int     n = 0;
//        int     num = 5;
//
//        Resource  subject[] = new Resource[num];
//        Property  predicate[] = new Property[num];
//        Vector    stmtv = new Vector();
//        Statement stmts[];
//        Statement stmt;
//
//        String suri = "http://aldabaran/test8/s";
//        String puri = "http://aldabaran/test8/";
//
//        boolean    tvBoolean[] = { false, true };
//        long       tvLong[]    = { 123, 321 };
//        char       tvChar[]    = { '@', ';' };
//        float      tvFloat[]     = { 456.789f, 789.456f };
//        double     tvDouble[]  = { 123.456, 456.123 };
//        String     tvString[]  = { "test8 testing string 1",
//                                   "test8 testing string 2" };
//        String     lang[]      = { "en", "fr" };
//
//        boolean subjf[] = new boolean[num];
//        boolean predf[] = new boolean[num];
//
//        int numObj = 9;
//        boolean objf[] = new boolean[numObj];
//        RDFNode object[] = new RDFNode[numObj];
//
//
//        // System.out.println("Beginning " + test);
//
//        try {
//            Literal     tvLitObj[]  = { m.createLiteral(new LitTestObjF()),
//                                        m.createLiteral(new LitTestObjF()) };
//            Resource    tvResObj[]  = { m.createResource(new ResTestObjF()),
//                                        m.createResource(new ResTestObjF()) };
//
//            for (int i = 0; i<num; i++) {
//                subject[i] = m.createResource(suri + Integer.toString(i));
//                predicate[i] = m.createProperty(puri + Integer.toString(i),
//                "p");
//            }
//
//            for (int i=0; i<num; i++) {
//                m.add(subject[i], predicate[4], false);
//            }
//
//            for (int i=0; i<2; i++) {
//                for (int j=0; j<2; j++) {
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvBoolean[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvLong[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvChar[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvFloat[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvDouble[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j], lang[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvLitObj[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvResObj[j]);
//                    m.add(stmt);
//                }
//            }
//            object[0] = m.createLiteral(tvBoolean[1]);
//            object[1] = m.createLiteral(tvLong[1]);
//            object[2] = m.createLiteral(tvChar[1]);
//            object[3] = m.createLiteral(tvFloat[1]);
//            object[4] = m.createLiteral(tvDouble[1]);
//            object[5] = m.createLiteral(tvString[1]);
//            object[6] = m.createLiteral(tvString[1], lang[1]);
//            object[7] = tvLitObj[1];
//            object[8] = tvResObj[1];
//
//            n = 100;
//
//            n++; stmt = m.getRequiredProperty(subject[1], predicate[1]);
//
//            n++; try {
//                stmt = m.getRequiredProperty(subject[1], RDF.value); error(test,n);
//            } catch (PropertyNotFoundException jx) {
//                // as required
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            boolean found = false;
//
//            ResIterator rIter = m.listSubjectsWithProperty(predicate[4]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, 110);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, 120);
//            }
//            for (int i=0; i<num; i++) {
//                if (! subjf[i]) error(test, 130+i);
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, 150);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, 160);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, 170+i);
//                } else {
//                    if (i<2) error(test, 190+i);
//                }
//            }
//
//            n=200;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvBoolean[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvBoolean[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=300;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (byte)tvLong[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                // System.out.println( "+ " + subj );
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (byte) tvLong[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=400;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (short) tvLong[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (short) tvLong[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=500;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (int) tvLong[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], (int) tvLong[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            // System.out.println( "* -- n := " + n );
//            n=600;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvLong[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvLong[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=700;
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvChar[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvChar[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=800;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvDouble[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvDouble[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=900;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvDouble[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvDouble[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=1000;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvString[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvString[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=1100;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvString[0],
//                                                lang[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvString[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=1200;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvLitObj[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvLitObj[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n=1300;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvResObj[0]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+10);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+20);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) {
//                    if (i>1) error(test, n+30+i);
//                } else {
//                    if (i<2) error(test, n+40+i);
//                }
//            }
//
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            found = false;
//            rIter = m.listSubjectsWithProperty(predicate[0], tvResObj[1]);
//            while (rIter.hasNext()) {
//                Resource subj = rIter.nextResource();
//                found = false;
//                for (int i=0; i<num; i++) {
//                    if (subj.equals(subject[i])) {
//                        found = true;
//                        if (subjf[i]) error(test, n+50);
//                        subjf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<num; i++) {
//                if (subjf[i]) error(test, n+70+i);
//            }
//
//            n = 1400;
//            // System.out.println( "* -- n := " + n );
//            for (int i=0; i<num; i++) {
//                subjf[i] = false;
//            }
//            NodeIterator nIter = m.listObjectsOfProperty(predicate[1]);
//            while (nIter.hasNext()) {
//                RDFNode obj = nIter.nextNode();
//                found = false;
//                for (int i=0; i<numObj; i++) {
//                    if (obj.equals(object[i])) {
//                        found = true;
//                        if (objf[i]) error(test, n+50);
//                        objf[i] = true;
//                    }
//                }
//                if (! found) error(test, n+60);
//            }
//            for (int i=0; i<numObj; i++) {
//                if (!objf[i]) error(test, n+70+i);
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//       // System.out.println("End of " + test);
//    }
//
//    /** test the list statements methods of model
//     * @param m the model implementation under test
//     */
//    public void test9(Model m) {
//        String  test = "Test9";
//        int     n = 0;
//        int     num = 2;
//
//        Resource  subject[] = new Resource[num];
//        Property  predicate[] = new Property[num];
//        Vector    stmtv = new Vector();
//        Statement stmts[];
//        Statement stmt;
//
//        String suri = "http://aldabaran/test9/s";
//        String puri = "http://aldabaran/test9/";
//
//        boolean    tvBoolean[] = { false, true };
//        long       tvLong[]    = { 123, 321 };
//        char       tvChar[]    = { '@', ';' };
//        double     tvDouble[]  = { 123.456, 456.123 };
//        String     tvString[]  = { "test8 testing string 1",
//                                   "test8 testing string 2" };
//        String     lang[]      = { "en", "fr" };
//
////        System.out.println("Beginning " + test);
//
//        try {
//            Literal     tvLitObj[]  = { m.createLiteral(new LitTestObjF()),
//                                        m.createLiteral(new LitTestObjF()) };
//            Resource    tvResObj[]  = { m.createResource(new ResTestObjF()),
//                                        m.createResource(new ResTestObjF()) };
//
//            for (int i = 0; i<num; i++) {
//                subject[i] = m.createResource(suri + Integer.toString(i));
//                predicate[i] = m.createProperty(puri + Integer.toString(i),
//                "p");
//            }
//
//            for (int i=0; i<num; i++) {
//                for (int j=0; j<num; j++) {
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvBoolean[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvLong[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvChar[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvDouble[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j], lang[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvLitObj[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvResObj[j]);
//                    m.add(stmt);
//                }
//            }
//
//            StmtIterator iter;
//            n=100;
//            int count = 0;
//            n++; iter = m.listStatements(
//                                  new SimpleSelector(null, null, (RDFNode)null));
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*num*8)) {
//                error(test,n);
//                System.err.println(count);
//            }
//
//
//
//            class foo extends SimpleSelector
//            {
//                public foo(Resource s, Property p, RDFNode o) {
//                    super(s,p,o);
//                }
//                public boolean selects(Statement s) {return false;}
//            }
//
//            n=110;
//            count = 0;
//            n++; iter = m.listStatements(
//                           new SimpleSelector(subject[0], null, (RDFNode) null));
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getSubject().equals(subject[0])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*8)) error(test,n);
//
//            n=120;
//            count = 0;
//            n++; iter = m.listStatements(
//                           new SimpleSelector(null, predicate[1], (RDFNode) null));
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getPredicate().equals(predicate[1])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*8)) error(test,n);
//
//            n=130;
//            count = 0;
//            n++; iter = m.listStatements(
//                            new SimpleSelector(null, null, tvResObj[1]));
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getObject().equals(tvResObj[1])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==2)) error(test,n);
//
//            n=140;
//            count = 0;
//            n++; iter = m.listStatements(
//                            new SimpleSelector(null, null, false));
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (  stmt.getBoolean()) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==2)) error(test,n);
//
//            n=150;
//            count=0;
//            n++; iter=m.listStatements(
//                          new SimpleSelector(null, null, tvString[1], lang[1]));
//            n++; while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getLanguage().equals(lang[1])) error(test,n);
//                    count++;
//                }
//            n++; iter.close();
//            n++; if (! (count==2)) error(test,n);
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test the query statements methods of model
//     * @param m the model implementation under test
//     */
//    public void test10(Model m) {
//        String  test = "Test10";
//        int     n = 0;
//        int     num = 2;
//
//        Resource  subject[] = new Resource[num];
//        Property  predicate[] = new Property[num];
//        Vector    stmtv = new Vector();
//        Statement stmts[];
//        Statement stmt;
//
//        String suri = "http://aldabaran/test10/s";
//        String puri = "http://aldabaran/test10/";
//
//        boolean    tvBoolean[] = { false, true };
//        long       tvLong[]    = { 123, 321 };
//        char       tvChar[]    = { '@', ';' };
//        double     tvDouble[]  = { 123.456, 456.123 };
//        String     tvString[]  = { "test8 testing string 1",
//                                   "test8 testing string 2" };
//        String     lang[]      = { "en", "fr" };
//
////        System.out.println("Beginning " + test);
//
//        try {
//            Literal     tvLitObj[]  =
//                                     { m.createLiteral(new LitTestObj(1)),
//                                       m.createLiteral(new LitTestObj(2))};
//            Resource    tvResObj[]  = { m.createResource(new ResTestObjF()),
//                                        m.createResource(new ResTestObjF()) };
//
//            for (int i = 0; i<num; i++) {
//                subject[i] = m.createResource(suri + Integer.toString(i));
//                predicate[i] = m.createProperty(puri + Integer.toString(i),
//                "p");
//            }
//
//            for (int i=0; i<num; i++) {
//                for (int j=0; j<num; j++) {
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvBoolean[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvLong[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvChar[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                             tvDouble[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvString[j], lang[i]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvLitObj[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvResObj[j]);
//                    m.add(stmt);
//                    stmt = m.createStatement(subject[i], predicate[j],
//                                            tvResObj[j]);
//                    m.add(stmt);
//                }
//            }
//
//            Model mm;
//            StmtIterator iter;
//            n=100;
//            int count = 0;
//            n++; mm = m.query(new SimpleSelector(null, null, (RDFNode)null));
//            n++; iter = mm.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*num*8)) error(test,n);
//            n++; if (! (mm.size() == count)) error(test,n);
//
//            n=110;
//            count = 0;
//            n++; mm = m.query(
//                           new SimpleSelector(subject[0], null, (RDFNode) null));
//            n++; iter = mm.listStatements();
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getSubject().equals(subject[0])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*8)) error(test,n);
//            n++; if (! (mm.size()==count))error(test,n);
//
//            n=120;
//            count = 0;
//            n++; mm = m.query(
//                        new SimpleSelector(null, predicate[1], (RDFNode) null));
//            n++; iter = mm.listStatements();
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getPredicate().equals(predicate[1])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==num*8)) error(test,n);
//            n++; if (! (mm.size()==count)) error(test,n);
//
//            n=130;
//            count = 0;
//            n++; mm = m.query(new SimpleSelector(null, null, tvResObj[1]));
//            n++; iter = mm.listStatements();
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (! stmt.getObject().equals(tvResObj[1])) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==2)) error(test,n);
//            n++; if (! (mm.size()==count)) error(test,n);
//
//            n=140;
//            count = 0;
//            n++; mm = m.query(new SimpleSelector(null, null, false));
//            n++; iter = mm.listStatements();
//            while (iter.hasNext()) {
//                stmt = iter.nextStatement();
//                if (  stmt.getBoolean()) error(test, n);
//                count++;
//            }
//            n++; iter.close();
//            n++; if (! (count==2)) error(test,n);
//            n++; if (! (mm.size()==count)) error(test,n);
//
//            n=150;
//            n++; mm=m.query(new SimpleSelector(null, null, tvString[1], lang[0]));
//            n++; if (! (mm.size()==1)) error(test,n);
//            n++; iter=mm.listStatements();
//            n++; while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getLanguage().equals(lang[0])) error(test,n);
//                }
//                iter.close();
//
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test model set operations
//     * @param m the model implementation under test
//     */
//    public void test11(Model m1, Model m2) {
//        String  test = "Test11";
//        int     n = 0;
//        Statement stmt;
//
//        Model um = null;
//        Model im = null;
//        Model dm = null;
//
//        if (! (   m1.supportsSetOperations()          // jjc
//               && m2.supportsSetOperations() ) )      // jjc
//            return;                                   // jjc
//
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                m2.add(m2.createResource(new ResTestObjF()), RDF.value, 1);
//                if (m1.containsAll(m2)) error(test,n);
//                n++; um = m1.union(m2);
//                n++; iter = um.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! (m1.contains(stmt) || m2.contains(stmt))) {
//                        System.out.println(stmt.toString());
//                        error(test,n);
//                    }
//                }
//                n++; iter.close();
//                n++; iter = m1.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! um.contains(stmt)) error(test,n);
//                }
//                n++; iter.close();
//                n++; iter = m2.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! um.contains(stmt)) error(test,n);
//                }
//                n++; iter.close();
//
//                n++; if (!um.containsAll(m1)) error(test,n);
//                n++; if (!um.containsAll(m2)) error(test,n);
//                n++; iter = m1.listStatements();
//                n++; if (!um.containsAll(iter)) error(test,n);
//                     iter.close();
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//
//            try {
//                n=200;
//                im= um.intersection(m1);
//                n++; iter = im.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! (um.contains(stmt) && m1.contains(stmt)))
//                        error(test,n);
//                }
//                n++; iter.close();
//                n++; iter = um.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (m1.contains(stmt)) {
//                        if (! im.contains(stmt)) error(test,n);
//                    }
//                }
//                n++; iter.close();
//                n++; iter = m1.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (m1.contains(stmt)) {
//                        if (! im.contains(stmt)) error(test,n);
//                    }
//                }
//                n++; iter.close();
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//
//            try {
//                n=300;
//                dm = um.difference(m2);
//                n++; iter = dm.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! (um.contains(stmt) && !(m2.contains(stmt))))
//                        error(test,n);
//                }
//                n++; iter.close();
//                n++; iter = um.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (m2.contains(stmt)) {
//                        if (  dm.contains(stmt)) error(test,n);
//                    } else {
//                        if (! dm.contains(stmt)) error(test, 1000+n);
//                    }
//                }
//                n++; iter.close();
//                n++; iter = m2.listStatements();
//                while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (  dm.contains(stmt)) error(test,n);
//                }
//                n++; iter.close();
//                n++; if (dm.containsAny(m2)) error(test,n);
//                n++; iter = m2.listStatements();
//                n++; if (dm.containsAny(iter)) error(test,n);
//                n++; iter.close();
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test Resource methods
//     * @param m the model implementation under test
//     */
//    public void test12(Model m) {
//        String  test = "Test12";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//            Resource r = m.createResource();
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal tvLiteral = m.createLiteral("test 12 string 2");
//            Resource tvResource = m.createResource();
//            String     lang     = "en";
//            Statement stmt;
//
//            n = 100;
//            n++; if (! r.addProperty(RDF.value, tvByte)
//                        .hasProperty(RDF.value, tvByte)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvShort)
//                        .hasProperty(RDF.value, tvShort)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvInt)
//                        .hasProperty(RDF.value, tvInt)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvLong)
//                        .hasProperty(RDF.value, tvLong)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvChar)
//                        .hasProperty(RDF.value, tvChar)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvFloat)
//                        .hasProperty(RDF.value, tvFloat)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvDouble)
//                        .hasProperty(RDF.value, tvDouble)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvString)
//                        .hasProperty(RDF.value, tvString)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvString, lang)
//                        .hasProperty(RDF.value, tvString, lang)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvObject)
//                        .hasProperty(RDF.value, tvObject)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvLiteral)
//                        .hasProperty(RDF.value, tvLiteral)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvResource)
//                        .hasProperty(RDF.value, tvResource)) error(test, n);
//            n++; if (! r.getRequiredProperty(RDF.value).getSubject().equals(r))
//                       error(test,n);
//            n++; try {
//                     r.getRequiredProperty(RDF.type); error(test, n);
//                } catch (PropertyNotFoundException e) { // as expected
//                }
//            n++; iter = r.listProperties(RDF.value);
//                 int count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != 12) error(test,n);
//           n++; iter = r.listProperties(RDF.type);
//                 count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != 0) error(test,n);
//           n++; iter = r.listProperties();
//                 count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != 12) error(test,n);
//
//           n++; r.removeProperties();
//           n++; Model mm = m.query(new SimpleSelector(r, null, (RDFNode) null));
//                if (! (mm.size()==0)) error(test,n);
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//
//    /** Test Statement methods
//     * @param m the model implementation under test
//     */
//    public void test13(Model m) {
//        String  test = "Test13";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//            Resource r = m.createResource();
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            Resource tvResource = m.createResource();
//            String     lang     = "fr";
//            Statement stmt;
//
//            n=100;
//            n++; if (! m.createStatement(r, RDF.value, r)
//                        .getResource()
//                        .equals(r)) error(test,n);
//            n++; try {
//                       m.createStatement(r, RDF.value, false)
//                        .getResource();
//                       error(test,n);
//                } catch(ResourceRequiredException e) {
//                    // as required
//                }
//            n++; if (! m.createStatement(r, RDF.value, true)
//                        .getLiteral()
//                        .getBoolean()) error(test,n);
//            n++; try {
//                       m.createStatement(r, RDF.value, r)
//                        .getLiteral();
//                       error(test,n);
//                } catch(LiteralRequiredException e) {
//                    // as required
//                }
//            n = 200;
//            n++; if (! m.createStatement(r, RDF.value, true)
//                        .getBoolean()) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvByte)
//                         .getByte()==tvByte)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvShort)
//                         .getShort()==tvShort)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvInt)
//                         .getInt()==tvInt)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvLong)
//                         .getLong()==tvLong)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvChar)
//                         .getChar()==tvChar)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvFloat)
//                         .getFloat()==tvFloat)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvDouble)
//                         .getDouble()==tvDouble)) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvString)
//                         .getString().equals(tvString))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvString, lang)
//                         .getString().equals(tvString))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvString,lang)
//                         .getLanguage().equals(lang))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvResObj)
//                         .getResource(new ResTestObjF())
//                         .equals(tvResObj))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvLitObj)
//                         .getObject(new LitTestObjF())
//                         .equals(tvLitObj))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvBag)
//                         .getBag().equals(tvBag))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvAlt)
//                         .getAlt().equals(tvAlt))) error(test,n);
//            n++; if (! (m.createStatement(r, RDF.value, tvSeq)
//                         .getSeq().equals(tvSeq))) error(test,n);
//            n=300;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(!tvBoolean);
//            n++;  if (! (stmt.getBoolean() == !tvBoolean)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, !tvBoolean))
//                       error(test,n);
//
//            n=310;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvByte);
//            n++;  if (! (stmt.getByte() == tvByte)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvByte))
//                       error(test,n);
//
//            n= 320;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvShort);
//            n++;  if (! (stmt.getShort() == tvShort)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvShort))
//                       error(test,n);
//
//            n=330;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvInt);
//            n++;  if (! (stmt.getInt() == tvInt)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvInt))
//                       error(test,n);
//
//            n=340;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvLong);
//            n++;  if (! (stmt.getLong() == tvLong)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvLong))
//                       error(test,n);
//
//            n=350;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvChar);
//            n++;  if (! (stmt.getChar() == tvChar)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvChar))
//                       error(test,n);
//
//            n=360;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvFloat);
//            n++;  if (! ((stmt.getFloat()-tvFloat)<0.00005)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvFloat))
//                       error(test,n);
//
//            n=370;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvDouble);
//            n++;  if (! ((stmt.getDouble()-tvDouble)<0.0005)) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvDouble))
//                       error(test,n);
//
//            n=380;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvString);
//            n++;  if (! (stmt.getString().equals(tvString))) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvString))
//                       error(test,n);
//            n++; stmt = stmt.changeObject(tvString, lang);
//            n++;  if (! (stmt.getString().equals(tvString))) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvString))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvString, lang))
//                       error(test,n);
//
//            n=390;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvResObj);
//            n++;  if (! (stmt.getResource().equals(tvResObj))) error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvResObj))
//                       error(test,n);
//
//            n=400;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; stmt = stmt.changeObject(tvLitObj);
//            n++;  if (! (stmt.getObject(new LitTestObjF()).equals(tvLitObj)))
//                      error(test,n);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//            n++;  if (! m.contains(stmt.getSubject(), RDF.value, tvLitObj))
//                       error(test,n);
//
//            n=500;
//            n++; stmt = m.createStatement(m.createResource(),
//                                          RDF.value, tvBoolean);
//            n++; m.add(stmt);
//            n++; m.remove(stmt);
//            n++;  if (  m.contains(stmt.getSubject(), RDF.value, tvBoolean))
//                        error(test,n);
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test bag
//     * @param m the model implementation under test
//     */
//    public void test14(Model m) {
//        String  test = "Test14";
//        int     n = 0;
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            int        num=10;
//            Statement stmt;
//
//            n=100;
//            n++; Bag bag = m.createBag();
//            n++; if (! m.contains(bag, RDF.type, RDF.Bag)) error(test,n);
//            n++; if (! (bag.size() == 0)) error(test,n);
//
//            n=200;
//            n++; bag.add(tvBoolean);
//            n++; if (! bag.contains(tvBoolean)) error(test, n);
//            n++; bag.add(tvByte);
//            n++; if (! bag.contains(tvByte)) error(test, n);
//            n++; bag.add(tvShort);
//            n++; if (! bag.contains(tvShort)) error(test, n);
//            n++; bag.add(tvInt);
//            n++; if (! bag.contains(tvInt)) error(test, n);
//            n++; bag.add(tvLong);
//            n++; if (! bag.contains(tvLong)) error(test, n);
//            n++; bag.add(tvChar);
//            n++; if (! bag.contains(tvChar)) error(test, n);
//            n++; bag.add(tvFloat);
//            n++; if (! bag.contains(tvFloat)) error(test, n);
//            n++; bag.add(tvDouble);
//            n++; if (! bag.contains(tvDouble)) error(test, n);
//            n++; bag.add(tvString);
//            n++; if (! bag.contains(tvString)) error(test, n);
//            n++; bag.add(tvLiteral);
//            n++; if (! bag.contains(tvLiteral)) error(test, n);
//            n++; bag.add(tvResObj);
//            n++; if (! bag.contains(tvResObj)) error(test, n);
//            n++; bag.add(tvLitObj);
//            n++; if (! bag.contains(tvLitObj)) error(test, n);
//            n++; if (! (bag.size()==12)) error(test,n);
//
//            {
//                n=300;
//                n++; bag = m.createBag();
//                     for (int i=0; i<num; i++) {
//                        bag.add(i);
//                    }
//                n++; if (! (bag.size()==num)) error(test,n);
//                n++; nIter = bag.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=400;
//                n++; nIter=bag.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=450;
//                n++; nIter = bag.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=480;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=500;
//                n++; bag = m.createBag();
//                     for (int i=0; i<num; i++) {
//                        bag.add(i);
//                    }
//                n++; nIter=bag.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=550;
//                n++; nIter = bag.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=580;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//                n=600;
//                n++; bag = m.createBag();
//                     for (int i=0; i<num; i++) {
//                        bag.add(i);
//                    }
//                n++; nIter=bag.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=650;
//                n++; nIter = bag.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=680;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test Alt
//     * @param m the model implementation under test
//     */
//    public void test15(Model m) {
//        String  test = "Test15";
//        int     n = 0;
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            int        num=10;
//            Statement stmt;
//
//            n=100;
//            n++; Alt alt = m.createAlt();
//            n++; if (! m.contains(alt, RDF.type, RDF.Alt)) error(test,n);
//            n++; if (! (alt.size() == 0)) error(test,n);
//
//            n=200;
//            n++; alt.add(tvBoolean);
//            n++; if (! alt.contains(tvBoolean)) error(test, n);
//            n++; alt.add(tvByte);
//            n++; if (! alt.contains(tvByte)) error(test, n);
//            n++; alt.add(tvShort);
//            n++; if (! alt.contains(tvShort)) error(test, n);
//            n++; alt.add(tvInt);
//            n++; if (! alt.contains(tvInt)) error(test, n);
//            n++; alt.add(tvLong);
//            n++; if (! alt.contains(tvLong)) error(test, n);
//            n++; alt.add(tvChar);
//            n++; if (! alt.contains(tvChar)) error(test, n);
//            n++; alt.add(tvFloat);
//            n++; if (! alt.contains(tvFloat)) error(test, n);
//            n++; alt.add(tvDouble);
//            n++; if (! alt.contains(tvDouble)) error(test, n);
//            n++; alt.add(tvString);
//            n++; if (! alt.contains(tvString)) error(test, n);
//            n++; alt.add(tvLiteral);
//            n++; if (! alt.contains(tvLiteral)) error(test, n);
//            n++; alt.add(tvResObj);
//            n++; if (! alt.contains(tvResObj)) error(test, n);
//            n++; alt.add(tvLitObj);
//            n++; if (! alt.contains(tvLitObj)) error(test, n);
//            n++; if (! (alt.size()==12)) error(test,n);
//
//            {
//                n=300;
//                n++; alt = m.createAlt();
//                     for (int i=0; i<num; i++) {
//                        alt.add(i);
//                    }
//                n++; if (! (alt.size()==num)) error(test,n);
//                n++; nIter = alt.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=400;
//                n++; nIter=alt.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=450;
//                n++; nIter = alt.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=480;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=500;
//                n++; alt = m.createAlt();
//                     for (int i=0; i<num; i++) {
//                        alt.add(i);
//                    }
//                n++; nIter=alt.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=550;
//                n++; nIter = alt.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=580;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//                n=600;
//                n++; alt = m.createAlt();
//                     for (int i=0; i<num; i++) {
//                        alt.add(i);
//                    }
//                n++; nIter=alt.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=650;
//                n++; nIter = alt.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=680;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=700;
//                n++; alt = m.createAlt();
//                n++; if (! (alt.setDefault(tvLiteral)
//                               .getDefault().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (! (alt.setDefault(tvLiteral)
//                               .getDefaultLiteral().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvResource)
//                               .getDefaultResource().equals(tvResource))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvByte)
//                               .getDefaultByte()== tvByte))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvShort)
//                               .getDefaultShort()==tvShort))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvInt)
//                               .getDefaultInt()==tvInt))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvLong)
//                               .getDefaultLong()==tvLong))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvChar)
//                               .getDefaultChar()==tvChar))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvFloat)
//                               .getDefaultFloat()==tvFloat))
//                       error(test,n);
//                n++; if (!  (alt.setDefault(tvDouble)
//                               .getDefaultDouble()==tvDouble))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvString)
//                               .getDefaultString().equals(tvString))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvResObj)
//                               .getDefaultResource(new ResTestObjF())
//                               .equals(tvResObj))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvLitObj)
//                               .getDefaultObject(new LitTestObjF())
//                               .equals(tvLitObj))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvAlt)
//                               .getDefaultAlt()
//                               .equals(tvAlt))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvBag)
//                               .getDefaultBag()
//                               .equals(tvBag))
//                       error(test,n);
//                n++; if (!  alt.setDefault(tvSeq)
//                               .getDefaultSeq()
//                               .equals(tvSeq))
//                       error(test,n);
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test Seq
//     * @param m the model implementation under test
//     */
//    public void test16(Model m) {
//        String  test = "Test16";
//        int     n = 0;
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            int        num=10;
//            Statement stmt;
//
//            n=100;
//            n++; Seq seq = m.createSeq();
//            n++; if (! m.contains(seq, RDF.type, RDF.Seq)) error(test,n);
//            n++; if (! (seq.size() == 0)) error(test,n);
//
//            n=200;
//            n++; seq.add(tvBoolean);
//            n++; if (! seq.contains(tvBoolean)) error(test, n);
//            n++; seq.add(tvByte);
//            n++; if (! seq.contains(tvByte)) error(test, n);
//            n++; seq.add(tvShort);
//            n++; if (! seq.contains(tvShort)) error(test, n);
//            n++; seq.add(tvInt);
//            n++; if (! seq.contains(tvInt)) error(test, n);
//            n++; seq.add(tvLong);
//            n++; if (! seq.contains(tvLong)) error(test, n);
//            n++; seq.add(tvChar);
//            n++; if (! seq.contains(tvChar)) error(test, n);
//            n++; seq.add(tvFloat);
//            n++; if (! seq.contains(tvFloat)) error(test, n);
//            n++; seq.add(tvDouble);
//            n++; if (! seq.contains(tvDouble)) error(test, n);
//            n++; seq.add(tvString);
//            n++; if (! seq.contains(tvString)) error(test, n);
//            n++; seq.add(tvLiteral);
//            n++; if (! seq.contains(tvLiteral)) error(test, n);
//            n++; seq.add(tvResObj);
//            n++; if (! seq.contains(tvResObj)) error(test, n);
//            n++; seq.add(tvLitObj);
//            n++; if (! seq.contains(tvLitObj)) error(test, n);
//            n++; if (! (seq.size()==12)) error(test,n);
//
//            {
//                n=300;
//                n++; seq = m.createSeq();
//                     for (int i=0; i<num; i++) {
//                        seq.add(i);
//                    }
//                n++; if (! (seq.size()==num)) error(test,n);
//                n++; nIter = seq.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=400;
//                n++; nIter=seq.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=450;
//                n++; nIter = seq.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=480;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=500;
//                n++; seq = m.createSeq();
//                     for (int i=0; i<num; i++) {
//                        seq.add(i);
//                    }
//                n++; nIter=seq.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=550;
//                n++; nIter = seq.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=580;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//                n=600;
//                n++; seq = m.createSeq();
//                     for (int i=0; i<num; i++) {
//                        seq.add(i);
//                    }
//                n++; nIter=seq.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=650;
//                n++; nIter = seq.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=680;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=700;
//                seq = m.createSeq();
//                n++; seq.add(tvBoolean);
//                n++; if (!  (seq.getBoolean(1)==tvBoolean)) error(test,n);
//                n++; seq.add(tvByte);
//                n++; if (!  (seq.getByte(2)==tvByte)) error(test,n);
//                n++; seq.add(tvShort);
//                n++; if (!  (seq.getShort(3)==tvShort)) error(test,n);
//                n++; seq.add(tvInt);
//                n++; if (!  (seq.getInt(4)==tvInt)) error(test,n);
//                n++; seq.add(tvLong);
//                n++; if (!  (seq.getLong(5)==tvLong)) error(test,n);
//                n++; seq.add(tvChar);
//                n++; if (!  (seq.getChar(6)==tvChar)) error(test,n);
//                n++; seq.add(tvFloat);
//                n++; if (!  (seq.getFloat(7)==tvFloat)) error(test,n);
//                n++; seq.add(tvDouble);
//                n++; if (!  (seq.getDouble(8)==tvDouble)) error(test,n);
//                n++; seq.add(tvString);
//                n++; if (!  (seq.getString(9).equals(tvString))) error(test,n);
//                n++; seq.add(tvLitObj);
//                n++; if (!  (seq.getObject(10, new LitTestObjF())
//                                .equals(tvLitObj))) error(test,n);
//                n++; seq.add(tvResource);
//                n++; if (!  (seq.getResource(11).equals(tvResource))) error(test,n);
//                n++; seq.add(tvLiteral);
//                n++; if (!  (seq.getLiteral(12).equals(tvLiteral))) error(test,n);
//                n++; seq.add(tvResObj);
//                n++; if (!  (seq.getResource(13, new ResTestObjF())
//                                .equals(tvResObj))) error(test,n);
//                n++; seq.add(tvBag);
//                n++; if (!  (seq.getBag(14).equals(tvBag))) error(test,n);
//                n++; seq.add(tvAlt);
//                n++; if (!  (seq.getAlt(15).equals(tvAlt))) error(test,n);
//                n++; seq.add(tvSeq);
//                n++; if (!  (seq.getSeq(16).equals(tvSeq))) error(test,n);
//                n++; try {
//                        seq.getInt(17); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//                n++; try {
//                        seq.getInt(0); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//            }
//
//            {
//                n=800;
//                seq = m.createSeq();
//                for (int i=0; i<num; i++) {
//                    seq.add(i);
//                }
//
//                     try {
//                n++;        seq.add(0, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//                     seq.add(num+1, false);
//                     if (seq.size() != num+1) error(test,n);
//                     seq.remove(num+1);
//                     try {
//                n++;        seq.add(num+2, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//
//               n=820;
//                    int size = seq.size();
//                    for (int i=1; i<=num-1; i++) {
//               n++;     seq.add(i, 1000+i);
//               n++;     if (! (seq.getInt(i)==1000+i)) error(test,n);
//               n++;     if (! (seq.getInt(i+1)==0)) error(test, n);
//               n++;     if (! (seq.size()==(size+i))) error(test,n);
//               n++;     if (! (seq.getInt(size)==(num-i-1))) error(test,n);
//                    }
//               n=900;
//                    seq = m.createSeq();
//                    seq.add(m.createResource());
//                    seq.add(1, tvBoolean);
//               n++; if (! (seq.getBoolean(1)==tvBoolean)) error(test,n);
//                    seq.add(1, tvByte);
//               n++; if (! (seq.getByte(1)==tvByte)) error(test,n);
//                    seq.add(1, tvShort);
//               n++; if (! (seq.getShort(1)==tvShort)) error(test,n);
//                    seq.add(1, tvInt);
//               n++; if (! (seq.getInt(1)==tvInt)) error(test,n);
//                    seq.add(1, tvLong);
//               n++; if (! (seq.getLong(1)==tvLong)) error(test,n);
//                    seq.add(1, tvChar);
//               n++; if (! (seq.getChar(1)==tvChar)) error(test,n);
//                    seq.add(1, tvFloat);
//               n++; if (! (seq.getFloat(1)==tvFloat)) error(test,n);
//                    seq.add(1, tvDouble);
//               n++; if (! (seq.getDouble(1)==tvDouble)) error(test,n);
//                    seq.add(1, tvString);
//               n++; if (! (seq.getString(1).equals(tvString))) error(test,n);
//                    seq.add(1, tvResource);
//               n++; if (! (seq.getResource(1).equals(tvResource))) error(test,n);
//                    seq.add(1, tvLiteral);
//               n++; if (! (seq.getLiteral(1).equals(tvLiteral))) error(test,n);
//                    seq.add(1, tvLitObj);
//               n++; if (! (seq.getObject(1, new LitTestObjF())
//                              .equals(tvLitObj))) error(test,n);
//
//               n=1000;
//               n++; if (! (seq.indexOf(tvLitObj)==1)) error(test,n);
//               n++; if (! (seq.indexOf(tvLiteral)==2)) error(test,n);
//               n++; if (! (seq.indexOf(tvResource)==3)) error(test,n);
//               n++; if (! (seq.indexOf(tvString)==4)) error(test,n);
//               n++; if (! (seq.indexOf(tvDouble)==5)) error(test,n);
//               n++; if (! (seq.indexOf(tvFloat)==6)) error(test,n);
//               n++; if (! (seq.indexOf(tvChar)==7)) error(test,n);
//               n++; if (! (seq.indexOf(tvLong)==8)) error(test,n);
//               n++; if (! (seq.indexOf(tvInt)==9)) error(test,n);
//               n++; if (! (seq.indexOf(tvShort)==10)) error(test,n);
//               n++; if (! (seq.indexOf(tvByte)==11)) error(test,n);
//               n++; if (! (seq.indexOf(tvBoolean)==12)) error(test,n);
//               n++; if (! (seq.indexOf(1234543)==0)) error(test,n);
//
//               n=1100;
//                   seq = m.createSeq();
//                   for (int i=0; i<num; i++) {
//                       seq.add(i);
//                   }
//              n=1110;
//                   seq.set(5, tvBoolean);
//              n++; if (! (seq.getBoolean(5)==tvBoolean)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1120;
//                   seq.set(5, tvByte);
//              n++; if (! (seq.getByte(5)==tvByte)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1130;
//                   seq.set(5, tvShort);
//              n++; if (! (seq.getShort(5)==tvShort)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1140;
//                   seq.set(5, tvInt);
//              n++; if (! (seq.getInt(5)==tvInt)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1150;
//                   seq.set(5, tvLong);
//              n++; if (! (seq.getLong(5)==tvLong)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1160;
//                   seq.set(5, tvChar);
//              n++; if (! (seq.getChar(5)==tvChar)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1170;
//                   seq.set(5, tvFloat);
//              n++; if (! (seq.getFloat(5)==tvFloat)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1180;
//                   seq.set(5, tvDouble);
//              n++; if (! (seq.getDouble(5)==tvDouble)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1190;
//                   seq.set(5, tvLiteral);
//              n++; if (! (seq.getLiteral(5).equals(tvLiteral))) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1200;
//                   seq.set(5, tvResource);
//              n++; if (! (seq.getResource(5).equals(tvResource))) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1210;
//                   seq.set(5, tvLitObj);
//              n++; if (! (seq.getObject(5, new LitTestObjF()))
//                             .equals(tvLitObj)) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//              n=1220;
//                   seq.set(5, tvResObj);
//              n++; if (! (seq.getResource(5, new ResTestObjF())
//                             .equals(tvResObj))) error(test,n);
//              n++; if (! (seq.getInt(4)==3)) error(test,n);
//              n++; if (! (seq.getInt(6)==5)) error(test,n);
//              n++; if (! (seq.size()==num)) error(test,n);
//        }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test enhanced resources
//     * @param m the model implementation under test
//     */
//    public void test17(Model m) {
//        String  test = "Test17";
//        int     n = 0;
//
//        try {
//            // System.out.println("Beginning " + test);
//            Resource r = new ResourceImpl((ModelCom)m);
//            n=1000; testResource(m, r, test, n, 0);
//
//
//            n=2000; testResource(m, m.createBag(), test, n, 1);
//            n=3000; testContainer(m, m.createBag(), m.createBag(), test, n);
//            n=4000; testBag(m, m.createBag(), m.createBag(), m.createBag(),
//                             test, n);
//
//            n=5000; testResource(m, m.createAlt(), test, n, 1);
//            n=6000; testContainer(m, m.createAlt(), m.createAlt(), test, n);
//            n=7000; testAlt(m, m.createAlt(), m.createAlt(),
//                              m.createAlt(), m.createAlt(), test, n);
//
//
//            n=8000; testResource(m, m.createSeq(), test, n, 1);
//            n=9000; testContainer(m, m.createSeq(), m.createSeq(), test, n);
//            n=10000; testSeq(m, m.createSeq(), m.createSeq(), m.createSeq(),
//                                m.createSeq(), m.createSeq(), m.createSeq(),
//                                m.createSeq(), test, n);
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test load from xml file
//     * @param m the model implementation under test
//     */
//    public void test18(Model m) {
//        String  test = "Test18";
//        if (test.equals( test )) return;
//        String  testURI = "http://aldabaran.hpl.hp.com/rdftest/test18/";
//        String  subject1 = testURI + "1";
//        String  object1 =
//   "<foo bar=\"bar\"><bar>abc<foobar/>def&lt;&gt;&apos;&quot;&amp;</bar></foo>";
//        String RDFSchemaURI = "http://lists.w3.org/Archives/Public/www-archive/"
//                            + "2001Sep/att-0064/00-rdfschema.rdf";
//        int     n = 0;
//
//        try {
//            System.out.println("Beginning " + test);
//            m.read(ResourceReader.getInputStream("modules/rdf/rdfschema.html"),
//                                      RDFSchemaURI);
//            n++; if (m.size() != 124) error(test, n);
//   //         n++; m.write(new PrintWriter(System.out));
//
//            StmtIterator iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            m.read(ResourceReader.getInputStream("modules/rdf/embeddedxml.xml"), "");
//            n++;
// /* I'd like to test for the exactly correct value here, but can't since the
//  * exactly correct value is not defined.
//            if (! m.contains(m.createResource(subject1),
//                             RDF.value, object1)) error(test, n++);
//  * So instead lets do some rough checks its right */
//            String xml = m.getResource(subject1)
//                          .getRequiredProperty(RDF.value)
//                          .getString();
//            n++; if ( xml.indexOf("&lt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&gt;") == -1) error(test, n);
//            n++; if ( xml.indexOf("&amp;") == -1) error(test, n);
//            n++; if ((xml.indexOf("'bar'") == -1) &&
//                     (xml.indexOf("\"bar\"") == -1)) error(test, n);
//
//            m.createResource()
//             .addProperty(RDF.value, "can't loose");
//  //          m.write(new PrintWriter(System.out));
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//            n++;
//            m.read(ResourceReader.getInputStream("modules/rdf/testcollection.rdf"), "");
//            if (m.size() != 24) error(test, (int) m.size());
//
//            iter = m.listStatements();
//            while (iter.hasNext()) {
//                iter.nextStatement();
//                iter.remove();
//            }
//
//            try {
//                m.read(System.getProperty("com.hp.hpl.jena.regression.testURL",
//                                          RDFSchemaURI));
// //               n++; m.write(new PrintWriter(System.out));
//                n++; if ((m.size() != 124) && (m.size() != 125)) {
//                    System.out.println("size = " + m.size());
//                      error(test, n);
//                }
//                if (! m.contains(RDF.Property, RDF.type, RDFS.Class))
//                    error(test, n);
//            } catch (JenaException rdfx) {
//                Throwable th = rdfx.getCause();
//                if ( th instanceof NoRouteToHostException
//                 || th instanceof UnknownHostException
//                 || th instanceof IOException
//                 || th instanceof ConnectException) {
//                    logger.warn( "Cannot access public internet- part of test not executed" );
//                } else {
//                    throw rdfx;
//                }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** test moving things between models
//     * @param m the model implementation under test
//     */
//    public void test19(Model m1, Model m2) {
//        String  test = "Test19";
//        int     n = 0;
//
//        try {
//            Statement stmt;
//            StmtIterator sIter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                Resource r11 = m1.createResource();
//                Resource r12 = m2.createResource(new ResTestObjF());
//                long size1 = m1.size();
//                long size2 = m2.size();
//
//                r11.addProperty(RDF.value, 1);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                stmt = m2.createStatement(r11, RDF.value, r12);
//                n++; if (! (stmt.getSubject().getModel() == m2)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m2)) error(test,n);
//
//                m1.add(stmt);
//                n++; if (! (m1.size() == ++size1)) error(test, n);
//                n++; if (! (m2.size() == size2)) error(test,n);
//
//                sIter = m1.listStatements(
//                                    new SimpleSelector(r11, RDF.value, r12));
//                n++; if (! sIter.hasNext()) error(test, n);
//                n++; stmt = sIter.nextStatement();
//                n++; if (! (stmt.getSubject().getModel() == m1)) error(test,n);
//                n++; if (! (stmt.getResource().getModel() == m1)) error(test,n);
//                sIter.close();
//
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//   /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test20(Model m) {
//        String  test = "Test20";
//        int     n = 0;
//
//        try {
////            System.out.println("Beginning " + test);
//            Statement s1 = null;
//            Statement s2 = null;
//
//            try {
//                n=100;
//                n++; s1 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s1.isReified()) error(test,n);
//                n++; m.add(s1);
//                n++; if (s1.isReified()) error(test,n);
//                n++; s2 = m.createStatement(m.createResource(),
//                                            RDF.type,
//                                            RDFS.Class);
//                n++; if (s2.isReified()) error(test,n);
//                n++; m.add(s2);
//                n++; if (s2.isReified()) error(test,n);
///*
//                n++; m.add(s1, RDF.value, new LiteralImpl("foo"));
//                n++; if (!s1.isReified()) error(test,n);
//
//                n++; m.add(s1, RDF.value, s2);
//                n++; if (!s2.isReified()) error(test,n);
// */
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    /** Testing for miscellaneous bugs
//     * @param m the model implementation under test
//     */
//    public void test97(Model m) {
//        String  test = "Test97";
//        int     n = 0;
//
//        try {
//
////            System.out.println("Beginning " + test);
//
//                /*
//                    the _null_ argument to LiteralImpl was preserved only for backward
//                    compatability. It was be logged and has now become an exception.
//                    (Brian and Chris had a discussion about this and agreed).
//                */
//                // Node.nullLiteralsGenerateWarnings();
//                try
//                    {
//                   n=100; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                        {}
//                try
//                    {
//                   n=101; m.query(new SimpleSelector(null,
//                                                   null,
//                                                   new LiteralImpl( Node.createLiteral( null, "en", false ), (ModelCom) m)));
//                    error( test, n );
//                    }
//                catch (NullPointerException e)
//                    {}
//                // end of nullLiteralsGenerateWarnings code
//
//               n=102;
//               StmtIterator iter
//                            = m.listStatements(new SimpleSelector(null,
//                                                                null,
//                                                                (String) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//               n=103;
//               iter = m.listStatements(new SimpleSelector(null,
//                                                        null,
//                                                        (Object) null));
//               while (iter.hasNext()) {
//                   RDFNode o = iter.nextStatement().getObject();
//               }
//
//            } catch (Exception e) {
//                error(test, n, e);
//            }
////        System.out.println("End of " + test);
//    }
//
//    /** Empty the passed in model
//     * @param m the model implementation under test
//     */
//    public void test99(Model m) {
//        String  test = "Test5";
//        int     n = 0;
//
//        try {
//            StmtIterator iter;
////            System.out.println("Beginning " + test);
//
//            try {
//                n=100;
//                n++; iter = m.listStatements();
//                while (iter.hasNext()) {
//                    iter.nextStatement();
//                    n++;    iter.remove();
//                }
//                n++; iter.close();
//                n++; if (! (m.size()==0)) error(test,999);
//            } catch (Exception e) {
//                error(test, n, e);
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
////        System.out.println("End of " + test);
//    }
//
//    public void testResource(Model m, Resource r, String test,
//                               int n, int numProps) {
//        try {
//            StmtIterator iter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal tvLiteral = m.createLiteral("test 12 string 2");
//            Resource tvResource = m.createResource();
//            String     lang = "fr";
//            Statement stmt;
//
//            n++; if (! r.addProperty(RDF.value, tvByte)
//                        .hasProperty(RDF.value, tvByte)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvShort)
//                        .hasProperty(RDF.value, tvShort)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvInt)
//                        .hasProperty(RDF.value, tvInt)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvLong)
//                        .hasProperty(RDF.value, tvLong)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvChar)
//                        .hasProperty(RDF.value, tvChar)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvFloat)
//                        .hasProperty(RDF.value, tvFloat)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvDouble)
//                        .hasProperty(RDF.value, tvDouble)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvString)
//                        .hasProperty(RDF.value, tvString)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvString, lang)
//                        .hasProperty(RDF.value, tvString, lang)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvObject)
//                        .hasProperty(RDF.value, tvObject)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvLiteral)
//                        .hasProperty(RDF.value, tvLiteral)) error(test, n);
//            n++; if (! r.addProperty(RDF.value, tvResource)
//                        .hasProperty(RDF.value, tvResource)) error(test, n);
//            n++; if (! r.getRequiredProperty(RDF.value).getSubject().equals(r))
//                       error(test,n);
//            n++;Property p = m.createProperty("foo/", "bar");
//                try {
//                     r.getRequiredProperty(p); error(test, n);
//                } catch (PropertyNotFoundException e) {
//                    // as required
//                }
//            n++; iter = r.listProperties(RDF.value);
//                 int count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != 12) error(test,n);
//           n++; iter = r.listProperties(p);
//                 count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != 0) error(test,n);
//           n++; iter = r.listProperties();
//                 count = 0;
//                 while (iter.hasNext()) {
//                    stmt = iter.nextStatement();
//                    if (! stmt.getSubject().equals(r)) error(test, n);
//                    count++;
//                }
//           n++; if (count != (12+numProps)) error(test,n);
//
//           n++; r.removeProperties();
//           n++; Model mm = m.query(new SimpleSelector(r, null, (RDFNode) null));
//                if (! (mm.size()==0)) error(test,n);
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testContainer(Model m, Container cont1, Container cont2,
//                                String test, int n) {
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            String     lang     = "en";
//            int        num=10;
//            Statement stmt;
//
//            n=(n/100+1)*100;
//            n++; if (! (cont1.size() == 0)) error(test,n);
//
//            n=(n/100+1)*100;
//            n++; cont1.add(tvBoolean);
//            n++; if (! cont1.contains(tvBoolean)) error(test, n);
//            n++; cont1.add(tvByte);
//            n++; if (! cont1.contains(tvByte)) error(test, n);
//            n++; cont1.add(tvShort);
//            n++; if (! cont1.contains(tvShort)) error(test, n);
//            n++; cont1.add(tvInt);
//            n++; if (! cont1.contains(tvInt)) error(test, n);
//            n++; cont1.add(tvLong);
//            n++; if (! cont1.contains(tvLong)) error(test, n);
//            n++; cont1.add(tvChar);
//            n++; if (! cont1.contains(tvChar)) error(test, n);
//            n++; cont1.add(tvFloat);
//            n++; if (! cont1.contains(tvFloat)) error(test, n);
//            n++; cont1.add(tvDouble);
//            n++; if (! cont1.contains(tvDouble)) error(test, n);
//            n++; cont1.add(tvString);
//            n++; if (! cont1.contains(tvString)) error(test, n);
//            n++; if ( cont1.contains(tvString, lang)) error(test, n);
//            n++; cont1.add(tvString, lang);
//            n++; if (! cont1.contains(tvString, lang)) error(test, n);
//            n++; cont1.add(tvLiteral);
//            n++; if (! cont1.contains(tvLiteral)) error(test, n);
//            n++; cont1.add(tvResObj);
//            n++; if (! cont1.contains(tvResObj)) error(test, n);
//            n++; cont1.add(tvLitObj);
//            n++; if (! cont1.contains(tvLitObj)) error(test, n);
//            n++; if (! (cont1.size()==13)) error(test,n);
//
//            {
//                n=(n/100+1)*100;;
//                     for (int i=0; i<num; i++) {
//                        cont2.add(i);
//                    }
//                n++; if (! (cont2.size()==num)) error(test,n);
//                n++; nIter = cont2.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=(n/100+1)*100;;
//                n++; nIter=cont2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;;
//                n++; nIter = cont2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testBag(Model m, Bag bag1, Bag bag2, Bag bag3,
//                          String test, int n) {
//        int num = 10;
//        NodeIterator nIter;
//
//        try {
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//
//                    for (int i=0; i<num; i++) {
//                        bag1.add(i);
//                    }
//                n++; nIter=bag1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                     nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = bag1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        bag2.add(i);
//                    }
//                n++; nIter=bag2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//               n=(n/100+1)*100;
//                n++; nIter = bag2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//              n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        bag3.add(i);
//                    }
//                n++; nIter=bag3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;;
//                n++; nIter = bag3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testAlt(Model m, Alt alt1, Alt alt2, Alt alt3, Alt alt4,
//                         String test, int n) {
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            String     lang     = "fr";
//            int        num=10;
//            Statement stmt;
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//               n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt1.add(i);
//                    }
//                n++; nIter=alt1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = alt1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt2.add(i);
//                    }
//                n++; nIter=alt2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=550;
//                n++; nIter = alt2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=580;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                        alt3.add(i);
//                    }
//                n++; nIter=alt3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                n++; nIter = alt3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100+1)*100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=(n/100+1)*100;
//                n++; if (! (alt4.setDefault(tvLiteral)
//                               .getDefault().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (! (alt4.setDefault(tvLiteral)
//                               .getDefaultLiteral().equals(tvLiteral)))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvResource)
//                               .getDefaultResource().equals(tvResource))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvByte)
//                               .getDefaultByte()== tvByte))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvShort)
//                               .getDefaultShort()==tvShort))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvInt)
//                               .getDefaultInt()==tvInt))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvLong)
//                               .getDefaultLong()==tvLong))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvChar)
//                               .getDefaultChar()==tvChar))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvFloat)
//                               .getDefaultFloat()==tvFloat))
//                       error(test,n);
//                n++; if (!  (alt4.setDefault(tvDouble)
//                               .getDefaultDouble()==tvDouble))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvString)
//                               .getDefaultString().equals(tvString))
//                       error(test,n);
//                n++; if (!  alt4.getDefaultLanguage().equals(""))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvString, lang)
//                               .getDefaultString().equals(tvString))
//                       error(test,n);
//                n++; if (!  alt4.getDefaultLanguage().equals(lang))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvResObj)
//                               .getDefaultResource(new ResTestObjF())
//                               .equals(tvResObj))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvLitObj)
//                               .getDefaultObject(new LitTestObjF())
//                               .equals(tvLitObj))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvAlt)
//                               .getDefaultAlt()
//                               .equals(tvAlt))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvBag)
//                               .getDefaultBag()
//                               .equals(tvBag))
//                       error(test,n);
//                n++; if (!  alt4.setDefault(tvSeq)
//                               .getDefaultSeq()
//                               .equals(tvSeq))
//                       error(test,n);
//            }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void testSeq(Model m, Seq seq1, Seq seq2, Seq seq3, Seq seq4,
//                           Seq seq5, Seq seq6, Seq seq7, String test, int n) {
//
//        try {
//            NodeIterator nIter;
//            StmtIterator sIter;
//            boolean    tvBoolean = true;
//            byte       tvByte = 1;
//            short      tvShort = 2;
//            int        tvInt = -1;
//            long       tvLong = -2;
//            char       tvChar = '!';
//            float      tvFloat = (float) 123.456;
//            double     tvDouble = -123.456;
//            String     tvString = "test 12 string";
//            LitTestObj tvObject = new LitTestObj(12345);
//            Literal    tvLiteral = m.createLiteral("test 12 string 2");
//            Resource   tvResource = m.createResource();
//            Resource   tvResObj = m.createResource(new ResTestObjF());
//            Object     tvLitObj = new LitTestObj(1234);
//            Bag        tvBag    = m.createBag();
//            Alt        tvAlt    = m.createAlt();
//            Seq        tvSeq    = m.createSeq();
//            String     lang     = "fr";
//            int        num=10;
//            Statement stmt;
//
//            {
//
//                     for (int i=0; i<num; i++) {
//                        seq1.add(i);
//                    }
//                n++; if (! (seq1.size()==num)) error(test,n);
//                n++; nIter = seq1.iterator();
//                    for (int i=0; i<num; i++) {
//                        if ( ! (((Literal) nIter.nextNode()).getInt() == i))
//                            error(test, 320+i);
//                    }
//                    nIter.close();
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {true,  true,  true,  false, false,
//                   false, false, false, true,  true };
//
//                n=(n/100)*100 + 100;
//                n++; nIter=seq1.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq1.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false,  true,  true,  false, false,
//                   false, false, false, true,  false };
//
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                        seq2.add(i);
//                    }
//                n++; nIter=seq2.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq2.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                boolean[] found = new boolean[num];
//                boolean[] pattern =
//                  {false, false, false, false, false,
//                   false, false, false, false, false};
//
//               n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                        seq3.add(i);
//                    }
//                n++; nIter=seq3.iterator();
//                     for (int i=0; i<num; i++) {
//                n++;    nIter.nextNode();
//                n++;    if (! pattern[i]) nIter.remove();
//                        found[i] = false;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                n++; nIter = seq3.iterator();
//                     while (nIter.hasNext()) {
//                        int v = ((Literal) nIter.nextNode()).getInt();
//                n++;    if (  found[v]) error(test,n);
//                        found[v] = true;
//                     }
//                n++; nIter.close();
//                n=(n/100)*100 + 100;
//                     for (int i=0; i<num; i++) {
//                n++;    if (! (found[i]==pattern[i])) error(test,n);
//                    }
//            }
//
//            {
//                n=(n/100)*100 + 100;
//                n++; seq4.add(tvBoolean);
//                n++; if (!  (seq4.getBoolean(1)==tvBoolean)) error(test,n);
//                n++; seq4.add(tvByte);
//                n++; if (!  (seq4.getByte(2)==tvByte)) error(test,n);
//                n++; seq4.add(tvShort);
//                n++; if (!  (seq4.getShort(3)==tvShort)) error(test,n);
//                n++; seq4.add(tvInt);
//                n++; if (!  (seq4.getInt(4)==tvInt)) error(test,n);
//                n++; seq4.add(tvLong);
//                n++; if (!  (seq4.getLong(5)==tvLong)) error(test,n);
//                n++; seq4.add(tvChar);
//                n++; if (!  (seq4.getChar(6)==tvChar)) error(test,n);
//                n++; seq4.add(tvFloat);
//                n++; if (!  (seq4.getFloat(7)==tvFloat)) error(test,n);
//                n++; seq4.add(tvDouble);
//                n++; if (!  (seq4.getDouble(8)==tvDouble)) error(test,n);
//                n++; seq4.add(tvString);
//                n++; if (!  (seq4.getString(9).equals(tvString))) error(test,n);
//                n++; if (!  (seq4.getLanguage(9).equals(""))) error(test,n);
//                n++; seq4.add(tvString, lang);
//                n++; if (!  (seq4.getString(10).equals(tvString))) error(test,n);
//                n++; if (!  (seq4.getLanguage(10).equals(lang))) error(test,n);
//                n++; seq4.add(tvLitObj);
//                n++; if (!  (seq4.getObject(11, new LitTestObjF())
//                                .equals(tvLitObj))) error(test,n);
//                n++; seq4.add(tvResource);
//                n++; if (!  (seq4.getResource(12).equals(tvResource))) error(test,n);
//                n++; seq4.add(tvLiteral);
//                n++; if (!  (seq4.getLiteral(13).equals(tvLiteral))) error(test,n);
//                n++; seq4.add(tvResObj);
//                n++; if (!  (seq4.getResource(14, new ResTestObjF())
//                                .equals(tvResObj))) error(test,n);
//                n++; seq4.add(tvBag);
//                n++; if (!  (seq4.getBag(15).equals(tvBag))) error(test,n);
//                n++; seq4.add(tvAlt);
//                n++; if (!  (seq4.getAlt(16).equals(tvAlt))) error(test,n);
//                n++; seq4.add(tvSeq);
//                n++; if (!  (seq4.getSeq(17).equals(tvSeq))) error(test,n);
//                n++; try {
//                        seq4.getInt(18); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//                n++; try {
//                        seq4.getInt(0); error(test,n);
//                    } catch (SeqIndexBoundsException e) {
//                        // as required
//                    }
//            }
//
//            {
//                n=(n/100)*100 + 100;
//                for (int i=0; i<num; i++) {
//                    seq5.add(i);
//                }
//
//                     try {
//                n++;        seq5.add(0, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//                     seq5.add(num+1, false);
//                     if (seq5.size()!=num+1) error(test,n);
//                     seq5.remove(num+1);
//                     try {
//                n++;        seq5.add(num+2, false); error(test,n);
//                     } catch (SeqIndexBoundsException e) {
//                        // as required
//                     }
//
//               n=(n/100)*100 + 100;
//                    int size = seq5.size();
//                    for (int i=1; i<=num-1; i++) {
//               n++;     seq5.add(i, 1000+i);
//               n++;     if (! (seq5.getInt(i)==1000+i)) error(test,n);
//               n++;     if (! (seq5.getInt(i+1)==0)) error(test, n);
//               n++;     if (! (seq5.size()==(size+i))) error(test,n);
//               n++;     if (! (seq5.getInt(size)==(num-i-1))) error(test,n);
//                    }
//               n=(n/100)*100 + 100;
//                    seq6.add(m.createResource());
//                    seq6.add(1, tvBoolean);
//               n++; if (! (seq6.getBoolean(1)==tvBoolean)) error(test,n);
//                    seq6.add(1, tvByte);
//               n++; if (! (seq6.getByte(1)==tvByte)) error(test,n);
//                    seq6.add(1, tvShort);
//               n++; if (! (seq6.getShort(1)==tvShort)) error(test,n);
//                    seq6.add(1, tvInt);
//               n++; if (! (seq6.getInt(1)==tvInt)) error(test,n);
//                    seq6.add(1, tvLong);
//               n++; if (! (seq6.getLong(1)==tvLong)) error(test,n);
//                    seq6.add(1, tvChar);
//               n++; if (! (seq6.getChar(1)==tvChar)) error(test,n);
//                    seq6.add(1, tvFloat);
//               n++; if (! (seq6.getFloat(1)==tvFloat)) error(test,n);
//                    seq6.add(1, tvDouble);
//               n++; if (! (seq6.getDouble(1)==tvDouble)) error(test,n);
//                    seq6.add(1, tvString);
//               n++; if (! (seq6.getString(1).equals(tvString))) error(test,n);
//                    seq6.add(1, tvString, lang);
//               n++; if (! (seq6.getString(1).equals(tvString))) error(test,n);
//                    seq6.add(1, tvResource);
//               n++; if (! (seq6.getResource(1).equals(tvResource))) error(test,n);
//                    seq6.add(1, tvLiteral);
//               n++; if (! (seq6.getLiteral(1).equals(tvLiteral))) error(test,n);
//                    seq6.add(1, tvLitObj);
//               n++; if (! (seq6.getObject(1, new LitTestObjF())
//                              .equals(tvLitObj))) error(test,n);
//
//               n=(n/100)*100 + 100;
//               n++; if (! (seq6.indexOf(tvLitObj)==1)) error(test,n);
//               n++; if (! (seq6.indexOf(tvLiteral)==2)) error(test,n);
//               n++; if (! (seq6.indexOf(tvResource)==3)) error(test,n);
//               n++; if (! (seq6.indexOf(tvString,lang)==4)) error(test,n);
//               n++; if (! (seq6.indexOf(tvString)==5)) error(test,n);
//               n++; if (! (seq6.indexOf(tvDouble)==6)) error(test,n);
//               n++; if (! (seq6.indexOf(tvFloat)==7)) error(test,n);
//               n++; if (! (seq6.indexOf(tvChar)==8)) error(test,n);
//               n++; if (! (seq6.indexOf(tvLong)==9)) error(test,n);
//               n++; if (! (seq6.indexOf(tvInt)==10)) error(test,n);
//               n++; if (! (seq6.indexOf(tvShort)==11)) error(test,n);
//               n++; if (! (seq6.indexOf(tvByte)==12)) error(test,n);
//               n++; if (! (seq6.indexOf(tvBoolean)==13)) error(test,n);
//               n++; if (! (seq6.indexOf(1234543)==0)) error(test,n);
//
//               n=(n/100)*100 + 100;
//                   for (int i=0; i<num; i++) {
//                       seq7.add(i);
//                   }
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvBoolean);
//              n++; if (! (seq7.getBoolean(5)==tvBoolean)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvByte);
//              n++; if (! (seq7.getByte(5)==tvByte)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvShort);
//              n++; if (! (seq7.getShort(5)==tvShort)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvInt);
//              n++; if (! (seq7.getInt(5)==tvInt)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//             n=(n/100)*100 + 100;
//                   seq7.set(5, tvLong);
//              n++; if (! (seq7.getLong(5)==tvLong)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvChar);
//              n++; if (! (seq7.getChar(5)==tvChar)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvFloat);
//              n++; if (! (seq7.getFloat(5)==tvFloat)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvDouble);
//              n++; if (! (seq7.getDouble(5)==tvDouble)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvString);
//              n++; if (! (seq7.getString(5).equals(tvString))) error(test,n);
//              n++; if (! (seq7.getLanguage(5).equals(""))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//                   seq7.set(5, tvString,lang);
//              n++; if (! (seq7.getString(5).equals(tvString))) error(test,n);
//              n++; if (! (seq7.getLanguage(5).equals(lang))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvLiteral);
//              n++; if (! (seq7.getLiteral(5).equals(tvLiteral))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvResource);
//              n++; if (! (seq7.getResource(5).equals(tvResource))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvLitObj);
//              n++; if (! (seq7.getObject(5, new LitTestObjF()))
//                             .equals(tvLitObj)) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//              n=(n/100)*100 + 100;
//                   seq7.set(5, tvResObj);
//              n++; if (! (seq7.getResource(5, new ResTestObjF())
//                             .equals(tvResObj))) error(test,n);
//              n++; if (! (seq7.getInt(4)==3)) error(test,n);
//              n++; if (! (seq7.getInt(6)==5)) error(test,n);
//              n++; if (! (seq7.size()==num)) error(test,n);
//
//        }
//
//        } catch (Exception e) {
//            logger.error( "test " + test + "[" + n + "]", e );
//            errors = true;
//        }
//    }
//
//    public void error(String testName, int testNum) {
//        System.out.println("Test Failed: "
//        + testName + " "
//        + testNum  + " ");
//        errors = true;
//    }
//
//    public void error(String testName, int testNum, long v) {
//        System.out.println("Test Failed: "
//        + testName + " "
//        + testNum  + " "
//        + Long.toString(v));
//        errors = true;
//    }
//
//    public void error(String testName, int testNum, Exception e) {
//        System.out.println("Test Failed: "
//        + testName + " "
//        + testNum  + " "
//        + e.toString());
//        errors = true;
//    }
//
//    public boolean getErrors() {
//        return errors;
//    }
//
//    public boolean setErrors(boolean b) {
//        boolean temp = errors;
//        errors = b;
//        return temp;
//    }
//
//    public class LitTestObj {
//        protected long content;
//
//        public LitTestObj(long l) {
//            content = l;
//        }
//
//        public LitTestObj(String s) {
//            content = Long.parseLong(s.substring(1, s.length()-1));
//        }
//
//        public String toString() {
//            return "[" + Long.toString(content) + "]";
//        }
//
//        public boolean equals(Object o) {
//            if (o instanceof LitTestObj) {
//                return content == ((LitTestObj)o).content;
//            } else {
//                return false;
//            }
//        }
/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
