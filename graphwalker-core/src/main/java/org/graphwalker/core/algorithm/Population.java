package org.graphwalker.core.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.GeneticAlgorithmGenerator.TestContext;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Edge.RuntimeEdge;
import org.graphwalker.core.model.Model.RuntimeModel;
import org.graphwalker.core.model.Vertex.RuntimeVertex;
import org.graphwalker.core.statistics.Profiler;
import org.graphwalker.core.statistics.SimpleProfiler;



public class Population implements Algorithm {

	private final RuntimeModel model;
	DNA[] population;
	double mutationRate;
	String best;
	String target;
	int generations;
	int popMax;
	boolean isFinished;
	ArrayList<DNA> matingPool;

	public Population(Context context) {
		this.model = context.getModel();
		this.generations = 0;
		this.best = "";
		this.matingPool = new ArrayList<DNA>();
		this.isFinished = false;
	}

	public void setPopulationParameters(String target, double mutationRate, int popMax) {
		this.mutationRate = mutationRate;
		this.target = target;
		this.popMax=popMax;
		this.population = new DNA[popMax];
		for (int i = 0; i < popMax; i++) {
			this.population[i] = new DNA(target.length());
		}
	}
	
	public void generateFirstPopulation(Context context,StopCondition condition) {
		
		//RuntimeModel model = context.getModel();
		
		List<Element> elements = context.filter(model.getElements());
		PathGenerator pathGenerator = new RandomPath(condition);
		// Generate 10 random paths
		for (int i = 0; i < 10; i++) {
			// RuntimeModel modelCopy=new RuntimeModel(model);
			Context testContext = new TestContext(model, pathGenerator);
			Profiler profiler = new SimpleProfiler();
			System.out.println("Iteration number=" + i + " Element size:" + elements.size());
			for (int j = 0; j < elements.size(); j++) {
				System.out.println("Element name=" + elements.get(j).getName());
			}
			profiler.addContext(testContext);
			testContext.setNextElement(elements.get(0));
			// create a machine and execute the test
			Machine machine = new SimpleMachine(testContext);
			while (machine.hasNextStep()) {
				machine.getNextStep();
			}
			System.out.println("Total Visit:" + profiler.getTotalVisitCount());
		}
	}
	
	// needs to be public, because it will be instantiated from graphwalker
		public class TestContext extends ExecutionContext {

			public TestContext(RuntimeModel model, PathGenerator pathGenerator) {
				super(model, pathGenerator);
			}

			public void myTestMethod() {
				// This is executed during the model execution
			}
		}
	
	
	
	public void calcFitness() {

		for (int i = 0; i < population.length; i++) {
			this.population[i].calcFitness(this.target);
		}

	}

	public void naturalSelection() {

		this.matingPool.clear();

		double bestFitnessScore = 0;
		for (int i = 0; i < population.length; i++) {
			if (bestFitnessScore < this.population[i].fitness) {
				bestFitnessScore = this.population[i].fitness;
				this.best = String.valueOf(this.population[i].genes);
			}
		}

		// Creating Mating Pool
		for (int i = 0; i < population.length; i++) {

			double fitness = map(this.population[i].fitness, 0, bestFitnessScore, 0, 1);
			// System.out.println("fitness:"+fitness);
			int n = (int) (fitness * 100);
			// System.out.println("n:"+n);
			for (int j = 0; j < n; j++) {
				this.matingPool.add(this.population[i]);
			}
		}

		System.out.println("Mating Pool Size:" + matingPool.size());

	}

	public void generate() {
		for (int i = 0; i < this.population.length; i++) {
			int a = (int) (Math.random() * (this.matingPool.size()));
			int b = (int) (Math.random() * (this.matingPool.size()));
			DNA partnerA = this.matingPool.get(a);
			DNA partnerB = this.matingPool.get(b);
			DNA child = partnerA.crossover(partnerB);
			child.mutation(this.mutationRate);
			this.population[i] = child;
		}
		this.generations++;
	}

	private double map(double fitness, double min, double max, double minMap, double maxMap) {
		double a = (fitness - min) / (max - min);
		double result = a * (max - min) + min;
		return result;
	}

	public boolean isFinished() {
		for (int i = 0; i < this.population.length; i++) {
			if (String.valueOf(this.population[i].genes).equals(target)) {
				this.isFinished = true;
				this.best = String.valueOf(this.population[i].genes);
				this.generations++;
				this.report();
				return true;
			}
		}

		return false;
	}

	public void report() {
		System.out.println("Generation:" + this.generations);
		System.out.println("Best=" + this.best);
	}
}
