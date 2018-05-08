package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeferredTest {

	Deferred<Integer> def;
	Integer testInt;
	int num;
	
	@Before
	public void setUp() throws Exception {
		this.def = new Deferred<Integer>();
		this.testInt = new Integer(171);
		this.num = 0;
	}
	
	@After
	public void tearDown() throws Exception {
		this.def = null;
		this.testInt = 0;
		this.num = 0;
	}
	
	@Test
	public void testGet() throws Exception {
		setUp();
		boolean tester = false;
		try {
			def.get();
		}
		catch (IllegalStateException e) { 
			tester = true;
		}
		catch (Exception e) {
			tester = false;
		}
		
		assertTrue(tester);
		def.resolve(testInt);
		assertEquals(def.get(), testInt);
		tearDown();
	}

	@Test
	public void testIsResolved() throws Exception {
		setUp();
		assertFalse(def.isResolved());
		def.resolve(testInt);
		assertTrue(def.isResolved());
		tearDown();
	}

	@Test
	public void testResolve() throws Exception {
		setUp();
		boolean tester = true;
		
		try {
			def.resolve(testInt);
		}
		catch (Exception e) {
			tester = false;
		}
		
		assertTrue(tester);
		
		try {
			def.resolve(testInt);
		}
		catch (IllegalStateException e) {
			tester = false;
		}
		catch (Exception e) {
			tester = true;
		}
		
		assertFalse(tester);
		tearDown();
	}

	@Test
	public void testWhenResolved() throws Exception {
		setUp();
		Runnable runnable=()->num = 25;
		def.whenResolved(runnable);
		def.resolve((Integer)100);
		assertEquals(25, num);
		tearDown();
	}
}
