import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.Arrays;


/**
 * Implementation of minhash and locality sensitive hashing (lsh) to find
 * similar objects.
 *
 */
public class LSH extends SimilaritySearcher {
    /* FILL IN HERE */
	private final int numDocuments;
	private final long numShingles;
	private final int numBands;
	private final int rowsPerBand;
	private final int numBuckets;
	private long[][] signatureMatrix;
	private final long p;
	private final int numberHashes;
	private long [] aValues;
	private long [] bValues;
	private long [] hashValues;
	private final static int maxRandom=1000;//Look for this in the future
	List<Set<Integer>> docToShingle;
	int []row;
	int [][]signaturesB;
	String []bandSignatures;
    /**
     * Construct an LSH similarity searcher.
     *
     * @param reader a data Reader object
     * @param threshold the similarity threshold
     * @param numHashes number of hashes to use to construct the signature matrix
     * @param numBands number of bands to use during locality sensitive hashing
     * @param numBuckets number of buckets to use during locality sensitive hashing
     * @param numValues the number of unique values that occur in the objects'
     *                  set representations (i.e. the number of rows of the 
     *                  original characteristic matrix)
     * @param rand should be used to generate any random numbers needed
     * @param rowsPerBand represents the number of rows-per-band parameter
     */
    public LSH(
            Reader reader, double threshold, int numHashes, int numBands, 
            int numBuckets, int numValues, Random rand, int rowsPerBand) {
        super(reader, threshold);
        numDocuments=reader.documentsPerCycle;
        numShingles=numValues;
        numberHashes=numHashes;
        this.numBands=numBands;
        this.rowsPerBand=rowsPerBand;
        this.numBuckets=numBuckets;
        signatureMatrix=new long[numberHashes][numDocuments];
        
        // Needed for Universal Hashing
        p=(long)Primes.findLeastPrimeNumber((int)numShingles);
        aValues=new long[numberHashes];
        bValues=new long[numberHashes];
        hashValues=new long[numberHashes];
        
        //Prepare hash values for Numbers A and B in the Universal Hashing
        for (int i = 0; i < numberHashes; i++) {
            aValues[i]=Math.abs((int)(rand.nextDouble()*maxRandom));
            bValues[i]=Math.abs((int)(rand.nextDouble()*maxRandom));
        }
        
        row=new int[numDocuments];
    }


