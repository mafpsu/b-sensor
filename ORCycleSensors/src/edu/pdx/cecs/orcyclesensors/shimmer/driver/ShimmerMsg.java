package edu.pdx.cecs.orcyclesensors.shimmer.driver;

public class ShimmerMsg {
	public int mIdentifier;
	public Object mB;
	
	public ShimmerMsg(int a, Object b){
		mIdentifier=a;
		mB=b;
	}
}
