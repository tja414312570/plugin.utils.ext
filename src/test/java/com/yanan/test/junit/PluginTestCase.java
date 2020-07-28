package com.yanan.test.junit;

import junit.framework.TestCase;

public class PluginTestCase extends TestCase{
	  /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
    	System.out.println("执行了1");
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
    	System.out.println("执行了2");
    }
}
