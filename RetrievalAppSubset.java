package lucene4ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlRootElement;
import static lucene4ir.RetrievalApp.SimModel.BM25;
import static lucene4ir.RetrievalApp.SimModel.LMD;
import static lucene4ir.RetrievalApp.SimModel.PL2;
import lucene4ir.similarity.BM25LSimilarity;
import lucene4ir.similarity.BM25Similarity;
import lucene4ir.similarity.OKAPIBM25Similarity;
import lucene4ir.similarity.SMARTBNNBNNSimilarity;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.IOUtils.read;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AfterEffect;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BasicModel;
import org.apache.lucene.search.similarities.BasicModelD;
import org.apache.lucene.search.similarities.BasicModelP;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.NormalizationH2;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

/*
* author: Alexandros Ioannidis
*/
public class RetrievalAppSubset {
    public RetrievalParams p;
    protected Similarity simfn;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
    protected QueryParser parser;
    protected LMSimilarity.CollectionModel colModel;
    protected String fieldsFile;
    protected String qeFile;
    /*----------------------------------------------------------------------------------------------*/
    protected enum SimModel {DEF, BM25, BM25L, LMD, LMJ, PL2, TFIDF, OKAPIBM25, SMARTBNNBNN, DFR  }

    protected RetrievalApp.SimModel sim;
    private void setSim(String val){
        try {
            sim = RetrievalApp.SimModel.valueOf(p.model.toUpperCase());
        } catch (Exception e){
            System.out.println("Similarity Function Not Recognized - Setting to Default");
            System.out.println("Possible Similarity Functions are:");
            for(RetrievalApp.SimModel value: RetrievalApp.SimModel.values()){
                System.out.println("<model>"+value.name()+"</model>");
            }
            sim = RetrievalApp.SimModel.DEF;
        }
    }
    /*-----------------------------------------------------------------------------------------------*/
    public void selectSimilarityFunction(RetrievalApp.SimModel sim){
        colModel = null;
        switch(sim){
            case OKAPIBM25:
                System.out.println("OKAPI BM25 Similarity Function");
                simfn = new OKAPIBM25Similarity(1.2f, 0.75f);
                break;
            case SMARTBNNBNN:
                System.out.println("SMART bnn.bnn Similarity Function");
                simfn = new SMARTBNNBNNSimilarity();
            case BM25:
                System.out.println("BM25 Similarity Function");
                simfn = new BM25Similarity(p.k, p.b);
                break;
            case BM25L:
                System.out.println("BM25L Similarity Function");
                simfn = new BM25LSimilarity(p.k, p.b, p.delta);
                break;
            case LMD:
                System.out.println("LM Dirichlet Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMDirichletSimilarity(colModel, p.mu);
                break;
            case LMJ:
                System.out.println("LM Jelinek Mercer Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMJelinekMercerSimilarity(colModel, p.lam);
                break;
            case PL2:
                System.out.println("PL2 Similarity Function (?)");
                BasicModel bm = new BasicModelP();
                AfterEffect ae = new AfterEffectL();
                Normalization nn = new NormalizationH2(p.c);
                simfn = new DFRSimilarity(bm, ae, nn);
                break;
            case DFR:
                System.out.println("DFR Similarity Function with no after effect (?)");     
                BasicModel bmd = new BasicModelD();
                AfterEffect aen = new AfterEffect.NoAfterEffect();
                Normalization nh1 = new NormalizationH1();
                simfn = new DFRSimilarity(bmd, aen, nh1);
                break;
            default:
                System.out.println("Default Similarity Function");
                simfn = new BM25Similarity();
                break;
        }
    }
    /*------------------------------------------------------------------------------------------------*/
        public void readParamsFromFile(String paramFile){
        /*    Reads in the xml formatting parameter file.  Maybe this code should go into the RetrievalParams class.
        Actually, it would probably be neater to create a ParameterFile class which these apps can inherit from - and customize accordinging.   */
        System.out.println("Reading parameters...");
        try { p = JAXB.unmarshal(new File(paramFile), RetrievalParams.class); }
        catch (Exception e){ 
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            System.exit(1);
        }
        setSim(p.model);
        if (p.maxResults==0.0) {p.maxResults=1000;}
        if (p.b < 0.0){ p.b = 0.75f;}
        // add the restrictions for k3 here
        if (p.beta <= 0.0){p.beta = 500f;}
        if (p.k <= 0.0){ p.k = 1.2f;}
        if (p.delta<=0.0){p.delta = 1.0f;}
        if (p.lam <= 0.0){p.lam = 0.5f;}
        if (p.mu <= 0.0){p.mu = 500f;}
        if (p.c <= 0.0){p.c=10.0f;}
        if (p.model == null) {  p.model = "def";  }
        if (p.runTag == null){ p.runTag = p.model.toLowerCase(); }
        if (p.resultFile == null){ p.resultFile = p.runTag+"_results.res"; }
        fieldsFile = p.fieldsFile;
        qeFile=p.qeFile;
        System.out.println("Path to index: " + p.indexName);
        System.out.println("Query File: " + p.queryFile);
        System.out.println("Result File: " + p.resultFile);
        System.out.println("Model: " + p.model);
        System.out.println("Max Results: " + p.maxResults);
        if (sim==BM25) {
            System.out.println("b: " + p.b);
            System.out.println("k: " + p.k);
        }
        else if (sim==PL2){ System.out.println("c: " + p.c); }
        else if (sim==LMD){ System.out.println("mu: " + p.mu); }
        if (p.fieldsFile!=null){ System.out.println("Fields File: " + p.fieldsFile); }
        if (p.qeFile!=null){ System.out.println("QE File: " + p.qeFile); }
        if (p.tokenFilterFile != null){
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(p.tokenFilterFile);
        }
        else{ analyzer = Lucene4IRConstants.ANALYZER; }
        System.out.println("Subset (qrels) File: " + p.subsetFile);
    }
    /*---------------------- Modified the processQueryFile() method to return only the specified subset of documents ---------*/    
    public void processQueryFile() throws IOException{
        /*   Assumes the query file contains a qno followed by the query terms.  One query per line. i.e.
        Q1 hello world
        Q2 hello hello
        Q3 hello etc       */
        System.out.println("Processing Query File..."); 
        // added by Alex
        HashMap<String, List<Integer>> mapOfSubsets = readSpecifiedSubsets(p.subsetFile); // Create a HashMap of key as String List Of integers as Value
        System.out.println(mapOfSubsets);  // print HashMap structure that contains the qrels subsets specified
        try {
            BufferedReader br = new BufferedReader(new FileReader(p.queryFile));
            File file = new File(p.resultFile);
            FileWriter fw = new FileWriter(file);
            try {
                String line = br.readLine();
                while (line != null){
                    String[] parts = line.split(" ");
                    String qno = parts[0];
                    String queryTerms = "";
                    for (int i=1; i<parts.length; i++)
                        queryTerms = queryTerms + " " + parts[i];
                    ScoreDoc[] scored = runQuery(qno, queryTerms.trim());
                    int n = Math.min(p.maxResults, scored.length);
                    for(int i=0; i<n; i++){
                        Document doc = searcher.doc(scored[i].doc);
                        String docno = doc.get("docnum"); // docno is the value to be checked
                        Iterator it = mapOfSubsets.entrySet().iterator(); // Get the iterator over the HashMap 
                        while (it.hasNext() == true) {
                            HashMap.Entry pair = (HashMap.Entry)it.next();
                            if (qno.equals(pair.getKey())) {
                                List<Integer> temp = (List<Integer>) pair.getValue();
                                if (temp.contains(Integer.parseInt(docno)) == true) {
                                    fw.write(qno + " QO " + docno + " " + (i+1) + " " + scored[i].score + " " + p.runTag);
                                    fw.write(System.lineSeparator()); 
                                } 
                            }
                        }                          
                    }
                    line = br.readLine();
                }
            } finally {
                br.close();
                fw.close();
            }
        } catch (Exception e){System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage()); }
    }
    /*-------------------------------------------------------------------------------------------*/
    public ScoreDoc[] runQuery(String qno, String queryTerms){
        ScoreDoc[] hits = null;
        System.out.println("Query No.: " + qno + " " + queryTerms);
        try {
            Query query = parser.parse(QueryParser.escape(queryTerms));
            try {
                TopDocs results = searcher.search(query, p.maxResults);
                hits = results.scoreDocs;
            }
            catch (IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }
        } catch (ParseException pe){
            pe.printStackTrace();
            System.exit(1);
        }
        return hits;
    }
    /* ------------ Read the specified subset to return for each query-topic -------------------- */
    public HashMap<String, List<Integer>> readSpecifiedSubsets(String subsetFile) throws IOException {
            File file = new File(subsetFile); 
            HashMap<String, List<Integer>> mapOfSubsets = new HashMap<>();
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    String queryTopic;
                    Integer PMID;
                    List<Integer> list = new ArrayList<Integer>();   
                    String temp = br.readLine();                    
                    String tempQueryTopic = temp.split("\\s+")[0];
                    list.add( Integer.parseInt( temp.split("\\s+" )[2]) );
                    while ((line = br.readLine()) != null) {
                       String[] splited = line.split("\\s+"); 
                       queryTopic = splited[0];
                       PMID = Integer.parseInt(splited[2]);
                       if (queryTopic.equals(tempQueryTopic)) { 
                           list.add(PMID); 
                       } else {  // Add List of specified PMIDs as value in the map 
                           mapOfSubsets.put(tempQueryTopic,list); //for specific query-topic
                           list = new ArrayList<Integer> ();
                           list.add(PMID);
                           tempQueryTopic = queryTopic;
                       }  
                    }
        mapOfSubsets.put(tempQueryTopic,list);
        return mapOfSubsets;
    }
/*---------------------------------------------------------------------------------------------------------------------------*/    
/*    public HashMap<String, List<Integer>> readSpecifiedSubsets() throws IOException{
            File folder = new File("/home/pfb16181/NetBeansProjects/lucene4ir-master/data/pubmed/SpecifySubsetOfDocumentsForEachQueryTopic");
            HashMap<String, List<Integer>> mapOfSubsets = new HashMap<>();
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
              File file = listOfFiles[i];
              if (file.isFile()) {
                String content = FileUtils.readFileToString(file);
                Scanner scanner = new Scanner(content);
                List<Integer> list = new ArrayList<Integer>();
                while (scanner.hasNextInt()) { list.add(scanner.nextInt()); } // get list of Integer from String
                // Add List of specified PMIDs as value in the map for specific query-topic
                mapOfSubsets.put(file.getName(), list) ;                                
              } 
            }
            return mapOfSubsets;
        } 
*/ /*-----Write a modified version of RetrievalApp to only return the specified subset of documents---*/
        public RetrievalAppSubset(String retrievalParamFile) throws IOException{
        System.out.println("Retrieval App Subset");
        System.out.println("Param File: " + retrievalParamFile);
        readParamsFromFile(retrievalParamFile);
        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(p.indexName).toPath()) );
            searcher = new IndexSearcher(reader);
            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);
            parser = new QueryParser(Lucene4IRConstants.FIELD_ALL, analyzer);
        } catch (Exception e){ System.out.println(" caught a " + e.getClass() +  "\n with message: " + e.getMessage()); }
    }
    /*---------------------------------------------------------------------------------------------*/    
    public static void main(String []args) throws IOException {
        String retrievalParamFile = "";
        try { retrievalParamFile = args[0]; }
        catch (Exception e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            System.exit(1);
        }
        // created retriever object for new class RetrievalAppSubset
        RetrievalAppSubset retriever = new RetrievalAppSubset(retrievalParamFile);
        retriever.processQueryFile();
    }  
}
