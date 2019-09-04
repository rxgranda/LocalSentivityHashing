/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved.
 **/
 
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Searching similar objects. Objects should be represented as a mapping from
 * an object identifier to a set containing the associated values.
 */
public abstract class SimilaritySearcher {

    Reader reader;
    double threshold;

    /**
     * Construct a SimilaritySearcher object.
     * @param reader a data Reader object
     * @param threshold the similarity threshold
     */
    public SimilaritySearcher(Reader reader, double threshold){
        this.reader = reader;
        this.threshold = threshold;
    }

    /**
     * Returns the pairs of the objectMapping that have a similarity coefficient exceeding threshold
     * @return the pairs with similarity above the threshold
     */
    abstract public Set<SimilarPair> getSimilarPairsAboveThreshold();
    

    /**
     * Jaccard similarity between two sets.
     * @param set1
     * @param set2
     * @return the similarity
     */
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
    
    public <T> double jaccardSimilarityList(List<Integer> l1, List<Integer> l2) {
    	int a,b,same=0;
       for (int i = 0; i < l1.size(); i++) {
    	   a=l1.get(i);
    	   b=l2.get(i);
    	   if(a==b)
    		   same++;
       }
        
        return (double) same/ l1.size();
    }
    
    public <T> double jaccardSimilarityList(int[] l1, int[] l2) {
    	int a,b,same=0;
       for (int i = 0; i < l1.length; i++) {
    	   a=l1[i];
    	   b=l2[i];
    	   if(a==b)
    		   same++;
       }
        
        return (double) same/ l1.length;
    }

}
