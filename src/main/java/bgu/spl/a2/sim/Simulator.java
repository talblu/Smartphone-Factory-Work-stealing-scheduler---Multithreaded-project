/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tasks.ManufactorTask;
import bgu.spl.a2.sim.tasks.WaveTask;
import bgu.spl.a2.sim.tools.GCDScrewdriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	private static WorkStealingThreadPool pool;
	private static WaveTask[] Waves;
	private static Deferred<ConcurrentLinkedQueue<Product>> finished;
	private static CountDownLatch l;
	private static String filePath;

	/**
	 * Begin the simulation
	 * Should not be called before attachWorkStealingThreadPool()
	 */
	public static ConcurrentLinkedQueue<Product> start() {
		try {
			readJSON(filePath);
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}
		AtomicInteger wavesCounter = new AtomicInteger(Waves.length);
		ConcurrentLinkedQueue<Product> res = new ConcurrentLinkedQueue<Product>();
		pool.start();
		pool.submit(Waves[0]);
		for (int n = 0 ; n < Waves.length ; n++)
			Waves[n].getResult().whenResolved(() -> {
				l.countDown();
				if (wavesCounter.decrementAndGet() == 0){
					for (int i = 0 ; i < Waves.length ; i++) // go through each wave
						for (int j = 0 ; j<Waves[i].getResult().get().length ; j++) //go through each manufacture task
							for (int k = 0 ; k < Waves[i].getResult().get()[j].getResult().get().length; k++){// go through each finished product
								res.add(Waves[i].getResult().get()[j].getResult().get()[k]); //add the product to the queue
							}
					finished.resolve(res);
				}
			});
		try {
			l.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	 * @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	 */
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool) {
		pool = myWorkStealingThreadPool;
	}

	public static void main(String [] args) {
		filePath = args[0]; 
		finished = new Deferred<>();
		try {
			ConcurrentLinkedQueue<Product> res = start();
			FileOutputStream fout = new FileOutputStream("result.ser");
			@SuppressWarnings("resource")
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(res);
			pool.shutdown();
		}

		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get all the data from the JSON file
	 */
	private static void readJSON(String filePath) throws FileNotFoundException, IOException, org.json.simple.parser.ParseException {
		Warehouse warehouse = new Warehouse();
		ArrayList<WaveTask> wavesCollection = new ArrayList<>(); //tasks collection to indicate when all the waves were completed
		ManufactorTask[][] allWaves = null;
		long nthreads = 0;
		// read the json file
		FileReader reader = new FileReader(filePath);
		JSONParser jsonParser = new JSONParser();

		//read initialWarehouse
		Object obj = (JSONObject) jsonParser.parse(reader);
		JSONObject initialWarehouse = (JSONObject)obj;

		/**
				// Reading the storage info from the json file
		 */

		//read number of threads from the JSON file
		nthreads = (long) initialWarehouse.get("threads");
		attachWorkStealingThreadPool(new WorkStealingThreadPool((int) nthreads));
		// get an array from the JSON object
		JSONArray initialTools = (JSONArray)initialWarehouse.get("tools");
		Tool[] tools = new Tool[initialTools.size()];

		// take the tools of the json array
		for(int i=0; i<initialTools.size(); i++){
			JSONObject tool = (JSONObject) initialTools.get(i);
			String toolType = (String) tool.get("tool");
			long amount = (long) tool.get("qty");
			switch (toolType){
			case "gs-driver": 
				tools[i] = new GCDScrewdriver();
				break;
			case "np-hammer": 
				tools[i] = new NextPrimeHammer();
				break;
			case "rs-pliers": 
				tools[i] = new RandomSumPliers();
				break;
			}
			warehouse.addTool(tools[i], (int)amount);
		}

		/**
				// Reading the services info from the json file
		 */

		JSONArray initialPlans = (JSONArray)initialWarehouse.get("plans");
		ManufactoringPlan[] plans = new ManufactoringPlan[initialPlans.size()];
		for(int i=0; i<initialPlans.size(); i++){
			JSONObject plan = (JSONObject) initialPlans.get(i);
			String productName = (String) plan.get("product");
			JSONArray toolsJSON = (JSONArray) plan.get("tools");
			String[] toolsArr = new String[toolsJSON.size()];
			for (int j = 0 ; j < toolsJSON.size(); j++)
				toolsArr[j] = (String)toolsJSON.get(j);
			JSONArray partsJSON = (JSONArray) plan.get("parts");
			String[] partsArr = new String[partsJSON.size()];
			for (int j = 0 ; j < partsJSON.size(); j++)
				partsArr[j] = (String)partsJSON.get(j);
			plans[i] = new ManufactoringPlan(productName, partsArr, toolsArr);
			warehouse.addPlan(plans[i]);
		}
		JSONArray initialWaves = (JSONArray)initialWarehouse.get("waves");
		allWaves = new ManufactorTask[initialWaves.size()][]; //All the manufacture tasks
		Waves = new WaveTask[allWaves.length];
		l = new CountDownLatch(Waves.length);
		for(int i=0; i<initialWaves.size(); i++){ 
			JSONArray currWave = (JSONArray) initialWaves.get(i);
			allWaves[i] = new ManufactorTask[currWave.size()];
			for (int j=0; j<currWave.size(); j++){
				JSONObject currOrder = (JSONObject)currWave.get(j);
				String productName = (String)currOrder.get("product");
				long productQty = (long)currOrder.get("qty"); 
				long productId = (long)currOrder.get("startId");
				ManufactorTask currTask = new ManufactorTask(productName, (int)productQty, productId, warehouse, pool);
				allWaves[i][j] = currTask;
			}
			Waves[i] = new WaveTask(allWaves[i], pool); //put current wave in a wave task
			wavesCollection.add(Waves[i]);
		}
		for (int i = 0 ; i < Waves.length - 1 ; i++){
			Waves[i].setNextWave(Waves[i+1]);//connect all the waves in a chain so that each wave will spawn the next one when complete
		}
	}
}