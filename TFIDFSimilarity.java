/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene4ir.similarity;


import java.io.IOException;
import java.lang.Object;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
//import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
/**
 *
 * @author Alexandros
 */
// you will have to know the document id. This is an internal lucene id and it usually changes on every index update
// IndexReader.getTermFreqVectors(int docno);
// How to get the term frequency and document frequency of a term
// Term termInstance = new Term("content", termText);
// long termFreq = reader.totalTermFreq(termInstance);
public class TFIDFSimilarity extends Similarity {
    
    float k1;
    float b;
    
    public TFIDFSimilarity(float k1, float b){
        if (Float.isFinite(k1) == false || k1 < 0)
	    throw new IllegalArgumentException("k1 = " + k1);
	if (Float.isNaN(b) || b < 0 || b > 1)
	    throw new IllegalArgumentException("b = " + b);
	this.k1 = k1;
	this.b  = b;
    }
    
    public TFIDFSimilarity() {
        /* force it to balk */
	this.k1 = -1;
	this.b  = -1;
	if (k1 < 0 || b < 0)
	    throw new IllegalArgumentException("Must set k1 and b, no defaults.");
    }
    
    public float coord(int overlap, int maxOverlap) { return 1f; }
    public float queryNorm(float valueForNormalization) { return 1f; }
    protected float idf(long n, long N) { return (float) Math.log(1 + (N - n + 0.5D)/(n + 0.5D)); }
    @Override
    public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats)
    {
	long  N, n;
	float idf_, avdl;
	idf_ = 1.0f;
	N    = collectionStats.docCount();
	if (N == -1)
	    N = collectionStats.maxDoc();
	avdl = collectionStats.sumTotalTermFreq() / N;
	if (termStats.length == 1) {
	    n    = termStats[0].docFreq();
	    idf_ = idf(n, N);
	}
	else { /* computation for a phrase */
	    for (final TermStatistics stat : termStats) {
		n     = stat.docFreq();
		idf_ += idf(n, N);
	    }
	}
	return new TFIDFWeight(collectionStats.field(), idf_, avdl);
    }
    
    @Override
        public final SimScorer simScorer(SimWeight sw, LeafReaderContext context) throws IOException
    {
	TFIDFWeight tw = (TFIDFWeight) sw;
	return new TFIDFScorer(tw, context.reader().getNormValues(tw.field));
    }
    
    public class TFIDFScorer extends SimScorer
    {
	private final TFIDFWeight tw;
	private final NumericDocValues norms;
	TFIDFScorer(TFIDFWeight tw, NumericDocValues norms) throws IOException
	{
	    this.tw    = tw;
	    this.norms = norms;
	}
	@Override
	public float score(int doc, float tf)
	{
	    float idf_, dl, avdl, K, w;
            // float tfIdf = (float) (freq * Math.log(numNonEmptyDocs * 1.0 / docFreq));
	    idf_ = tw.idf_;
	    avdl = tw.avdl;
	    dl   = (float)norms.get(doc);
	    K    = k1 * (1.0f - b + b * (dl / avdl));
	    w    = ((k1 + 1.0f) * tf) / (K + tf) * idf_;
//            System.out.println("doc = "+ doc + "    dl = " + dl + "    w = " + w);
	    return w;
	}
	@Override
	public float computeSlopFactor(int distance) { return 1.0f;} // return 1.0f / (distance + 1);
	@Override
	public float computePayloadFactor(int doc, int start, int end, BytesRef payload) { return 1.0f;	}
    }
    
    public static class TFIDFWeight extends SimWeight
    {
	private final String field;
	private final float  idf_;
	private final float  avdl;
	public TFIDFWeight(String field, float idf_, float avdl)
	{
	    this.field = field;
	    this.idf_  = idf_;
	    this.avdl  = avdl;
	}
	@Override
	public float getValueForNormalization() { return 1.0f; }
	@Override
	public void normalize(float queryNorm, float boost) {}
    }       
    
    
        @Override
        public long computeNorm(FieldInvertState state) {
            return state.getLength();
        }  
}
