import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimilarityUpdater {
	private Reader reader;
	private double threshold;
	private boolean verbose=true;
	
	
	public SimilarityUpdater(Reader reader, double threshold) {
		this.reader=reader;
		this.threshold=threshold;
	}
	
	public SimilarityUpdater(Reader reader, double threshold,boolean verbose) {
		this.reader=reader;
		this.threshold=threshold;
		this.verbose=verbose;
	}
	/**
	 * Update the jackard similarity of the given set of simmilar pairs
	 * @param set
	 */
	public void updateSimilarity(Set<SimilarPair> set) {
	        reader.readAll(set);
	        double acumSim=0;
	        double max=Double.MIN_VALUE;
	        double min=Double.MAX_VALUE;
	        List<Set<Integer>> docToShingle = reader.getObjectMapping();
	        int total=0;
	        int below=0;
	        for(SimilarPair pair:set) {
	        	int post1=-1;
	        	int post2=-1;
	        	
	        	for (int i = 0; i < docToShingle.size(); i++) {
	        		if(pair.id1==reader.getExternalId(i))
	        			post1=i;
	        		if(pair.id2==reader.getExternalId(i))
	        			post2=i;
				}
	        	
	        	if(post1==-1||post2==-1)
	        		continue;
	        	double sim = jaccardSimilarity(docToShingle.get(post1), docToShingle.get(post2));
	        	pair.sim=sim;
	        	acumSim+=pair.sim;
	        	if(pair.sim>max)
	            	max=pair.sim;
	            if(pair.sim<min)
	            	min=pair.sim;
	        	total++;
	        	if(sim<this.threshold)
	        		below++;	        	
	        }
	   if (verbose) {	        
		    System.out.printf("-Total: "+total +" -- Greather than %.3f: %d   Less than  %.3f: %d \n",this.threshold,(total-below),this.threshold,below);
		    if (total!=0)
		    	System.out.printf("--Detailed Resume: Average Similarity: %.3f   Max: %.3f  Min: %.3f \n",acumSim/((double)total),max,min);
	   }
	}
	 
	 public <T> double jaccardSimilarity(Set<T> set1, Set<T> set2) {
	        Set<T> union = new HashSet<T>(set1);
	        union.addAll(set2);
	
	        Set<T> intersection = new HashSet<T>(set1);
	        intersection.retainAll(set2);
	
	        if (union.size() == 0){
	            return 0;
	        }
	        return (double) intersection.size() / union.size();
	    }

}
