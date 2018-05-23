/*
 * Created on Nov 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package reqGen.ncsu.xacml;

import java.util.Random;
import java.util.Set;

/**
 * @author eemartin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RandomRequestFactory extends AllComboReqFactory {

	private long maxRand;
	
	private Random rand;
	
	private int nextOnBit, nextOffBit;
	
	public RandomRequestFactory(Set policies) {
		this(policies, 0);
	}
	
	public RandomRequestFactory(Set policies, long max) {
		super(policies);
		maxRand = max;
		rand = new Random();
		nextOnBit = 0;
		nextOffBit = 0;
		updateMaps();
	}
	
	public boolean hasNext() {
		if (maxRand <= 0) {
			return true;
		}
		return count <= maxRand;
	}
/*	
	protected void updateMaps() {
		// increment count
		count++;
		map.clear();
		// randomize bits
		for (int i = 0; i < lengthOfMap(); i++) {
			map.set(i, rand.nextBoolean());
		}
		// to ensure all bits are off and on at least once
		if (nextOnBit < lengthOfMap()) {
			map.set(nextOnBit++, true); 
		} else if ((nextOnBit == lengthOfMap()) 
				&& (nextOffBit < lengthOfMap())) {
			map.set(nextOffBit++, false);
		}
	}
*/	
	protected void updateMaps() {
		// increment count
		count++;
		map.clear();
		// randomize bits
		for (int i = 0; i < lengthOfMap(); i++) {
			map.set(i, rand.nextBoolean());
		}
		
	
		// to ensure all bits are off and on at least once
		if (nextOnBit < lengthOfMap()) {
			map.set(nextOnBit++, true); 
		} else if ((nextOnBit == lengthOfMap()) 
				&& (nextOffBit < lengthOfMap())) {
			map.set(nextOffBit++, false);
		}
	}	
	
}
