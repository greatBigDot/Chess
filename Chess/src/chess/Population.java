package chess;

import java.util.Random;

public class Population {
	Genome[] genomes;
	long[] seeds;
	int size;
	
	/* There are three stages to the genetic algorithm:
	 * * initialization: a sample population of thousands is produced.
	 * * selection: a function maps each genome to a number from 0 to 1 representing how fit it is. I may limit to 0, 0.5, and 1 in this case--TODO: decide what to do about that.   
	 * * reproduction: use genetic operators (e.g., crossover and mutation) to produce next generation.
	 */
	
	public static Genome[] initialize(int popSize){
		Genome[] population = new Genome[popSize];
		long metaSeed = new Random().nextLong();  //Ugh, this is so layered--and it's not written recursively. Future self, this is the bottom. Good luck figuring this out. It's what you get for programming away before you had it 100% planned out. 
		long[] seeds = seeds(metaSeed,popSize);  //seeds(long,int) is a private method in this class, by the way.
		
		for(int i=0;i<=popSize-1;i++){
			population[i] = new Genome(seeds[i]);
		}
		
		return population;
	}
	
	public static double[] select(Genome[] genome){
		/* This is the key. It's where it's decided who lives and who dies, it's where the neural
		 *  network actually does stuff, it's where the rules of chess come in...you get the
		 *  idea. When I make a general genome class and not a chess-specific one like this, I 
		 *  will almost certainly have to abstract this method.
		 */
		
		
		
		double[] shutUpEclipse = new double[0];
		return shutUpEclipse;
		
	}
	
	public static void reproduce(Genome[] genome){
		//for some reason random.booleans(long streamSize) isn't a thing, so this'll have to do.
		//new Random(repSeed
	}
	
	
	private static long[] seeds(long seedForSeeds, int numOfSeeds){
		// I really feel like this is an ad-hoc, overly layered, and inelegant way of doing it, but whatever. TODO: consider cleaning this up.
		return new Random(seedForSeeds).longs(numOfSeeds,Long.MIN_VALUE,Long.MAX_VALUE+1).toArray();  //Remember: the upper bound is exclusive.
	}
	
	
	
}