package bgu.spl.a2.sim.tasks;

import java.util.ArrayList;
import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
/**
 * 
 * A task class for production of a single product (also used for production of parts)
 *
 */

public class ProductTask extends Task<Product>{

	private ManufactoringPlan plan;
	private String productName;
	private long id;
	private Warehouse warehouse;
	private WorkStealingThreadPool pool;

	/**
	 * ProductTask Constructor
	 * 
	 * @param product - The specific product string
	 * @param qty - number of units to manufacture
	 * @param startId - starting id
	 * @param warehouse - the source of the plans and tools to use
	 * @param pool - thread pool to use
	 */
	public ProductTask(String product, long startId, Warehouse warehouse, WorkStealingThreadPool pool) {
		this.id = startId;
		this.warehouse = warehouse;
		this.plan = warehouse.getPlan(product);
		this.productName = product;
		this.pool = pool;
	}


	@Override
	protected void start() {
		//Note: if the product has no parts or no tools, the final id of the product is its start id
		Product product = new Product(id, productName);
		int numOfParts = plan.getParts().length;
		if (numOfParts > 0){
			ArrayList<Task<Product>> partsArr = new ArrayList<>();
			for (int i = 0 ; i < numOfParts ; i++){
				ProductTask part = new ProductTask(plan.getParts()[i], id + 1, warehouse, pool);
				partsArr.add(part);
				spawn(part);
			}
			whenResolved(partsArr, () -> { 
				for (int n = 0 ; n < plan.getParts().length ; n++) //adding the parts to the product's part list
					product.addPart(partsArr.get(n).getResult().get());
				if (plan.getTools().length > 0){
					ToolTask useToolOnProduct = new ToolTask(plan, product, warehouse); // task to use all the required tools on all the product's parts
					pool.submit(useToolOnProduct);
					useToolOnProduct.getResult().whenResolved(() -> {
						complete(product);
					});
				}
				else {
					complete(product);
				}
			});
		}
		else { 
			complete(product);
		}
	}
}
