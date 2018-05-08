package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VersionMonitorTest {

	VersionMonitor ver;

	@Before
	public void setUp() throws Exception {
		this.ver = new VersionMonitor();
	}

	@After
	public void tearDown() throws Exception {
		this.ver = null;
	}

	@Test
	public void testGetVersion() throws Exception {
		setUp();
		assertEquals(0, ver.getVersion());
		tearDown();
	}

	@Test
	public void testInc() throws Exception {
		setUp();
		ver.inc();
		assertEquals(1, ver.getVersion());
		tearDown();
	}

	@Test
	public void testAwait() throws Exception {
		setUp();
		Thread t = new Thread (()->{
			try { ver.await(0);}
			catch (Exception e){
				e.printStackTrace();
			}
		});
		t.start();
		ver.inc();
		try {
			t.join();
		}
		catch (InterruptedException e){
			e.printStackTrace();
		}
		assertNotEquals((Integer)0, (Integer)ver.getVersion());
		tearDown();
	}
}
