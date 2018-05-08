package bgu.spl.a2.sim.tools;

import java.util.Random;

import bgu.spl.a2.sim.Product;

public class RandomSumPliers implements Tool {
	private String type;

	public RandomSumPliers() {
		type = "rs-pliers";
	}

	@Override
	public String getType() {
		return type;
	}

	public long useOn(Product p){
		long res = 0;
		for (int i = 0 ; i < p.getParts().size() ; i++) {
			Random r = new Random(p.getParts().get(i).getFinalId().get());
			long  sum = 0;
			for (long j = 0; j < p.getParts().get(i).getFinalId().get() % 10000; j++) {
				sum += r.nextInt();
			}
			res += Math.abs(sum);
		}
		return res;
	}
}