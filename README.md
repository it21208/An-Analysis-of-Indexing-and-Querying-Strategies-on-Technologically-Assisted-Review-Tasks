[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4698334.svg)](https://doi.org/10.5281/zenodo.4698334)
#### Paper submission for SIGIR 2018
#### Author: Alexandros Ioannidis
#### Date: February 2018


#### Part 1. The execution of the IndexerApp and RetrievalApp of Lucene4IR has been automated for certain scenarios 
##### (different indexers: 
##### (1) Title + Abstract, 
##### (2) 1 + Author + Journal Title + Year, 
##### (3) 2 + Mesh Headings, 
##### (4) 2 + MedlineTA, 
##### (5) 2 + Mesh Headings + MedlineTA 

##### and different query parsers (A) Title, (B) Normalized Query, (C) A + B and (D) {to be done} Boolean Query  

##### Requirements: the 'lucene4ir-master' https://github.com/lucene4ir/lucene4ir
##### and the 'tar-master' https://github.com/CLEF-TAR/tar projects need to be downloaded/cloned. 
##### The trec_eval http://trec.nist.gov/trec_eval/ also needs to be installed and added to the PATH of the user.

1. We place the ipython notebook 'execute_IndexerApp_and_RetrievalApp.ipynb' in the Root directory of the 'lucene4ir-master' project
2. We open with Jupyter Notebook the ipython notebook 'execute_RetrievalApp.ipynb' and adjust the path to our 'lucene4ir-master' project  in lines 9, 16, 18, 28 and 37.
3. In the 'lucene4ir-master' we create different classes for each indexer, and in the IndexerApp class and in 'execute_IndexerApp_and_RetrievalApp.ipynb' we update the names of the available index_types. 
4. We also need to add the different query parser files in a 'query_parsers' folder inside the /lucene4ir-master/data/pubmed/ directory.

##### The execution of all the different index/retrieval combinations (index methods and query parsers) are implemented by the ipython notebook.

#### Part 2.
1. We copy paste the results from Part 1. into a folder named 'results' inside the directory path '/lucene4ir-master/data/pubmed/'
2. We place the 'trec_and_tar_eval_automation.ipynb' in the '/lucene4ir-master/data/pubmed/' directory.
3. We open with Jupyter Notebook the file 'trec_eval_automation.ipynb' which is placed in the folder-directory 'data/pubmed/', adjust the paths in lines 7, 18 and 20 and execute it.
 
