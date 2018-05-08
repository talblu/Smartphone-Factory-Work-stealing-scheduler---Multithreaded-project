package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.*;
import bgu.spl.a2.sim.conf.ManufactoringPlan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import bgu.spl.a2.Deferred;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {
	private ConcurrentHashMap<String, AtomicInteger> tools;
	private ConcurrentHashMap<String, ManufactoringPlan> plans;

	private ConcurrentLinkedQueue<Deferred<Tool>> HammerQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> DriverQueue;
	private ConcurrentLinkedQueue<Deferred<Tool>> PliersQueue;

	private GCDScrewdriver  gsDriver;
	private NextPrimeHammer npHammer;
	private RandomSumPliers rsPliers;

	private final Lock HammerLock;
	private final Lock PliersLock;
	private final Lock DriverLock;

	/**
	 * Constructor
	 */
	public Warehouse() {
		tools = new ConcurrentHashMap<String, AtomicInteger>();
		plans = new ConcurrentHashMap<String, ManufactoringPlan>();

		HammerQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		DriverQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();
		PliersQueue = new ConcurrentLinkedQueue<Deferred<Tool>>();

		gsDriver = new GCDScrewdriver();
		npHammer = new NextPrimeHammer();
		rsPliers = new RandomSumPliers();

		HammerLock = new ReentrantLock();
		PliersLock = new ReentrantLock();
		DriverLock = new ReentrantLock();

	}

	/**
	 * Tool acquisition procedure
	 * Note that this procedure is non-blocking and should return immediately
	 * @param type - string describing the required tool
	 * @return a deferred promise for the  requested tool
	 */
	public Deferred<Tool> acquireTool(String type) {
		/*
		 * Note: if the requested tool is not available (else condition in each case), a non-resolved deferred tool will be returned.
		 * the deferred tool will be resolved once available
		 */
		Deferred<Tool> defTool = new Deferred<Tool>();
		switch (type) {
		case "np-hammer" :
			HammerLock.lock();
			if (tools.get(type).get() > 0){
				tools.get(type).decrementAndGet();
				defTool.resolve(npHammer);
				HammerLock.unlock();
				return defTool;
			}
			else {
				HammerQueue.add(defTool);
				HammerLock.unlock();
				return defTool;
			}
		case "rs-pliers" :
			PliersLock.lock();
			if (tools.get(type).get() > 0){
				tools.get(type).decrementAndGet();
				defTool.resolve(rsPliers);
				PliersLock.unlock();
				return defTool;
			}
			else {
				PliersQueue.add(defTool);
				PliersLock.unlock();
				return defTool;
			}

		default :
			DriverLock.lock();
			if (tools.get(type).get() > 0){
				tools.get(type).decrementAndGet();
				defTool.resolve(gsDriver);
				DriverLock.unlock();
				return defTool;
			}
			else {
				DriverQueue.add(defTool);
				DriverLock.unlock();
				return defTool;
			}
		}
	}


	/**
	 * Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	 * @param tool - The tool to be returned
	 */
	public void releaseTool(Tool tool) {
		String type = tool.getType();
		switch (type) {
		case "np-hammer" :
			HammerLock.lock();
			tools.get(type).incrementAndGet();
			if (HammerQueue.size() > 0){
				tools.get(type).decrementAndGet();
				HammerQueue.poll().resolve(npHammer);
			}
			HammerLock.unlock();
			break;
		case "rs-pliers" :
			PliersLock.lock();
			tools.get(type).incrementAndGet();
			if (PliersQueue.size() > 0){
				tools.get(type).decrementAndGet();
				PliersQueue.poll().resolve(rsPliers);
			}
			PliersLock.unlock();
			break;
		case "gs-driver" :
			DriverLock.lock();
			tools.get(type).incrementAndGet();
			if (DriverQueue.size() > 0){
				tools.get(type).decrementAndGet();
				DriverQueue.poll().resolve(gsDriver);
			}
			DriverLock.unlock();
			break;
		}
	}

	/**
	 * Getter for ManufactoringPlans
	 * @param product - a string with the product name for which a ManufactoringPlan is desired
	 * @return A ManufactoringPlan for product
	 */
	public ManufactoringPlan getPlan(String product) {
		if (!plans.containsKey(product))
			throw new IllegalStateException("Plan " + product + " does not exist");
		return plans.get(product);
	}

	/**
	 * Store a ManufactoringPlan in the warehouse for later retrieval
	 * @param plan - a ManufactoringPlan to be stored
	 */
	public void addPlan(ManufactoringPlan plan) {
		if (plans.contains(plan))
				throw new IllegalStateException("Plan " + plan.getProductName() + " already exists");
		plans.put(plan.getProductName(), plan);
		}

	/**
	 * Store a qty Amount of tools of type tool in the warehouse for later retrieval
	 * @param tool - type of tool to be stored
	 * @param qty - amount of tools of type tool to be stored
	 */
	public void addTool(Tool tool, int qty) {
		if (tools.containsKey(tool.getType())) {
			for (int i = 0; i < qty; i++) {
				tools.get(tool.getType()).incrementAndGet();
			}
		}
		else {
			tools.put(tool.getType(), new AtomicInteger(0));
			for (int i = 0; i < qty; i++) {
				tools.get(tool.getType()).incrementAndGet();
			}
		}
	}
}
