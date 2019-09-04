import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.XMLStreamException;

/**
 * The Runner can be ran from the commandline to find the most similar pairs
 * of StackOverflow questions.
 */
public class Runner {

    public static void main(String[] args) {

        // Default parameters
        String dataFile = "";
        String testFile = null;
        String outputFile = "";
        String method = "";
        int numHashes = -1;
        int numShingles = 1000;
        int numBands = -1;
        int numBuckets = 2000;
        int seed = 1234;
        int maxQuestions = -1;
        int shingleLength = -1;
        float threshold = -1;
        int rowsPerBand=-1;
        int documentsPerCycle=100;

        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            String arg = args[i];
            if (arg.equals("-method")) {
                if (!args[i+1].equals("bf") && !args[i+1].equals("lsh")){
                    System.err.println("The search method should either be brute force (bf) or minhash and locality sensitive hashing (lsh)");
                }
                method = args[i+1];
            }else if(arg.equals("-numHashes")){
                numHashes = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numBands")){
                numBands = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numBuckets")){
                numBuckets = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-numShingles")){
                numShingles = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-seed")){
                seed = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-dataFile")){
                dataFile = args[i+1];
            }else if(arg.equals("-testFile")){
                testFile = args[i+1];
            }else if(arg.equals("-outputFile")){
                outputFile = args[i + 1];
            }else if(arg.equals("-maxQuestions")){
                maxQuestions = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-shingleLength")){
                shingleLength = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-threshold")){
                threshold = Float.parseFloat(args[i+1]);
            }else if(arg.equals("-rowsPerBand")){
                rowsPerBand = Integer.parseInt(args[i+1]);
            }else if(arg.equals("-documentsPerCycle")){
            	documentsPerCycle = Integer.parseInt(args[i+1]);            
            }
            i += 2;
        }
        rowsPerBand=numHashes/numBands;
        System.out.println("&& Starting With Parameters threshold: "+threshold+" shingleLength:"+shingleLength+" bands:"+numBands+" numHashes:"+numHashes+" rowsPerBand:"+rowsPerBand);

        Shingler shingler = new Shingler(shingleLength, numShingles);

        DataHandler dh = new DataHandler(maxQuestions, shingler, dataFile,documentsPerCycle);
        SimilaritySearcher searcher = null;

        if (method.equals("bf")){
            searcher = new BruteForceSearch(dh, threshold);
        }else if(method.equals("lsh")){
            if(numHashes == -1 || numBands == -1 || rowsPerBand == -1){
                throw new Error("The parameters -rowsPerBand -numHashes and -numBands are mandatory arguments for the LSH method");
            }
            Random rand = new Random(seed);
            searcher = new LSH(dh, threshold, numHashes, numBands, numBuckets, numShingles, rand,rowsPerBand);
        }

        long startTime = System.currentTimeMillis();

        System.out.println("Searching items more similar than " + threshold + " ... ");
        Set<SimilarPair> similarItems = searcher.getSimilarPairsAboveThreshold();
        System.out.println("done! Took " +  (System.currentTimeMillis() - startTime)/1000.0 + " seconds.");
        System.out.println("--------------");
             
     
        savePairs(outputFile, similarItems);
        if (testFile != null) {
        	System.out.println("Starting Enhanced Duplicates.xml Test..");
        	DataHandler dh2 = new DataHandler(maxQuestions, shingler,dataFile ,documentsPerCycle);
            SimilarityUpdater updater = new SimilarityUpdater(dh2, threshold,false);
            Set<SimilarPair>references=SimilarPairParser.read(testFile);
            updater.updateSimilarity(references);
            testPairs(similarItems,references , dh.idToDoc2,threshold);
        }
        System.out.println("## End of Program");

    }


    /**
     * Save pairs and their similarity.
     * @param similarItems
     */
    public static void savePairs(String outputFile, Set<SimilarPair> similarItems){
        try {
            SimilarPairWriter.save(outputFile, similarItems);
            System.out.println("Found " + similarItems.size() + " similar pairs, saved to '" + outputFile + "'");
            System.out.println("--------------");
        } catch (FileNotFoundException e) {
            System.err.println("The file '" + outputFile + "' does not exist!");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static void testPairs(Set<SimilarPair> results, Set<SimilarPair> references, List<Integer> processedDocs,float threshold) {
        int truePosCount = 0;
        int falsePosCount = 0;
        int falseNegCount = 0;
        for (SimilarPair result : results) {
            if (references.contains(result)) {
                truePosCount++;
            } else {
                falsePosCount++;
            }
        }
        double sim=0;
        double max=Double.MIN_VALUE;
        double min=Double.MAX_VALUE;
        int aboveThreshold=0;
        System.out.println("--Missing pairs above threshold--");
        for (SimilarPair reference : references) {
            if (processedDocs.contains(reference.id1)
                    && processedDocs.contains(reference.id2)
                    && !results.contains(reference)) {
            	if(reference.id1==reference.id2) // I found some pairs reported as "Duplicates", but those pairs have the same QuestionID, which is not possible. 
            		continue;
                falseNegCount++;
                sim+=reference.sim;
                if(reference.sim>max)
                	max=reference.sim;
                if(reference.sim<min)
                	min=reference.sim;
                if(reference.sim>=threshold) {
                	aboveThreshold++;
                	//System.out.println(reference);
                }
            }
        }
        if(falseNegCount!=0) {
        	System.out.println("--Missed above threshold:  "+aboveThreshold);
        	System.out.printf("--Total Missed: "+falseNegCount+" -- Average Similarity: %.3f  Max Similarity: %.3f   Min Similarity: %.3f  Specified Theshold: %.3f\n\n",sim/((double)falseNegCount),max,min,threshold);        
        }
        double precision = (double) truePosCount / (truePosCount + falsePosCount);
        double recall = (double) truePosCount / (truePosCount + falseNegCount);
        double f1 = 2d*truePosCount / (2*truePosCount + falsePosCount + falseNegCount);
        System.out.println("Test results:");
        System.out.println("TP: " + truePosCount
                + ", FP: " + falsePosCount
                + ", FN: " + falseNegCount
                + ", F1: " + f1
                );
    }

}
