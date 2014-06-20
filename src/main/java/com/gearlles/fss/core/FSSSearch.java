package com.gearlles.fss.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSSSearch {
	
	private Logger logger = LoggerFactory.getLogger(FSSSearch.class);
	
	private List<Fish> school;
	private int dimensions;
	private int schoolSize;
	private double RANGE = 5.12;
	
	private Random rand = new Random();

	private double WEIGHT_SCALE;
	
	private double STEP_IND;
	private double FINAL_STEP_IND;
	private double INITIAL_STEP_IND; 
	
	private double STEP_VOL;
	private double FINAL_STEP_VOL;
	private double INITIAL_STEP_VOL;
	
	private double lastOverallWeight;
	private double bestFitness;
	private double maxIterations = 1000;
	
	public FSSSearch() {
		this.school = new ArrayList<Fish>();
		this.dimensions = 30;
		this.schoolSize = 30;
		this.lastOverallWeight = 0;
		this.bestFitness = Double.MAX_VALUE;
		
		this.INITIAL_STEP_IND = 1;
		this.STEP_IND = INITIAL_STEP_IND;
		this.FINAL_STEP_IND = .1;
		
		this.INITIAL_STEP_VOL = .05;
		this.STEP_VOL = INITIAL_STEP_VOL;
		this.FINAL_STEP_VOL = .005;
		
		this.WEIGHT_SCALE = 1000;
		
		initialize();
	}
	
	private void initialize() {
		for (int i = 0; i < schoolSize; i++) {
			double position[] = new double[dimensions];
			
			for (int j = 0; j < dimensions; j++) {
				position[j] = rand.nextDouble() * 2 * RANGE - RANGE;
			}
			
			Fish fish = new Fish();
			fish.setPosition(position);
			fish.setWeight(2500);
			
			school.add(fish);
		}
	}

	public double iterateOnce(int it) {
		
		double iterationBestFitness = Double.MAX_VALUE;
		
		for (int i = 0; i < school.size(); i++) {
			Fish fish = school.get(i);
			
			// 1. update position applying the individual operator
			double[] tempPosition = individualmovement(fish);
			double oldFitness = calculateFitness(fish.getPosition());
			double newFitness = calculateFitness(tempPosition);
			
			boolean insideAquarium = true;
			for (int j = 0; j < tempPosition.length; j++) {
				double d = tempPosition[j];
				if (d < -RANGE || d > RANGE) {
					insideAquarium = false;
				}
				
			}
			
			if ((newFitness < oldFitness) && insideAquarium) {
				double[] deltaPosition = new double[dimensions];
				for (int j = 0; j < dimensions; j++) {
					deltaPosition[j] = tempPosition[j] - fish.getPosition()[j];
				}
//				logger.debug(String.format("Fish %d: %s", i, fish.toString()));
				fish.setDeltaPosition(deltaPosition);
				fish.setDeltaFitness(newFitness - oldFitness);
				fish.setPosition(tempPosition);
				
				if (newFitness < iterationBestFitness) {
					iterationBestFitness = newFitness;
				}
				
			} else {
//				logger.debug(String.format("Fish %d: inside = %b, better fitness: %b", i, insideAquarium, newFitness < oldFitness));
				fish.setDeltaPosition(new double[dimensions]);
				fish.setDeltaFitness(0);
			}
		}
		
		STEP_IND -= (INITIAL_STEP_IND - FINAL_STEP_IND) / maxIterations;
		
		// 2. applying feeding operator
		for (int i = 0; i < school.size(); i++) {
			Fish fish = school.get(i);
			double newWeight = feedingOperator(fish);
			fish.setWeight(newWeight);
		}
		
		if (iterationBestFitness < bestFitness)
		{
			bestFitness = iterationBestFitness;
		}
		
		logger.debug(String.format("%f",bestFitness));
		
		double overallWeight = calculateOverallWeight();
		boolean overallWeightIncreased = overallWeight > lastOverallWeight;
		lastOverallWeight = overallWeight;
		
		// 3. applying collective-instinctive movement
		collectiveInstinctiveMovement();
		
		// 4. applying collective-volitive movement
		collectiveVolitiveMovement(overallWeightIncreased);
		
		return bestFitness;
	}
	
	private double[] individualmovement(Fish fish) {
		double[] newPosition = new double[dimensions];
		double[] oldPosition = fish.getPosition();
		double[] randArray = new double[dimensions];
		
		for (int i = 0; i < randArray.length; i++) {
			randArray[i] = rand.nextDouble() * 2 - 1; 
		}
		
		for (int i = 0; i < dimensions; i++) {
			newPosition[i] = oldPosition[i] + randArray[i] * STEP_IND;
		}
		
		return newPosition;
	}
	
	private double feedingOperator(Fish fish) {
		double newWeight = Double.MIN_VALUE;
		double oldWeight = fish.getWeight();
		double bestDeltaFitness = Double.MAX_VALUE;
		
		// looking for the max delta fitness in the school
		// TODO should we handle negatives values?
		for (int i = 0; i < school.size(); i++) {
			double deltaFitness = school.get(i).getDeltaFitness();
			if (deltaFitness < bestDeltaFitness) {
				bestDeltaFitness = deltaFitness;
			}
		}
		if (bestDeltaFitness != 0)  {
			newWeight = oldWeight + fish.getDeltaFitness() / bestDeltaFitness;
		} else {
			newWeight = oldWeight;
		}
		
		// limit the weight according to Carmelo, 2008.
		if (newWeight > WEIGHT_SCALE) {
			newWeight = WEIGHT_SCALE;
		}
		
		return newWeight;
	}
	
	private void collectiveInstinctiveMovement() {
		double[] m = new double[dimensions];
		double totalFitness = 0;
		
		// calculating m, the weighted average of individual movements 
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			
			for (int j = 0; j < dimensions; j++) {
				m[j] += _fish.getDeltaPosition()[j] * _fish.getDeltaFitness();
			}
			
			totalFitness += _fish.getDeltaFitness();
		}
		
		// avoid division by 0
		if (totalFitness == 0) {
			return;
		}
		
		for (int i = 0; i < dimensions; i++) {
			m[i] /= totalFitness;
		}
		
		// applying m
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			double[] schoolInstinctivePosition = new double[dimensions];
			
			for (int j = 0; j < dimensions; j++) {
				schoolInstinctivePosition[j] = _fish.getPosition()[j] + m[j];
				
				boolean collision = schoolInstinctivePosition[j] < -RANGE || schoolInstinctivePosition[j] > RANGE;
				if (collision)
				{
					schoolInstinctivePosition[j] = schoolInstinctivePosition[j] > 0 ? RANGE : - RANGE;
				}
			}
			
			_fish.setPosition(schoolInstinctivePosition);
		}
		
	}
	
	private void collectiveVolitiveMovement(boolean overallWeightIncreased) {
		double[] barycenter = new double[dimensions];
		double totalWeight = 0;
		
		// calculating barycenter
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			
			for (int j = 0; j < dimensions; j++) {
				barycenter[j] += _fish.getPosition()[j] * _fish.getWeight();
			}
			
			totalWeight += _fish.getWeight();
		}
		
		for (int i = 0; i < dimensions; i++) {
			barycenter[i] /= totalWeight;
		}
		
		// applying barycenter
		for (int i = 0; i < school.size(); i++) {
			Fish _fish = school.get(i);
			double[] schoolVolitivePosition = new double[dimensions];
			
			for (int j = 0; j < dimensions; j++) {
				double product = STEP_VOL * rand.nextDouble() * (_fish.getPosition()[j] - barycenter[j]);
				if (!overallWeightIncreased) product *= -1;
				schoolVolitivePosition[j] = _fish.getPosition()[j] + product;
				
				boolean collision = schoolVolitivePosition[j] < -RANGE || schoolVolitivePosition[j] > RANGE;
				if (collision)
				{
					schoolVolitivePosition[j] = schoolVolitivePosition[j] > 0 ? RANGE : - RANGE;
				}
			}
			
			_fish.setPosition(schoolVolitivePosition);
		}
		
		STEP_VOL -= (INITIAL_STEP_VOL - FINAL_STEP_VOL) / maxIterations;
	}
	
	private double calculateOverallWeight() {
		double overallWeight = 0;
		for (int i = 0; i < school.size(); i++) {
			overallWeight += school.get(i).getWeight();
		}
		return overallWeight;
	}
	
	private double calculateFitness(double[] inputs) {
		double res = 10 * inputs.length;
		for (int i = 0; i < inputs.length; i++)
			res += inputs[i] * inputs[i] - 10
					* Math.cos(2 * Math.PI * inputs[i]);
		return res;
	}
	
	private double calculateFitnessa(double[] inputs) {
		double res = 0;
		for (int i = 0; i < inputs.length; i++)
			res += Math.pow(inputs[i], 2);
		return res;
	}

	public List<Fish> getSchool() {
		return this.school;
	}
	
	public static void main(String[] args) {
		int iterations = 1000;
		
		double[] best = new double[iterations];
		
		for (int i = 0; i < 30; i++) {
			FSSSearch s = new FSSSearch();
			for (int j = 0; j < best.length; j++) {
				best[j] += s.iterateOnce(j);
			}
		}
		
		for (int i = 0; i < best.length; i++) {
			best[i] /= 30;
		}
		
		XYSeries series = new XYSeries("Fitness");
		for (int i = 0; i < iterations; i++) {
			series.add(i, best[i]);
		}
		
		
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart("Fish School Search - Rastrigin function (#1)", // Title
				"Iteration", // x-axis Label
				"Best Fitness", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		try {
			ChartUtilities.saveChartAsJPEG(new File("C:\\Users\\Gearlles\\Desktop\\chart_fss.jpg"), chart, 500, 300);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double getRANGE() {
		return RANGE;
	}
}
