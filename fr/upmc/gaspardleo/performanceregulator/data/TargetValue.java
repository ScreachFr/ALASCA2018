package fr.upmc.gaspardleo.performanceregulator.data;

public class TargetValue {
	private Double upperBound;
	private Double lowerBound;
	
	
	public TargetValue(Double upperBound, Double lowerBound) {
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}
	
	
	public Double getLowerBound() {
		return lowerBound;
	}
	
	public Double getUpperBound() {
		return upperBound;
	}
	
	@Override
	public String toString() {
		return "TargetValue[upper : " + upperBound + ", lower : " + lowerBound + "]";
	}
}
