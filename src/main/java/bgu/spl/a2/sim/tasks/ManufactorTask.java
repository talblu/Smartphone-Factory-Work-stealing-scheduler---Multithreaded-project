package bgu.spl.a2.sim.tasks;

import java.util.ArrayList;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;

/**
 * A manufacturing Task of a specific product with a certain amount of units
 *
 */
public class ManufactorTask extends Task<Product[]>{

	private String product;
	private int qty;
	private long startId;
	private Warehouse warehouse;
	private WorkStealingThreadPool pool;

	/**
	 * ManufactorTask Constructor
	 * 
	 * @param product - The specific product string
	 * @param qty - number of units to manufacture
	 * @param id - starting id
	 * @param warehouse - the source of the plans and tools to use
	 * @param pool - thread pool to use
	 */
	public ManufactorTask(String product, int qty, long id, Warehouse warehouse, WorkStealingThreadPool pool){
		this.product = product;
		this.qty = qty;
		this.startId = id;
		this.warehouse = warehouse;
		this.pool = pool;
	}

	@Override
	protected void start() {
		ArrayList<Task<Product>> taskList = new ArrayList<>();
		for (int i = 0 ; i < qty ; i++){
			ProductTask prdct = new ProductTask(product, startId + i, warehouse, pool);
			taskList.add(prdct);
			spawn(prdct);
		}
		whenResolved(taskList, () -> { //All the units are completed
			Product[] finalProducts = new Product[taskList.size()];
			for (int i = 0 ; i < taskList.size() ; i++){
				finalProducts[i] = taskList.get(i).getResult().get();
			}
			complete(finalProducts);
		});
	}
}
