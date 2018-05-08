package bgu.spl.a2;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * an abstract class that represents a task that may be executed using the
 * {@link WorkStealingThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 * @param <R> the task result type
 */
public abstract class Task<R> {
	private Deferred<R> defResult = new Deferred<R>();
	private Processor myHandler = null;
	private AtomicInteger childTasks = new AtomicInteger();
	private boolean started = false;
	private Runnable callBack;
	/**
	 * start handling the task - note that this method is protected, a handler
	 * cannot call it directly but instead must use the
	 * {@link #handle(bgu.spl.a2.Processor)} method
	 */
	protected abstract void start();

	/**
	 *
	 * start/continue handling the task
	 *
	 * this method should be called by a processor in order to start this task
	 * or continue its execution in the case where it has been already started,
	 * any sub-tasks / child-tasks of this task should be submitted to the queue
	 * of the handler that handles it currently
	 *
	 * IMPORTANT: this method is package protected, i.e., only classes inside
	 * the same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 * @param handler the handler that wants to handle the task
	 */
	/*package*/ final void handle(Processor handler) {
		if (!started){
			started = true;
			myHandler = handler;
			this.start();
			myHandler.getPool().getVer().inc(); //VERSION INCREASE
		}
		else {
			if (childTasks.get() == 0 && callBack != null){
				callBack.run();
			}
		}
	}

	/**
	 * This method schedules a new task (a child of the current task) to the
	 * same processor which currently handles this task.
	 *
	 * @param task the task to execute
	 */
	protected final void spawn(Task<?>... task) {
		myHandler.getPool().getVer().inc(); //VERSION INCREASE
		for (int i = 0 ; i < task.length ; i++){
			childTasks.incrementAndGet();
			myHandler.getPool().getTasks()[myHandler.getId()].addFirst(task[i]); //add the spawned tasks
		}
	}

	/**
	 * add a callback to be executed once *all* the given tasks results are
	 * resolved
	 *
	 * Implementors note: make sure that the callback is running only once when
	 * all the given tasks completed.
	 *
	 * @param tasks
	 * @param callback the callback to execute once all the results are resolved
	 */
	@SuppressWarnings("rawtypes")
	protected final void whenResolved(Collection<? extends Task<?>> tasks, Runnable callback) {
		this.callBack = callback;
		if (defResult.isResolved())
			callback.run();
		int numOfTasks = tasks.size();
		for (int i = 0 ; i < numOfTasks ; i ++){
			((Task)tasks.toArray()[i]).defResult.whenResolved(() -> {
				if (childTasks.decrementAndGet() == 0){
					myHandler.getPool().submit(this);
				}
			});
		}
	}

	/**
	 * resolve the internal result - should be called by the task derivative
	 * once it is done.
	 *
	 * @param result - the task calculated result
	 */
	protected final void complete(R result) {
		if (!defResult.isResolved()) {
			defResult.resolve(result);
			myHandler.getPool().getVer().inc(); //VERSION INCREASE
		}
	}

	/**
	 * @return this task deferred result
	 */
	public final Deferred<R> getResult() {
		return defResult;
	}
}
