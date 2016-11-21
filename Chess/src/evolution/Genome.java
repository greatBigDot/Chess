package evolution;

import java.util.Random;

public class Genome {
	/**
	 * LAYER_k: The number of boxes in layer k.
	 */
	private final static int LAYER_0 = 66, LAYER_1 = 98, LAYER_2 = 129;
	private final static double MUTATE_VAL = 0.05;

	
	/**
	 * The number of codons in the genome.
	 */
	final static int LENGTH = LAYER_0*LAYER_1 + LAYER_1*LAYER_2;
	
	/**
	 * The number of games each genome plays in the select stage.
	 */
	final static int NUM_ROUNDS = 20;
	
	/**
	 * Counts how many games the genome has played so far.
	 */
	private int count;
	
	/**
	 * Measures how good the neural network is at playing chess.
	 */
	private double fitness;
	
	/**
	 * The seed used to randomly determine the values of the weights.
	 */
	long seed;
	
	/**
	 * Randomly generates, using seed, the codons.
	 */
	private Random random;
	
	/**
	 * The array containing all LENGTH of the weights of the neural network.
	 */
	double[] codons = new double[LENGTH];
	
	//private final NeuralNetwork nerualNet;
	
	
	
	/**
	 * A constructor to create a random genome; note that the initialization
	 * stage of the genetic algorithm takes place in a seperate method, not
     * the constructor.
     *
	 * @param seed	Used to initialize seed.
	 */
	public Genome(long seed){
		//neuralNet = new NeuralNetwork(this);
		this.seed = seed;
		random = new Random(seed);
		fitness=0;count=0;
	}
	
	public Genome(double[] codons){
		this.codons = codons;
	}
	
	
	/**
	 * An instance method that randomly creates the weights of the genome from
	 * its seed.
	 */
	public void initialize(){
		codons = random.doubles(LENGTH, -2, 2).toArray();
	}
	
	/**
	 * The part that does everything.
	 * 
	 * @param competitors	competitors.length should always be two.
	 */
	public static void select(Genome[] competitors){
		/* double result = NeuralNetwork.play(competitors[0].getNet,competitors[1].getNet);
		 * competitors[0].adjustFitness(result);
		 * competitors[1].adjustFitness(1-result);
		 */
	}

	/* private NeuralNet getNet(){
	 *     return neuralNet;
	 * }
	 */
	
	public static Genome reproduce(Genome[] parents, long crossoverSeed){
		Random randCrossover = new Random(crossoverSeed);
		double[] crossoverKeys = randCrossover.doubles(LENGTH, 0, 1).toArray();
		
		double k = 1.0/(parents[0].fitness + parents[1].fitness);
		double adjFitness = k*parents[0].fitness;
		double[] codons = new double[LENGTH];
		for(int i=0;i<=LENGTH-1;i++){
			if(crossoverKeys[i]<=adjFitness){
				codons[i] = parents[0].codons[i];
			} else {
				codons[i] = parents[1].codons[i];
			}
		}
		Genome child = new Genome(codons);
		return child;
	}
	
	public void mutate(int codonPos,int dir){
		//dir should always be -1, 0, or 1.
		this.codons[codonPos]+= MUTATE_VAL*dir;
	}
	/**
	 * 
	 * @param adj
	 */
	private void adjustFitness(double adj){
		/* If Population.select is written right, this method won't be called if
		 * isFitnessFinal() returns true, but just in case.... There should
		 * probably be an exception here instead.
		 */
		if(!isFitnessFinal()){
			fitness *= count;
			fitness += adj;;
			fitness /= (count+1);
			count+=1;
		}
	}
	
	protected boolean isFitnessFinal(){
		//== should be sufficient, but I'm sure there are bugs here.
		return count >= NUM_ROUNDS;
	}
	
