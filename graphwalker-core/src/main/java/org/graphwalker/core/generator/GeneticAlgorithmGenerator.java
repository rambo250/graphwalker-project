package org.graphwalker.core.generator;

import org.graphwalker.core.algorithm.Population;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Model.RuntimeModel;
import org.graphwalker.core.statistics.Profiler;
import org.graphwalker.core.statistics.SimpleProfiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <h1>Genetic Algorithm Generator</h1> The Genetic Algorithm generator will use
 * Genetic algorithm to create a path through a model.
 * </p>
 * Genetic algorithm will be used for finding a optimal path for a big models.
 * </p>
 *
 * @author Ramazan Koc & Onur Enginer
 */

public class GeneticAlgorithmGenerator extends PathGeneratorBase<StopCondition> {

	private static final Logger LOG = LoggerFactory.getLogger(GeneticAlgorithmGenerator.class);

	private static boolean firstIteration = true;
	
	private static Population population;

	public GeneticAlgorithmGenerator(StopCondition stopCondition) {
		setStopCondition(stopCondition);

	}

	@Override
	public Context getNextStep() {
		Context context = super.getNextStep();
		
		//genetic algorithm calıscak
		if (firstIteration) {
			firstIteration = false;
			//Getting population algorithm to use
			population=context.getAlgorithm(Population.class);
			population.setPopulationParameters("Test", 0.01, 10);
			population.generateFirstPopulation(super.getContext(),this.getStopCondition());
			//firstGeneration();
		}
		
		Element currentElement = context.getCurrentElement();
		System.out.println("Next Step | Current Element:" + currentElement.getName());
		List<Element> elements = context.filter(context.getModel().getElements(currentElement));

		System.out.println("Next Step | Element size:" + elements.size());
		for (int j = 0; j < elements.size(); j++) {
			System.out.println("Element name=" + elements.get(j).getName());
		}

		if (elements.isEmpty()) {
			LOG.error("currentElement: " + currentElement);
			LOG.error("context.getModel().getElements(): " + context.getModel().getElements());
			throw new NoPathFoundException(context.getCurrentElement());
		}
		context.setCurrentElement(elements.get(SingletonRandomGenerator.nextInt(elements.size())));
		return context;
	}

	@Override
	public boolean hasNextStep() {
		return !getStopCondition().isFulfilled();
	}

	public void firstGeneration() {
		Context context = super.getContext();
		RuntimeModel model = context.getModel();
		List<Element> elements = context.filter(context.getModel().getElements());

		StopCondition condition = this.getStopCondition();
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
			System.out.println("Time:" + profiler.getTotalExecutionTime());
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

}
