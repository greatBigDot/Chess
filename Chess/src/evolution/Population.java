package evolution;

import java.util.Random;

import permutation.Permutation;

public class Population {
	final static int POP_SIZE = 1000;
	private final static int NUM_ROUNDS = 1;    //Increasing this may force me to create of a faceoff seed scheme. TODO: Figure that out
	private final static double REDUNDANCY = 10.0/9.0;
	private final static double MUTATE_PROB = 0.01;
	
	Genome[] members;
	
	private final long metaSeed;
	private final Random randCodons;
	private long seeds[] = new long[POP_SIZE];
	
	private final long matingSeed;
	private final Random randMatings;
	private int[] matingKeys = new int[(int) Math.ceil(REDUNDANCY * POP_SIZE)];    //Not sure if this is right
	
	private final long crossoverMetaSeed;
	private final Random randMetaCrossovers;	
	private long[] crossoverSeeds = new long[POP_SIZE];
	
	private final long mutationSeed;
	private final Random randMutations;
	private int[] mutationKeys = new int[(int) MUTATE_PROB*POP_SIZE*Genome.LENGTH];
	
	public Population(long metaSeed,long matingSeed,long crossoverMetaSeed,long mutationSeed){
		this.metaSeed = metaSeed;
		randCodons = new Random(metaSeed);
		
		this.matingSeed = matingSeed;
		randMatings = new Random(matingSeed);
		
		this.crossoverMetaSeed = crossoverMetaSeed;
		randMetaCrossovers = new Random(crossoverMetaSeed);
		
		this.mutationSeed = mutationSeed;
		randMutations = new Random(mutationSeed);
	}
	
	public void initialize(){
		seeds = randCodons.longs(seeds.length).toArray();
		matingKeys = randMatings.ints(matingKeys.length, 0, POP_SIZE-1).toArray();
		crossoverSeeds = randMetaCrossovers.longs(crossoverSeeds.length).toArray();
		mutationKeys = randMutations.ints(mutationKeys.length,-1*POP_SIZE*Genome.LENGTH,POP_SIZE*Genome.LENGTH).toArray();

		for(int i = 0;i<=POP_SIZE-1;i++){
			members[i] = new Genome(seeds[i]);
			members[i].initialize();
		}
	}
	
	public void select(){
		Genome[] competitors = new Genome[2];
		//The increment works; trust me! At least, it does if POP_SIZE = 1000...
		for(int i=0;i<=POP_SIZE-1;i += 2*(i%2)+1){
			competitors[0] = members[i];competitors[1] = members[i+2];
			Genome.select(competitors);
		}
	}
	
	public void reproduce(){
		Genome[] parents = new Genome[2];
		//First, randomly re-order the population (see permutation.Permutation.java)
		int[] matingOrder = Permutation.permute(0, POP_SIZE-1, matingKeys);
		Genome[] permutation = new Genome[POP_SIZE];
		Genome[] nextGen = new Genome[POP_SIZE];
		
		for(int i=0;i<=POP_SIZE-1;i++){
			permutation[i] = members[matingOrder[i]];
		}
		
		//Second, use said ordering to reproduce randomly.
		for(int i=0;i<=POP_SIZE-1;i+=2){
			//Don't forget: each pair of parents has two kids
			parents[0]=permutation[i]; parents[1]=permutation[i+1];
			nextGen[i] = Genome.reproduce(parents,crossoverSeeds[i]);
			nextGen[i+1] = Genome.reproduce(parents,crossoverSeeds[i+1]);
		}
		
		//Third, mutate.
		int mKey;
		for(int i=0;i<=mutationKeys.length-1;i++){
			mKey=mutationKeys[i];
			nextGen[(int) Math.floor(mKey/Genome.LENGTH)].mutate(mKey%Genome.LENGTH,(int) Math.signum(mKey));
		}
		
		
	}
}