package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

public class NextPrimeHammer implements Tool {
	private String type;

	public NextPrimeHammer(){
		type = "np-hammer";
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public long useOn(Product p) {
		long res = 0;
		for (int i = 0 ; i < p.getParts().size(); i++){ 
			long v = p.getParts().get(i).getFinalId().get() + 1;
			while (!isPrime(v)) {
				v++;
			}
			res += Math.abs(v);
		}
		return res;
	}

	private boolean isPrime(long value) {
		long sq = (long) Math.sqrt(value);
		for (long i = 2; i < sq; i++) {
			if (value % i == 0) {
				return false;
			}
		}
		return true;
	}
}