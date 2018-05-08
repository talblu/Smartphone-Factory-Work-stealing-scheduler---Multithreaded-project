package bgu.spl.a2.sim.tasks;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A task to request and use all the required tools on all the product's part according to the product's plan
 * If a product has no tools or no parts, its final id value won't be changed
 */

public class ToolTask extends Task<Product>{
	private Product product;
	private Warehouse warehouse;
	private ManufactoringPlan plan;

	/**
	 * ToolTask constructor
	 * 
	 * @param plan - the source to get the list of tools to use
	 * @param product - the product to use the tools on
	 * @param warehouse - the warehouse to get the tools from, and eventually release the tools to
	 */
	public ToolTask(ManufactoringPlan plan, Product product, Warehouse warehouse) {
		this.plan = plan;
		this.product = product;
		this.warehouse = warehouse;
	}

	@Override
	protected void start() {
		AtomicInteger counter = new AtomicInteger(0);
		for (int i = 0 ; i < plan.getTools().length ; i++) {
			Deferred<Tool> acquiredTool = warehouse.acquireTool(plan.getTools()[i]);
			if (acquiredTool.isResolved()){
				product.setFinalId(acquiredTool.get().useOn(product));
				if (counter.incrementAndGet() == plan.getTools().length)
					complete(product);
				warehouse.releaseTool(acquiredTool.get());
			}
			else {
				acquiredTool.whenResolved(() -> {
					product.setFinalId(acquiredTool.get().useOn(product));
					warehouse.releaseTool(acquiredTool.get());
					if (counter.incrementAndGet() == plan.getTools().length)
						complete(product);
					
				});
			}
		}
	}
}