/**
 * Copyright (c) DTAI - KU Leuven – All rights reserved.
 **/
 
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads a set of documents and constructs shingle representations for these
 * documents.
 */
public abstract class Reader {

    // maps a doc id to its shingle representation
    protected List<Set<Integer>> idToShingle = new ArrayList<Set<Integer>>();
    // a shingler
    public Shingler shingler;
    // max number of docs to read
    protected int maxDocs;
    // maps each doc's internal id to its external id
    public List<Integer> idToDoc = new ArrayList<Integer>();
    // maps each doc's internal id to its external id
    public List<Integer> idToDoc2 = new ArrayList<Integer>();
    // number of docs read
    protected int curDoc;
    
    protected int documentsPerCycle;

    /**
     * Construct a new document reader.
     * @param maxDocs maximal number of documents to read.
     * @param shingler a document shingler.
     */
    public Reader(int maxDocs, Shingler shingler,int documentsPerCycle){
        this.shingler = shingler;
        this.maxDocs = maxDocs;
        this.curDoc = 0;
        this.documentsPerCycle=documentsPerCycle;
    }

    /**
     * Read the next document.
     * @return the shingle representation for the next document.
     */
    abstract public Set<Integer> next();
    
    
    /**
     * Read the next document.
     * @return the shingle representation for the next document.
     */
    abstract public Set<Integer> next(Set<Integer> set);

    /**
     * Reset this reader.
     */
    abstract public void reset();

    /**
     * Check whether there are more documents.
     * @return True if there are more documents; otherwise False.
     */
    public boolean hasNext(){
        return this.curDoc < this.maxDocs ;
    }

    /**
     * Get the mapping of the object id to its set representation.
     * @return the mapping
     */
    public List<Set<Integer>> getObjectMapping() {
        return idToShingle;
    }

    /**
     * Read all maxDocs documents at once.
     */
    public void readAll(){
        reset();
        while (this.hasNext()){
            Set<Integer> shingle = this.next();
            this.idToShingle.add(shingle);
        }
    }
    
    public void readAll(Set<SimilarPair> set){
        reset();
        Set<Integer> set2=new HashSet<Integer>();
        for(SimilarPair a:set) {
        	set2.add(a.id1);
        	set2.add(a.id2);
        }
        while (this.hasNext()){
            Set<Integer> shingle = this.next(set2);
            if (shingle!=null)
            	this.idToShingle.add(shingle);
        }
    }
    
    public void readNextDocuments(){
        reset();
        int documentesReaded=0;
        while (this.hasNext() && documentesReaded<this.documentsPerCycle ){
            Set<Integer> shingle = this.next();
            this.idToShingle.add(shingle);
            documentesReaded++;
        }
    }

    /**
     * Get the number of unique shingles that were processed.
     * @return the number of unique shingles
     */
    public int getNumShingles(){
        return this.shingler.getNumShingles();
    }

    /**
     * Get the number of documents that were processed.
     * @return the number of documents.
     */
    public int getNumDocuments(){
        return this.maxDocs;
    }

    /**
     * Map an internal id to an external id.
     */
    public int getExternalId(int id) {
        return this.idToDoc.get(id);
    }

}