    /**
     * Returns the pairs with similarity above threshold (approximate).
     */
    @Override
    public Set<SimilarPair> getSimilarPairsAboveThreshold() {
    	Set<SimilarPair> similarPairsAboveThreshold =new HashSet<SimilarPair>();    	
    	 /* FILL IN HERE */
    	HashMap<String,SimilarPair> potentialSimilars =new HashMap<String,SimilarPair>();

    	HashSet<Integer> rowsToAnalyze;
    	int DOCUMENTS;
    	int hashSignature=-1;
    	ListIterator<Integer>itr;
        int doc1,doc2;
        int documentId;
        LinkedList<Integer> bucketValues;
        String firma;
        
        // Create a structure for holding Buckets for all the Questions
    	List <HashMap<Integer,LinkedList<Integer>>> hashBuckets=new ArrayList<HashMap<Integer,LinkedList<Integer>>>();
    	for(int k=0;k<numBands;k++) 
    		hashBuckets.add(new HashMap<Integer,LinkedList<Integer>>());
    	// Iterate over all the documents to analyze, taking <documentsPerCycle> Documents 
    	while(reader.hasNext()) {
	    	
    		this.resetSignatureMatrix();	    	
	    		       
	        reader.readNextDocuments();
	        docToShingle = reader.getObjectMapping();
	    	DOCUMENTS=docToShingle.size();
	    	// Simulate Input Matrix
	    	rowsToAnalyze=new HashSet<Integer>();
	    	
	    	for (int doc = 0; doc < docToShingle.size(); doc++)	        	     			        		
	    		rowsToAnalyze.addAll(docToShingle.get(doc));	        			        	
	    	System.gc();
  		    //MinHash Implementation    
	        for (int r : rowsToAnalyze) {
	        	Arrays.fill(row, 0);
	        	// Simulate extraction of the row r in the Input Matrix
	        	getRowAtX(r);
	        
	            for (int i = 0; i < numberHashes; i++) {
	                hashValues[i]=universalHashing((long)r,i);
	            }
	            //Iterate thought the columns of the row
	            for (int c = 0; c < DOCUMENTS; c++) {
	                if (row[c]==0)
	                    continue;
	                //Compare Hash values vs Signature matrix
	                for (int i = 0; i < numberHashes; i++) {
	                    if(signatureMatrix[i][c]> hashValues[i])
	                        signatureMatrix[i][c]=hashValues[i];
	                }
	            }            
	        }
	        
	   	        
	        //Perform Band Analysis for the current amount of documents	      
	        hashSignature=-1;
	        bandSignatures=new String[DOCUMENTS];
	        Arrays.fill(bandSignatures,"");
	        
	        for (int b = 0; b < numBands; b++) {        	
	        	//Built Hash Signatures	        	
	        	for (int i = 0; i <rowsPerBand ; i++) {	        			        		
	        		for (int j = 0; j <DOCUMENTS ; j++) {		        			
	        			bandSignatures[j]+=signatureMatrix[i+rowsPerBand*b][j];	
	        		} 	        		
		        }
	        	// Prepare Band Signatures for Hashing
	        	for (int i = 0; i <bandSignatures.length ; i++) {
	        		// Get Hash for Band Signature
	        		hashSignature=MurmurHash.hash32(bandSignatures[i],1234)%numBuckets;
	        		documentId=reader.getExternalId(i);	        			        			        		
	        		// Push DocumentID into the Collection of Documents for the given hash of the Signature
	        		if (!hashBuckets.get(b).containsKey(hashSignature)){ 
	        			bucketValues=new LinkedList<Integer>(); 
	        			hashBuckets.get(b).put(hashSignature, bucketValues);	        			
	        			
	                }else { // If there is already documents with the given hash, then add as potential similar pairs
	                	bucketValues=hashBuckets.get(b).get(hashSignature);
	                	itr=(ListIterator<Integer>) bucketValues.iterator();
	                	while(itr.hasNext()) {
	                		doc1=itr.next();
	                    	doc2=documentId;
	                    	SimilarPair similar=new SimilarPair(doc1, doc2, (float)rowsPerBand/(float)numberHashes);
	                    	
	                    	if(doc1>doc2)
	                    		firma=doc1+"-"+doc2;
	                    	else
	                    		firma=doc2+"-"+doc1;
	                    	if(!potentialSimilars.containsKey(firma))
	                    		potentialSimilars.put(firma, similar);
	                    	else {
	                    		potentialSimilars.get(firma).sim+=(double)((float)rowsPerBand/(float)numberHashes);
	                    	}               	
	                	}	                	
	                }
	        		bucketValues.add(documentId);        		        		
	        		bandSignatures[i]="";
		        }	        		        	        		        	
	            //System.out.println("---");
	        }	        
    	}
    	
    	//// Check for candidates above jackard similarity above threshold
    	System.gc();
    	//System.out.println("LSH Done!\nUpdating Similarity of Pairs..");
        //DataHandler dh2 = new DataHandler(this.reader.maxDocs, this.reader.shingler,((DataHandler)this.reader).filePath,this.reader.documentsPerCycle);
        //SimilarityUpdater updater = new SimilarityUpdater(dh2, threshold);
        //System.out.println("Numero Similares:"+potentialSimilars.size());
        //updater.updateSimilarity(potentialSimilars);
        System.out.println("--LIST OF SIMILAR PAIRS--");
        for(SimilarPair pair: potentialSimilars.values()) { 
        	if(pair.sim>=threshold) {
        		similarPairsAboveThreshold.add(pair);
        		System.out.println(pair);       
        	} 	        
        	potentialSimilars.remove(pair);
        }
    	
        return similarPairsAboveThreshold;
    }
    
    /**
     * Calculate Universal Hashing for a given row and a given hash function
     * @param x Number of the row
     * @param i Number of Hash Function
     * @return the value for Universal Hashing
     */
    private final long universalHashing(long x,int i) {
    	return ((aValues[i]*x+bValues[i]) %p) % numShingles;
    }
    
    
    /**
     * Method for reset the signature matrix for LSH
     */
    private final void resetSignatureMatrix() {
    	for (long [] row: signatureMatrix)
            Arrays.fill(row, Long.MAX_VALUE);
    
    }
    /**
     * Method for the simulation of extracting a row for the Input Matrix
     * @param x  Number of the row
     */
    private void getRowAtX(int x) {
    	for (int doc = 0; doc < docToShingle.size(); doc++){        	
        	if  (docToShingle.get(doc).contains(x)) {
        		row[doc]=1;
        	}
        }    	
    }
          

}
