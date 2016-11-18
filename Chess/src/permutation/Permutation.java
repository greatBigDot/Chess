package permutation;

import java.util.Arrays;

public class Permutation {
	/* This class exists to randomly pair genes prior to the reproduction phase
	 * of the evolutionary algorithm. For reasons explained in the documentation
	 * at the bottom of evolution.Genome.java, an ordered permutation is
	 * necessary.
	 */
	
	//This takes an array of numbers from min to max and returns a random permutation of it.
	public static int[] permute(int min, int max, int[] rands){
		max=max-min;   // Now it starts at 0; min is added back later.
		int dual, newMap, adjMap;   //Where rands[max-i] is associated with if it overflows i, where i is mapped to, and the adjusted assignment value, respectively.
		int[] map = new int[max+1]; Arrays.fill(map, -1);   //Tracks all new assignments for when overflow occurs. 0 is a valid assignment; -1 isn't.
		int[] permuted = new int[max+1]; Arrays.fill(permuted, -1);   //The permuted array. 0 is a valid value; -1 isn't.
		int count = 0;   //Counts which random number is being used. Because of reassignments and whatnot, I can't just use max-i.
		boolean overflow;   //Tests whether newPos overflows i.
		
		for(int i=max; i>=0; i--){			
			//If i already has a new mapping set, then use it. If not, figure out what is should be.
			if(map[i]!=-1){
				newMap = map[i];
			}
			else{
				dual = ((max+1)%(i+1))-1;
				dual = (dual + (i+1)) % (i+1);   //According to java, (-1) mod 5 = -1, when it should be (-1) mod 5 = 4. This is to take care of that special case (i.e., when i+1 is a factor of max+1.
			
				//All of this should be structured better. But don't blame me; blame idempotency!
				newMap = rands[count];count++;   //Set tentative value for newPos; if it overflows i (i.e., if newMap>=map-dual), then store it for future use by dual
				overflow = (newMap >= max-dual) && (i!=dual);   //True whenever there is an overflow.  Also, it gets screwed up when a number is its own dual (including max), so add that as a special case.
				if(overflow && (map[dual]==-1))   //If there is an overflow (and if dual doesn't already have a mapping stored), store a mapping for future use.
					map[dual] = newMap%(dual+1);   //Use newPos's failures to my advantage--set the future mapping for i's dual. Note that (max-dual) % (dual+1) == 0 doesn't necessarily hold.
				while(overflow){   //keep producing new newPos's until we have a valid (i.e., non-overflowing) value for i.
					newMap = rands[count];count++;   //reset newPos...
					overflow = (newMap >= max-dual) && (i!=dual);   //...and update overflow accordingly.
				}
				newMap = newMap % (i+1);   //Normailze newMap
			}
			
			//Now that we finally have a value for newMap, use it:
			adjMap = map(permuted, newMap);   //Adjust newMap to take into account that permute is partially filled
			permuted[adjMap] = i + min;   //Store i safely away in its new randomly generate position. Also, add min back.
		}
		return permuted;   //Permutation complete!
	}
	
	//This maps the input to a certain position in the array, skipping any filled positions. Ex: map({5,13,-1,-1,7,-1,10},42,1))=3, because the 1st unfilled position is actually position 3.
	private static int map(int[] partialArray, int mapping){
		//Note: this only works with this class's setup.
		
		int count=-1;   //Specifies
		for(int i=0;i<=partialArray.length-1;i++){
			if(partialArray[i]==-1)
				count++;
			if(count==mapping)
				return i;
		}
		return -1;   
	}
}
