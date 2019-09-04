#Finding near-duplicates text  in Big Data using using Locality Sensitive Hashing (LSH) 

Near-duplicates text detection in large collection of documents (Big Data) is a challenging problem. This project intends identify very simmilar or duplicate questions in the Stack Overfow platform using Locality-sensitive Hashing algorithm(LSH). Experiment over a small set of stack overflow question is provided here; however it can be spanded to the full datasetwith more than a million questions. 

*(To be debugged)

## Literature
The LSH algorithm is based on: Indyk, Piotr, and Rajeev Motwani. "Approximate nearest neighbors: towards removing the curse of dimensionality." Proceedings of the thirtieth annual ACM symposium on Theory of computing. ACM, 1998.

### Prerequisites

Java 1.8


### How to run?
```
cd src

javac Runner.java

java -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -Xmx2048m Runner -method lsh -dataFile ../Questions.xml  -documentsPerCycle 10 -threshold 0.1  -maxQuestions 10 -numBuckets 2147482951  -outputFile ../Output.xml -shingleLength 3 -numShingles 3200000 -numHashes 25 -numBands 10 -rowsPerBand 10
```

## Parameters
```
* k (shingle-length) Number of characters for shingles construction
* number-of-Shingles The maximum number of unique shingles
* number-of-hashes The number of hash functions used to construct the signature matrix
* number-of-bands The number of subsections in which the signature matrix is going to be processed
* rows-per-band The number of rows per each band
* number-of-buckets The maximum unique number of hash values that the algorithm is going to assign to the classiffied objects
* approximate-threshold The maximum unique number of hash values that the algorithm is going to assign to the classified objects
```

## Output

The program will return a file named Output.xml with all possible duplicate pairs.


## Built With

* java version "1.8.0_191"   Java(TM) SE Runtime Environment (build 1.8.0_191-b12)      


## Authors

* **Roger Granda** - *Initial work* - [DTAI]

## Disclaimer

This version still has erros to be corrected.

## Acknowledgments

* DTAI


