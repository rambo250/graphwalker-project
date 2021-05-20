package org.graphwalker.core.machine;

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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.graphwalker.core.condition.EdgeCoverage;
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.condition.StopConditionException;
import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.generator.SingletonRandomGenerator;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Guard;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.graphwalker.core.statistics.Execution;
import org.graphwalker.core.statistics.Profiler;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class SimpleMachineGATest {

	private Model model=null;
	private Vertex startElement=null;

	@SuppressWarnings("deprecation")
	
	public void multipleSimpleMachine() throws Exception {
		//Vertex vertex = new Vertex();
		//Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(new Vertex()));
		createTestModel();
		if(this.model==null) {
			System.out.println("Model not found");
		}

		//Machine 1
		Context context = new TestExecutionContext(this.model, new RandomPath(new VertexCoverage(100)));
		context.setNextElement(this.startElement);
		Machine machine = new SimpleMachine(context);
		while (machine.hasNextStep()) {
			machine.getNextStep();
			//Element currentElement = context.getCurrentElement();
			//System.out.println("Next Step | Current Element:" + currentElement.getName());
			assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
		}
		assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);

		Profiler testProfiler=machine.getProfiler();
		List<Execution> path = testProfiler.getExecutionPath();
		System.out.println("Machine 1 Statistics");
		printExecutionPath(path);
		printExecutionTime(testProfiler);

		//Machine 2
		context = new TestExecutionContext(model, new RandomPath(new VertexCoverage(100)));
		context.setNextElement(this.startElement);
		machine = new SimpleMachine(context);
		while (machine.hasNextStep()) {
			machine.getNextStep();
			assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
		}

		Profiler testProfiler2=machine.getProfiler();
		List<Execution> path2 = testProfiler2.getExecutionPath();
		System.out.println("Machine 2 Statistics");
		printExecutionPath(path2);
		printExecutionTime(testProfiler2);
		assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void createFirstGeneration() {
		int N=10;
		createTestModel();
		if(this.model==null) {
			System.out.println("Model not found");
		}
		if(this.startElement==null) {
			System.out.println("Model start element not found");
		}
		
		Context context;
		Machine machine;
		Profiler profiler;
		for(int i=0;i<N;i++) {
			context = new TestExecutionContext(this.model, new RandomPath(new ReachedVertex("d")));
			context.setNextElement(this.startElement);
			machine = new SimpleMachine(context);
			while (machine.hasNextStep()) {
				machine.getNextStep();
				assertThat(context.getExecutionStatus(), is(ExecutionStatus.EXECUTING));
			}
			assertNotEquals(context.getProfiler().getTotalVisitCount(), 0);
			profiler=machine.getProfiler();
			List<Execution> path = profiler.getExecutionPath();
			System.out.println("Machine "+i+" Statistics");
			printExecutionPath(path);
			printExecutionTime(profiler);
		}
	}


	private void printExecutionPath(List<Execution> path) {

		for(int i=0;i<path.size();i++) {
			Element currentElement=path.get(i).getElement();
			System.out.print(currentElement.getName()+" : ");
		}
		System.out.println("");
	}

	private void printExecutionTime(Profiler profiler) {
		long totalExecutionTime=profiler.getTotalExecutionTime(TimeUnit.NANOSECONDS);
		System.out.println("Total Execution Time: "+totalExecutionTime);
		System.out.println("Total Visits: "+profiler.getTotalVisitCount());
	}

	private void createTestModel() {
		Vertex start = new Vertex().setName("Start");

		Vertex a = new Vertex().setName("a");
		Vertex b1 = new Vertex().setName("b1");
		Vertex b2 = new Vertex().setName("b2");
		Vertex c = new Vertex().setName("c");
		Vertex d = new Vertex().setName("d");

		Edge e1 = new Edge().setName("S->a").setSourceVertex(start).setTargetVertex(a);
		Edge e2 = new Edge().setName("a->S").setSourceVertex(a).setTargetVertex(start);
		Edge e3 = new Edge().setName("S->b1").setSourceVertex(start).setTargetVertex(b1);
		Edge e4 = new Edge().setName("b1->S").setSourceVertex(b1).setTargetVertex(start);
		Edge e5 = new Edge().setName("b1->a").setSourceVertex(b1).setTargetVertex(a);
		Edge e6 = new Edge().setName("a->b1").setSourceVertex(a).setTargetVertex(b1);


		Edge e7 = new Edge().setName("b1->b2").setSourceVertex(b1).setTargetVertex(b2);
		Edge e8 = new Edge().setName("b2->b1").setSourceVertex(b2).setTargetVertex(b1);
		Edge e9 = new Edge().setName("b2->c").setSourceVertex(b2).setTargetVertex(c);
		Edge e10 = new Edge().setName("c->b2").setSourceVertex(c).setTargetVertex(b2);
		Edge e11 = new Edge().setName("c->d").setSourceVertex(c).setTargetVertex(d);


		Model model = new Model().addEdge(e1).addEdge(e2).addEdge(e3).addEdge(e4)
				.addEdge(e5).addEdge(e6).addEdge(e7).addEdge(e8).addEdge(e9)
				.addEdge(e10).addEdge(e11);

		this.startElement=start;
		this.model=model;
	}


}
