package chess;

import java.util.Random;

public class Genome {
	//Total number of weights per genome: 64^2 + 64*(64*2) = 12288
	
	//TODO: Add castling! It'll mess up all the numbers: remember to use variables instead of hardcoded constants!
	final static int LENGTH = 12288;
	long seed;
	double[][][] weights = new double[64][64][2];
	
//	public Genome(double[][][] weights){
//		//This is mostly for debugging purposes.
//		//TODO: Make exception or something for a weight parameter with improper dimensions. 
//		this.weights = weights;
//		//TODO: Consider setting seed here (but make sure it is something the seed could never be, like 0 or -1 or something)
//	}
	public Genome(long seed){
		this.seed = seed;
		Random random = new Random(seed);
		/* While the lower bound is inclusive, the upper bound is exclusive. 
		 * It's annoying, but it shouldn't matter; if it does, I'll make 
		 * it (-2,2 + Double.MIN_VALUE) or whatever.
		 */
		double[] oneDimWeights = random.doubles(LENGTH,-2,2).toArray();
		//TODO: There's got to be a more elegant way of doing this.
		int count=0;
		for(int i=0;i<=63;i++){
			for(int j=0;j<=63;j++){
				//The order makes sense if you think about it. Trust me.
				weights[j][i][0] = oneDimWeights[count];
				count++;
			}
		}
		for(int i=0;i<=127;i++){
			for(int j=0;j<=63;j++){
				weights[j][i][1] = oneDimWeights[count];
				count++;
			}
		}
		if(count != LENGTH){
			System.out.println("SOMETHING WENT HORRIBLY WRONG");  //maybe I should learn how to do exceptions...
		}
	}
	