	/**
	 * ****DOCUMENTATION****
	 * 
	 * I. EVOLUTION--
	 * 
	 * There are three stages to the genetic algorithm:
	 * 
	 * * Initialization--a sample population of thousands is randomly produced.
	 * This occurs once, at the very start of the program. A given population is
	 * ultimately determined by metaSeed.
	 * 
	 * * Selection--a game is simulated between several random pairs of genomes.
	 * (This, therefore, is where the neural network comes in.) After each game,
	 * each player in the pair has their fitness updated. After NUM_ROUNDS
	 * games, the fitness is finalized. The fitness is a double from 0 to 1
	 * representing how fit it is.
	 * 
	 * * Reproduction: use genetic operators (e.g., crossover and mutation) to
	 * produce next generation.
	 *
	 *
	 * These three are defined on an individual basis here. Those definitions
	 * are then used to define the population-wide definitions in the Population
	 * class. For example, the "select(Genome genomes[])" method here simulates
	 * a game between Genome[0] and Genome[1], then gives a fitness score to
	 * each. (I made them arrays instead of just having two inputs so that it
	 * would be easier to abstract to an arbitrary genetic algorithm.) In
	 * Population.java, a method select() is defined that runs each pair of
	 * genomes (these pairs are guaranteed to be random by last generation's
	 * reproduction step) through the select method here. It works analogously
	 * in the other two steps.
	 *
	 *
	 * With all of the (pseudo)randomness here, it can be hard to keep track of
	 * what all the randomly generated numbers are. Here's an overview of all of
	 * the random numbers and the seeds that produce them in both classes
	 * (excuse the horrible naming--I'll refactor once I get everything
	 * straightened out).
	 * 
	 * 
	 * * megaSeed[all]--this is the seed whose value alone determines a
	 * population and its future evolution. Since it only produces four
	 * (meta)seeds, it isn't really necessary, but it is convenient to wrap
	 * everything up in one number. The four seeds are: metaSeed, which
	 * determines the codons making up the initial population; matingMetaSeed,
	 * which determines who mates with whom; crossoverMetaSee, which
	 * determines how exactly two parent genes produce their two children; and
	 * mutationSeed, which randomly mutates some of the codons of the
	 * resulting children. There is precisely one of these for every complete
	 * evolutionary history. It produces 4 longs from Long.MIN_LENGTH to
	 * Long.MAX_LENGTH:
	 * 
	 *     * metaSeed--[init] instance field of Population. This exists so that
	 *     each population is completely--but deterministically--different.
	 *     There is one of these per initial population. It produces
	 *     popSize~=1000 longs from the Long.MIN_LENGTH to Long.MAX_LENGTH:
  	 *         * seed--an instance field of Genome. It produces LENGTH=12,288
	 *         doubles from -2 to +2:
  	 *             * codons--the weights of the neural network. See the
  	 *             selection methods for more details on how the net works. Note
  	 *             that these are packed into an one dimensional array that is
  	 *             one of the instance fields of Genome. There are ~=12,288,000
  	 *             per initial population.
	 * 
	 *     * matingMetaSeed--[rep] This scheme's job is to split the gene pool
	 *     into random, randomly ordered pairs. Like the mutation scheme, this
	 *     set of random values can't be taken care of at the beginning; this
	 *     meta-seed must produce a new seed every generation. This is
	 *     necessary; to ensure that crossover and face-offs are random every
	 *     time, they must be paired differently every time. Furthermore, the
	 *     pairing *must be ordered,* or else I'd need a metaMetaCrossoverSeed
	 *     below. Consequently, this scheme creates a *permutation* of the
	 *     population. It produces 1 long *per generation* from
	 *     Long.MIN_LENGTH to Long.MAX_LENGTH:
	 *         * matingSeed--an instance field of Population. Like mutateSeed,
	 *         this changes with each passing generation. Read the documentation
	 *         of permutation.Permutation to see how I made permutation work and
	 *         have it be seedable; for now the only relevant detail is that
	 *         more seeds than are strictly necessary need to be produced--the
	 *         method is roughly 92% efficient. As long as the efficiency is
	 *         above 90%, a redundancyFactor of 10/9 is sufficient. For safety I
	 *         may end up increasing this, however. It produces 
	 *         redudancyFactor * popSize ~= 1111 ints from 0 to popSize-1~=999:
	 *             * matingKey--please see Permutation.java to fully understand
	 *             how this works; I'll just give a brief overview here. Each
	 *             mating key acts as an "assignment value" for an element of
	 *             the ordered ~1000-tuple (0,1,2,3,...,~999). For example, if
	 *             the first (randomly produced) matingKey is 628, then 999 ends
	 *             up in position 628 in the permuted ~999-tuple. (I did it
	 *             backwards because it made the alhorithm's structure
	 *             significantly clearer.) For values below popSize, the
	 *             assignment value skips over spots in the partially permuted
	 *             array that had already been filled in. When a matingKey
	 *             exceeds its maximum allowed value (when half of the
	 *             population has been sorted, half of the remaining spots are
	 *             left, so only numbers from 0-~499 are still legal), it is
	 *             modded down to a legal value. To preserve probabilistic
	 *             equality, any extra values are stored for future consumption
	 *             by a smaller number and the next matingKey is used instead
	 *             (it makes sense; trust me). If more than one illegal value is
	 *             produced for a given starting value, then all but the first
	 *             are thrown away. Those last two parts are why a given
	 *             matingKey isn't associated with any gene in particular; it
	 *             may end up far from where it started or forgotten entirely.
	 *             It's also why more than popSize ints have to be produced. 
	 *  
	 *     * metaCrossoverSeed--[rep] an instance field of Population. Note that
	 *     though reproduction occurs every generation, there is only one of
	 *     these per population (I really don't want to have to deal with meta-
	 *     meta-seeds at this level). This should be okay so long as the
	 *     mating scheme pairing process leaves them ordered randomly--AND
	 *     orders them differently every generation. That way, the top genome
	 *     (for example) won't have the first (of ~1000) crossoverSeed every
	 *     time. I could be wrong, so... TODO: Make sure that meta-meta-seeds
	 *     are unnecessary in crossover under this system--can I reuse the same
	 *     meta-seed every generation? If it does work, then TODO: see if the
	 *     same logic can be used to cut down on meta-seeds. This produces
	 *     popSize ~= 1000 longs from Long.MIN_LENGTH to Long.MAX_LENGTH:
	 *         * crossoverSeed--while it itself is not a field of any class, the 
	 *         array containing all of them is an instance field of Population.
	 *         Note that there are ~=1000 crossoverSeeds for each population and
	 *         not ~=500. This is because each parent produces two children to
	 *         keep the population stable. It produces LENGTH=12,288 doubles
	 *         from 0 to 1:
	 *             * crossoverKey--contained in a two dimensional array instance
	 *             field of Population. These are used to determine how children
	 *             are created from their parents. See the methods for more
	 *             details on reproduction itself, but essentially each parent
	 *             has a probability, p and q=1-p, which are directly
	 *             proportional to their respective fitnesses, which represents
	 *             the probability that a given codon will come from each
	 *             parent. To simulate this in a way that I can repeat (with the
	 *             magic of seeded pseudorandomness!), this double is created.
	 *             It is uniformly distributed over [0,1] (actually, it's [0,1)
	 *             because of some unfathomable [shut up, eclipse's spell-check
	 *             --that word is perfectly spelled!] reasoning by Java's
	 *             libraries' creators, but it shouldn't matter. Probably.) If
	 *             0 <= key <= p, it goes with parent A's codon for that
	 *             position. If p < key <= 1, parent B's. (Note: I think this is
	 *             how inverse transform sampling works, except obviously with
	 *             uncountably infinitely many values instead of two. Or maybe
	 *             my intuition is off.) There are ~= 12,288,000 per initial
	 *             population.
	 *             
	 *     * metaMutationSeed--[rep] This seed scheme's function is to randomly
	 *     mutate the next generation, every generation. Like mating seed
	 *     scheme, this crosses time. Otherwise, with a low enough mutateProb,
	 *     the codon at, say, position 32,019 could never be mutated, no matter
	 *     how randomly the mating scheme permutes the population. Note that
	 *     mutation takes place after the new generation has been created by
	 *     their parents with the crossover scheme. It produces 1 long *per
	 *     generation* from Long.MIN_LENGTH to Long.MAX_LENGTH:
	 *         * mutationSeed--an instance field of Population. Every new 
	 *         generation (and hence new population--with this strucure, each
	 *         generation represents a different population object) has new
	 *         mutationSeed. It's job is to produce ~=122,880 ints from
	 *         ~=-12,288,000 to ~=12,288,000:
	 *             * mutateKey--so the way I'm making this work is a total
	 *             kludge. ~=122,880 keys are produced--~=mutateProb of the
	 *             total number of codons in the population. The mutation is
	 *             performed as follows: the codon corresponding to the absolute
	 *             value of a key is mutated by mutateVal if the key is positive
	 *             and -mutateVal if the key is negative. If the same codon is
	 *             produced twice, it is mutated twice. If 0 is a key, nothing
	 *             happens. If a codon is mutated above 2 or below -2, it gets
	 *             set to those bounds respectively. "But wait!" you say. "That
	 *             means that less than mutateProp (~=12,288,000) codons may be 
	 *             mutated, but never more!" To that I say: Shut up and make a
	 *             better system. But seriously, it may be fixable by taking
	 *             mutateProp, shifting it upwards, then using that to determine
	 *             the number of mutated codons. I haven't done the math, so I
	 *             don't know how feasible that is. If it isn't, then I'll just
	 *             make a better way to do it (though it'll take up a good chunk
	 *             of memory and slow down the program). TODO: Can I fudge this
	 *             kludge to a proper solution? (Oh, so "fudge" isn't a word
	 *             now, eclipse? Really?)
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
	 * yet mean anything.) Note that this means a genome will never play its
	 * sibling--which I think should be a good thing.
	 * 
	 *
	 * 
	 * II. NEURAL NETWORK
	 * 
	 * What a neural network does, in the most general sense, is take in a
	 * series of sensory inputs and output some action to take. Think of each
	 * input and output as a box that stores a value. All of the input boxes
	 * form one "layer," and all of the output boxes form another layer.
	 * Additionally, there is (at least) one "hidden layer" between the input
	 * and output layers. The values stored in the input data are sensory info
	 * --in this case, the positions of the game pieces (and whether or not each
	 * side has castled). To determine the value of, say, the 42nd box in the
	 * hidden layer, the neural network multiplies each of the values of the
	 * preceding layer by some weight, then finds their sum. Consequently, with
	 * 66 boxes in the input layer and 98 in the hidden layer, there are a total
	 * of 66 weights per hidden box, or 66*98 weights total from the input layer
	 * to the hidden layer. (All of these weights, by the way, are the codons of
	 * the neural networks' genome.) The weights range from -2 to +2. The same
	 * process is done to determine the output layer from the hidden layer,
	 * except after the weighted sum has been taken, it's run through (a linear
	 * transformation of) the hyperbolic tangent function to make the values
	 * stored in the output boxes be between 0 and 1. These values are used to
	 * rank all possible moves (really, any line from and square to any other)
	 * from most likely to least likely. Illegal moves are removed, and the
	 * legal move with the highest score is that neural networks move for that
	 * board configuration. Heres's an overview of the network and its layers:
	 *
	 *
	 * * Input Layer--66 boxes. Represents the board configuration. Values in
	 * boxes 0-63 are integers ranging from -6 to +6, inclusive; values in boxes
	 * 64-65 are integers ranging from 0 to 1, inclusive. The first 64 boxes
	 * each represent a square on the board; a value of 0 is an empty square, +1
	 * is a pawn of your color, -1 is a pawn of the other color, +2 is a knight,
	 * or you color,... you get the idea. The final two squares represent
	 * whether or not the white king and black king have castled. I included
	 * this because the input information should give you *everything* you need
	 * to know about how to play the game from then on out. In theory, if a
	 * grandmaster were to start a game at move 42, what he would do next could
	 * depend very heavily on whether or not castling has occurred. Granted,
	 * usually one can tell--but not always, which is why I included it. "But
	 * wait! " I here you say. "Based on that criteria, shouldn't you include
	 * whose move it is?" Yes, it does match the criteria, but whenever a neural
	 * network turns inputs into outputs to make a move, it is its move, so it
	 * wouldn't give any extra information. Finally, note that the box each
	 * square is associated with changes with color. For example, at the start
	 * of a game, box 7 refers to the network's bottom right rook--no matter
	 * which color you are playing.
	 * 
	 * * Hidden Layer--98 boxes. Doesn't represent anything. Values in boxes are
	 * integers ranging from -240 to +240, inclusive. (These bounds would never
	 * be reached; 236 is only hit if every weight going into the hidden layer
	 * box is 2 or -2, if both sides have queened all of their pawns, if the
	 * original queen and the rooks remain, and if both sides have castled. For
	 * reference, if every weight is 1 for your color and -1 for the other
	 * color, then at the starting position the box is 74.) No normalization is
	 * done after the weighted sum is taken (though I may change this in the
	 * future by dividing by 66). Again, this layer doesn't represent anything;
	 * it simply connects the input layer and output layer in a more complicated
	 * way. I may add more hidden layers in the future. I chose to make this 98
	 * boxes because that's the average of the input layer's cardinality and the
	 * output layer's cardinality, rounded up.
	 * 
	 * Output Layer--129 boxes. Represents the move to make with the given board
	 * configuration. Values in boxes 0-127 are reals ranging from 0 to 1,
	 * exclusive; the value in box 128 is an integer ranging from 2 to 5,
	 * inclusive. There is a normalization process (or rather, two); for boxes
	 * 0-127, the weighted sum is run through (tanh(x/s_0)+1)/2, where 's_0' is
	 * some as-yet-to-be-determined scaling value and tanh is the hyperbolic
	 * tangent. The end result is that the weighted sum is mapped to a number
	 * between 0 and 1, depending on how big it is. Box 129 runs the weighted
	 * sum through round[(3*tanh(x/s_1)+7)/2], where round[x] rounds x to the
	 * nearest integer, tanh(x) is the hyperbolic tangent, and 's_1' is a second
	 * as-yet-undecided scaling factor. Therefore, it ends up mapping the
	 * weighted sum to some integer from 2 to 5, inclusive. This 129-tuple of
	 * real numbers from 0 to 1 represents a chess move. There are three types
	 * of output boxes, starting-point boxes, finishing-point boxes, and
	 * promotion boxes. Every move in chess for a given board position can be
	 * uniquely represented by the starting square and the finishing
	 * square--except for promotion, which also must include which piece one is
	 * promoting to. This special case is why box 129 exists. First, illegal
	 * moves are removed. (Most will be easy--moves that start on an empty
	 * square or an opponents piece will be immediate; for others I'll have to
	 * take into account the piece's movement pattern. That leaves things like
	 * check and whatnot to worry about.) Second, I look at all of the remaining
	 * moves, which are represented as pairs of numbers from 0 to 1, and
	 * multiply them together, yielding a new number from 0 to 1. This number
	 * represents a move's final score; the move with the highest score is
	 * how the net actually moves. TODO: What about ties? 
	 */

}