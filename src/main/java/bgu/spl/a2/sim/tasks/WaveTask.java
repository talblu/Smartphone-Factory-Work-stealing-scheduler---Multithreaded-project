package bgu.spl.a2.sim.tasks;

import java.util.ArrayList;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

/**
 * A class for an array of manufacture tasks
 */
public class WaveTask extends Task<ManufactorTask[]>{
	private ManufactorTask[] wave;
	private WaveTask nextWave;
	private WorkStealingThreadPool pool;

	/**
	 * WaveTask constructor
	 * 
	 * @param wave - current wave of manufacture tasks
	 * @param pool - thread pool containing the used threads
	 */
	public WaveTask(ManufactorTask[] wave, WorkStealingThreadPool pool){
		this.pool = pool;
		this.wave = wave;
		nextWave = null;
	}

	@Override
	protected void start() {
		ArrayList<ManufactorTask> tasksToSpawn = new ArrayList<>();
		for (int i = 0; i < wave.length; i++){
			tasksToSpawn.add(wave[i]);
			spawn(wave[i]);
		}
		whenResolved(tasksToSpawn, () -> {
			complete(wave);
			if (nextWave != null) {
				pool.submit(nextWave);
			}
		});
	}

	/**
	 * Set the next task wave to be manufactured
	 * 
	 * @param next - the next wave to be submitted to the thread pool task list
	 */
	public synchronized void setNextWave(WaveTask next){
		this.nextWave = next;
	}
}