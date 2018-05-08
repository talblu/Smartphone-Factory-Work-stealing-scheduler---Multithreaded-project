package bgu.spl.a2.sim.tools;

import java.math.BigInteger;

import bgu.spl.a2.sim.Product;

public class GCDScrewdriver implements Tool {
	private String type;

	public GCDScrewdriver(){
		type = "gs-driver";
	}

	@Override
	public String getType() {
		return type;
	}
	
	public long reverse(long n){
		long reverse=0;
		while( n != 0 ){
			reverse = reverse * 10;
			reverse = reverse + n%10;
			n = n/10;
		}
		return reverse;
	}
	public long useOn(Product p){
		long res = 0;
		for (int i = 0 ; i < p.getParts().size(); i++){
			BigInteger b1 = BigInteger.valueOf(p.getParts().get(i).getFinalId().get());
			BigInteger b2 = BigInteger.valueOf(reverse(p.getParts().get(i).getFinalId().get()));
			BigInteger value = (b1.gcd(b2));
			res += Math.abs(value.longValue());
		}
		return res;
	}
}
