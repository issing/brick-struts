package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BrickStrutsTest extends TestCase {

    public BrickStrutsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickStrutsTest.class);
    }

    public void testApp() {
        assertTrue(true);
    }

}
