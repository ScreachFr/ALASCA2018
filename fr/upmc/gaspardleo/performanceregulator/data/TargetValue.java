package fr.upmc.gaspardleo.performanceregulator.data;

/**
 * La classe <code> TargetValue </ code> implémente le comportement
 * pour la valeur cible d'attente des requêtes utilisée dans les stratégies de régulation.
 * 
 * @author Leonor & Alexandre
 */
public class TargetValue {
	
	/** Valeur maximum de la cible */
	private Double upperBound;
	/** Valeur minimum de la cible */
	private Double lowerBound;
	
	/**
	 * @param upperBound	Valeur maximum de la cible.
	 * @param lowerBound	Valeur minimum de la cible.
	 */
	public TargetValue(Double upperBound, Double lowerBound) {
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}
	
	/**
	 * @return La valeur maximum de la cible.
	 */
	public Double getLowerBound() {
		return lowerBound;
	}
	
	/**
	 * @return La valeur minimum de la cible.
	 */
	public Double getUpperBound() {
		return upperBound;
	}
	
	@Override
	public String toString() {
		return "TargetValue[upper : " + upperBound + ", lower : " + lowerBound + "]";
	}
}
