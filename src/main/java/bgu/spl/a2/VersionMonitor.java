package bgu.spl.a2;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {

	protected volatile int version = 0; //The value is volatile to make sure the newest value is always read by the threads.

	/**
	 * 
	 * @return current version monitor value
	 */
	public synchronized int getVersion() {
		return version;
	}

	/**
	 * Increase the version monitor value by 1 and notify all idle threads
	 */
	public synchronized void inc() {
		version++;
		notifyAll();
	}

	/**
	 * This method is used by the processors to stay in idle state until a change is made in the task list,
	 * indicated by the change in the version monitor value
	 * @param version value the processor is waiting to change
	 * @throws InterruptedException
	 */
	public synchronized void await(int version) throws InterruptedException {
		while (version == this.version)
			wait();
	}
}
