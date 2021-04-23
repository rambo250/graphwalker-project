package org.graphwalker.core.algorithm;



public class DNA {

	public char[] genes;
	public double fitness;
	int length;
	
	public DNA(int length) {
		this.length=length;
		this.fitness=0;
		this.genes = new char[length];
		
		for(int i=0;i<length;i++) {
			this.genes[i]=newChar();
		}
		//System.out.println("DNA created:"+String.valueOf(genes));
	}
	
	//returns fitness score between 0 and 1
	public double calcFitness(String target) {
		int score=0;
		for(int i=0;i<target.length();i++) {
			if(genes[i]==target.charAt(i)) {
				score++;
			}
		}
		this.fitness=(double)score/target.length();
		//System.out.println("Score calculated:"+(double)score/target.length());
		return fitness;
	}
	
	public DNA crossover(DNA partnerB) {
		DNA child=new DNA(this.length);
		
		int midpoint=(int)(Math.random()*(this.length));
		//System.out.println("Midpoint:"+midpoint+" / "+this.length);
		for(int i=0;i<this.length;i++) {
			if(i<midpoint) {
				child.genes[i]=this.genes[i];
			}
			else {
				child.genes[i]=partnerB.genes[i];
			}
		}
		
		return child;
	}
	
	public void mutation(double rate) {
		for(int i=0;i<this.length;i++) {
			if(Math.random()<rate) {
				this.genes[i]=newChar();
			}
		}
	}
	
	private char newChar() {
		int max=122;
		int min=63;
		//System.out.println("X "+ (int)(Math.random()*(max-min+1)));
		int val=((int)(Math.random()*(max-min+1))+min);
		if (val==64) { //Change @ sign with space character
			val=32;
		}
		//System.out.println("Char="+(char)val+" "+val );
		return (char)val;
	}
	
}