	/*
	 * There are three stages to the genetic algorithm:
	 * 
	 * * Initialization: a sample population of thousands is randomly produced.
	 * * Selection: a function maps each genome to a number from 0 to 1 
	 *       representing how fit it is. I may limit it to 0, 0.5, and 1 in this 
	 *       case--TODO: decide what to do about that.
	 *  * Reproduction: use genetic operators (e.g., crossover and mutation) to
	 *       produce next generation.
	 * 
	 * These three are defined on an individual basis here. Those definitions
	 * are then used to define the population-wide definitions in the Population
	 * class. For example, the "select(Genome opponent)" method here simulates a
	 * game between this and opponent, then gives a fitness score to this. (I 
	 * made it instance instead of static to make it easier to generalize to an
	 * arbitrary genetic algorithm. See, I can think ahead!) In Population.java,
	 * the method select() is defined that randomly pairs every genome in the
	 * population together, then runs each pair through the select method here.
	 * It works analogously in the other two steps. Also, note that while
	 * selection and reproduction are methods, initialization is done in the
	 * constructor, both in this class and in Population.
	 * 
	 * With all of the (pseudo)randomness here, it can be hard to keep track of
	 * what all the randomly generated numbers are. Here's an overview of all of
	 * the random numbers and the seeds that produce them in both classes
	 * (excuse the horrible naming--I'll refactor once I get everything
	 * straightened out).
	 * 
	 * * metaSeed--[init] instance field of Population. This exists so that each
	 *   population is completely--but deterministically--different. There is
	 *   one of these per initial population. It produces popSize~=1000 longs
	 *   from the Long.MIN_LENGTH to Long.MAX_LENGTH:
	 *     * seed--an instance field of Genome. It produces LENGTH=12,288
	 *       doubles from -2 to +2:
	 *         * weights--the weights of the neural network.  See the selection
	 *           methods for more details on how the net works. Note that they 
	 *           are packed into a three dimensional array that is one of the 
	 *           instance fields of Genome. Also note that the weights are also
	 *           called codons throughout this documentation. There are
	 *           ~=12,288,000 per initial population.
	 * 
	 * * matingSeed--[rep] an instance field of Population. It splits the gene
	 *   pool into two equal-sized random ordered sets in order to pair them up
	 *   randomly. I haven't quite figured out how to use the seed to do this
	 *   yet, but I'll figure out. (idea: IVT like below, then just recurse to
	 *   remove extras(verify this is uniformly random)) (that has issues--to
	 *   avoid meta-meta-seeds, I need the order of the ~500 pairs to be
	 *   random (see below).) TODO: sort this out ASAP
	 *
	 * * metaCrossoverSeed--[rep] an instance field of Population. Note that
	 *   though reproduction occurs every generation, there is only one of these
	 *   per population (I really don't want to have to deal with meta-meta-
	 *   seeds at this level). This should be okay so long as the previous
	 *   pairing process leaves them ordered randomly--AND orders them
	 *   differently every generation. That way, the top genome (for example)
	 *   won't have the first (of ~1000) crossoverSeed every time. I could be
	 *   wrong, so... TODO: Make sure that meta-meta-seeds are unnecessary in
	 *   crossover under this system--can I reuse the same meta-seed every
	 *   generation? If it does work, then TODO: see if the same logic can be
	 *   used to cut down on meta-seeds. This produces popSize ~= 1000 longs
	 *   from Long.MIN_LENGTH to Long.MAX_LENGTH:
	 *       * crossoverSeed--while it itself is not a field of any class, the 
	 *         array containing all of them is an instance field of Population.
	 *         Note that there are ~=1000 crossoverSeeds for each population and
	 *         not ~=500. This is because each parent produces two children to
	 *         keep the population stable. It produces LENGTH=12,288 doubles
	 *         from 0 to 1:
	 *             * crossoverKey--contained in a two dimensional array instance
	 *               field of Population. These are used to determine how
	 *               children are created from their parents. See the methods
	 *               for more details on reproduction itself, but essentially
	 *               each parent has a probability, p and q=1-p, which are
	 *               directly proportional to their respective fitnesses, which
	 *               represents the probability that a given codon will come
	 *               from each parent. To simulate this in a way that I can
	 *               repeat (with the magic of seeded pseudorandomness!), this 
	 *               double is created. It is uniformly distributed over [0,1]
	 *               (actually, it's [0,1) because of some unfathomable [shut 
	 *               up, eclipse's spell-check--that word is perfectly spelled!]
	 *               reasoning by Java's libraries' creators.) If 0 <= key <= p,
	 *               it goes with parent A's codon for that position. If
	 *               p<key<=1, parent B's. (Note: I think this is how inverse
	 *               transform sampling works, except obviously with uncountably
	 *               infinitely many values instead of two. Or maybe my 
	 *               intuition is off.)
	 *               
	 * * mutationSeed--[rep] this is to ensure that mutation produces ~=122,880
	 *   ints from ~=-12,288,000 to ~= 12,288,000.
	 *       * mutateKey--so the way I'm making this work is a total kludge.
	 *         ~=122,880 keys are produced--~=mutateProb of the total number of
	 *         codons in the population. The mutation is performed as follows:
	 *         the codon corresponding to the absolute value of a key is mutate
	 *         by mutateVal if the key is positive and -mutateVal if the key is 
	 *         negative. If the same codon is produced twice, it is mutated 
	 *         twice. If 0 is a key, nothing happens. If a codon is mutated
	 *         above 2 or below -2, it gets set to those bounds respectively.
	 *         "But wait!" you say. "That means that less than mutateProp
	 *         (~=12,288,000) codons may be mutated, but never more!" To that I
	 *         say: Shut up and make a better system. But seriously, it may be
	 *         fixable by taking mutateProp, shifting it upwards, then using
	 *         that to determine the number of mutated codons. I haven't done
	 *         the math, so I don't know how feasible that is. If it isn't, then
	 *         I'll just make a better way to do it (though it'll take up a good
	 *         chunk of memory and slow down the program). TODO: Can I fudge
	 *         this kludge to a proper solution? (Oh, so "fudge" isn't a word
	 *         now, eclipse? Really?)
	 *
	 *
	 * That's it. Trust me--it's not as bad as it looks. Note that there is no
	 * "faceOffKey" or seeds for it. That's because I'm just going to have 1
	 * face off against 3, 2 against 4, 5 v. 7, 6 v. 8, etc. This will still
	 * work because matingSeed will results in a randomly ordered population
	 * each generation. That is, by saying 1 always plays 3 in the first round,
	 * what I'm saying is the first child of 42 and 628 will play against the
	 * first child of 156 and 27. (These numbers are just examples--and,
	 * crucially, *they change every generation.* Note also that the first
	 * generation face-offs will also be random--each seed corresponds to a
	 * random unique gene pool at the beginning, so the numbered labels don't
	 * yet mean anything.)
	 *
	 * The only mechanism yet to be worked out is matingSeed. That matters a lot
	 * because that's what guarantees the faceOff keys and the
	 * meta-meta-crossover seed are unnecessary; if I can't get mating seed to
	 * work, the entire system will have to be reworked
	 * 
	 */

}