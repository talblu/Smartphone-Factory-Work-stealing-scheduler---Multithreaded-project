package bgu.spl.a2;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

	private final WorkStealingThreadPool pool;
	private final int id;
	private final int poolSize;

	/**
	 * constructor for this class
	 *
	 * IMPORTANT:
	 * 1) this method is package protected, i.e., only classes inside
	 * the same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 * 2) you may not add other constructors to this class
	 * nor you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param id - the processor id (every processor need to have its own unique
	 * id inside its thread pool)
	 * @param pool - the thread pool which owns this processor
	 */
	/*package*/ Processor(int id, WorkStealingThreadPool pool) {
		this.id = id;
		this.pool = pool;
		poolSize = pool.getTasks().length;
	}

	@Override
	public void run() {
		boolean interrupt=false;
		while (!interrupt) {
			if(Thread.currentThread().isInterrupted())
				interrupt=true;
			@SuppressWarnings("rawtypes")
			Task curTask = (Task)pool.getTasks()[id].pollFirst();
			if (curTask != null){
				if (!curTask.getResult().isResolved())
					curTask.handle(this);
			}
			else {
				boolean check=steal();
				if(!check){
					try {
						pool.getVer().await(pool.getVer().getVersion());
					}
					catch (InterruptedException e){
						interrupt=true;
					};
				}
			}
		}
	}

	/**
	 * This method is used by the processors to attempt stealing a task from other processors
	 * 
	 * @return boolean value indicating if the attempt was successful
	 */
	private boolean steal(){
		boolean success = false;
		for (int i = (id + 1) % poolSize ; i != id && !success ; i = (i + 1) % poolSize){ //go through all the task queues
			if (pool.getTasks()[i].size() > 1) {
				boolean empty = false;
				int numOfTasksToGet = (pool.getTasks()[i].size() / 2);
				for (int j = 0 ; j < numOfTasksToGet && !empty ; j++){ //attempt to steal from current queue
					@SuppressWarnings("rawtypes")
					Task stolen = (Task) pool.getTasks()[i].pollLast();
					if (stolen != null){
						success = true;
						pool.getTasks()[id].addFirst(stolen);		
					}
					else { //the current task queue is empty
						empty = true;
					}
				}
			}
		}
		return success;
	}

	/**
	 * 
	 * @return The unique ID of the current processor
	 */
	protected int getId(){
		return id;
	}

	/**
	 * 
	 * @return a reference to the operation environment for this processor
	 */
	protected WorkStealingThreadPool getPool(){
		return pool;
	}
}
