package org.apache.jena.geosparql.implementation.jts;

import org.junit.*;
import org.locationtech.jts.geom.*;

import static org.junit.Assert.*;

public class CustomCoordinateSequenceTest {

    public CustomCoordinateSequenceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetCoordinate_2DSpatial_0DMeasure() {
        CustomCoordinateSequence sequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XY, "1 1, 2 2, 3 3");
        Coordinate coord = new CoordinateXY();
        int index = 1;

        sequence.getCoordinate(index, coord);

        assertEquals(2.0, coord.getX(), 0.001);
        assertEquals(2.0, coord.getY(), 0.001);
        assertTrue(Double.isNaN(coord.getZ()));
        assertTrue(Double.isNaN(coord.getM()));
    }

    @Test
    public void testGetCoordinate_3DSpatial_0DMeasure() {
        CustomCoordinateSequence sequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZ, "1 1 1, 2 2 2, 3 3 3");
        Coordinate coord = new Coordinate();
        int index = 1;

        sequence.getCoordinate(index, coord);

        assertEquals(2.0, coord.getX(), 0.001);
        assertEquals(2.0, coord.getY(), 0.001);
        assertEquals(2.0, coord.getZ(), 0.001);
        assertTrue(Double.isNaN(coord.getM()));
    }

    @Test
    public void testGetCoordinate_3DSpatial_1DMeasure() {
        CustomCoordinateSequence sequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XYZM, "1 1 1 1, 2 2 2 2, 3 3 3 3");
        System.out.println(sequence);
        Coordinate coord = new CoordinateXYZM();
        int index = 1;

        sequence.getCoordinate(index, coord);

        assertEquals(2.0, coord.getX(), 0.001);
        assertEquals(2.0, coord.getY(), 0.001);
        assertEquals(2.0, coord.getZ(), 0.001);
        assertEquals(2.0, coord.getM(), 0.001);
    }

    @Test
    public void testGetCoordinate_2DSpatial_1DMeasure() {
        CustomCoordinateSequence sequence = new CustomCoordinateSequence(CoordinateSequenceDimensions.XYM, "1 1 1, 2 2 2, 3 3 3");
        Coordinate coord = new CoordinateXYM();
        int index = 1;

        sequence.getCoordinate(index, coord);

        assertEquals(2.0, coord.getX(), 0.001);
        assertEquals(2.0, coord.getY(), 0.001);
        assertTrue(Double.isNaN(coord.getZ()));
        assertEquals(2.0, coord.getM(), 0.001);
    }
}