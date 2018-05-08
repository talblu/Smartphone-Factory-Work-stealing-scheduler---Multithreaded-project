package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {
	@SuppressWarnings("rawtypes")
	private ConcurrentLinkedDeque<Task>[] tasks;
	private Processor[] processors;
	private Thread[] threads;
	private VersionMonitor ver;
	/**
	 * creates a {@link WorkStealingThreadPool} which has nthreads
	 * {@link Processor}s. Note, threads should not get started until calling to
	 * the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads the number of threads that should be started by this
	 * thread pool
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WorkStealingThreadPool(int nthreads) {
		tasks = new ConcurrentLinkedDeque[nthreads];
		processors = new Processor[nthreads];
		threads = new Thread[nthreads];
		ver = new VersionMonitor();
		for (int i = 0 ; i < nthreads ; i ++) {
			processors[i] = new Processor(i, this);
			threads[i] = new Thread(processors[i]);
			tasks[i] = new ConcurrentLinkedDeque<Task>();
		}
	}

	/**
	 * submits a task to be executed by a processor belongs to this thread pool
	 *
	 * @param task the task to execute
	 */
	public void submit(Task<?> task) {
		if (!task.getResult().isResolved()) {
			int randomP = (int) (Math.random() * tasks.length);
			tasks[randomP].addFirst(task);
			ver.inc(); //VERSION INCREASE
		}
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and wait
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException if the thread that shut down the threads is
	 * interrupted
	 * @throws UnsupportedOperationException if the thread that attempts to
	 * shutdown the queue is itself a processor of this queue
	 */
	public void shutdown() throws InterruptedException { 
		for (int i = 0 ; i < threads.length ; i++) {
			if (threads[i] != Thread.currentThread())
				threads[i].interrupt();
			else
				throw new UnsupportedOperationException();
		}
	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		for (int i = 0 ; i < threads.length ; i++)
			threads[i].start();
	}

	/**
	 * 
	 * @return An array of queues containing all the tasks of all the processors
	 */
	@SuppressWarnings("rawtypes")
	ConcurrentLinkedDeque<Task>[] getTasks(){
		return tasks;
	}

	/**
	 * 
	 * @return Current version monitor value
	 */
	VersionMonitor getVer(){
		return ver;
	}
}
