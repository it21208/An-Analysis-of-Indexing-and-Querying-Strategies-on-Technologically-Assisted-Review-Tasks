package io.anserini.index.generator;
import io.anserini.collection.MultifieldSourceDocument;
import io.anserini.collection.SourceDocument;
import io.anserini.index.IndexCollection;
import io.anserini.index.transform.StringTransform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;
/* Converts a {@link SourceDocument} into a Lucene {@link Document}, ready to be indexed. Prior to the creation of 
 the Lucene document, this class will apply an optional {@link StringTransform} to, for example, clean 
 HTML document  @param <T> type of the source document */
public class LuceneDocumentGenerator<T extends SourceDocument> {
  private static final Logger LOG = LogManager.getLogger(LuceneDocumentGenerator.class);
  public static final String FIELD_RAW = "raw";
  public static final String FIELD_BODY = "contents";
  public static final String FIELD_ID = "id";
  private final StringTransform transform;
  protected IndexCollection.Counters counters;
  protected IndexCollection.Args args;
  /* --- Field names taken from Lucene4IR ---- */
  public static final String FIELD_CONTENT = "content";
  public static final String FIELD_TITLE = "title";
  public static final String FIELD_AUTHOR = "author";
  public static final String FIELD_DOCNUM = "docnum";
  public static final String FIELD_PUBDATE = "pubdate";
  public static final String FIELD_SOURCE = "source";
  public static final String FIELD_MESH = "mesh";
  public static final String FIELD_ALL = "all";
  public LuceneDocumentGenerator() {  this.transform = null; }  /* Default constructor */
  /* Constructor to specify optional {@link StringTransform}. @param transform string transform to apply */
  public LuceneDocumentGenerator(StringTransform transform) { this.transform = transform; }
  /* Constructor with config and counters, @param args configuration arguments, @param counters counters */
  public LuceneDocumentGenerator(IndexCollection.Args args, IndexCollection.Counters counters) {
    this.transform = null;
    config(args);
    setCounters(counters);
  }
  /* Constructor with config and counters @param transform string transform to apply, @param args configuration arguments, @param counters counters */
  public LuceneDocumentGenerator(StringTransform transform, IndexCollection.Args args, IndexCollection.Counters counters) {
    this.transform = transform;
    config(args);
    setCounters(counters);
  }
  public void config(IndexCollection.Args args) { this.args = args; }
  public void setCounters(IndexCollection.Counters counters){ this.counters = counters; }     
  public Document createDocument(T src) {
    String id = src.id(); 
    String contents; // If there's a transform use it 
    try { contents = transform != null ? transform.apply(src.content()) : src.content(); }
    catch (Exception e) {
      LOG.error("Error extracting document text, skipping document: " + id, e);
      counters.errors.incrementAndGet();
      return null;
    }
    if (contents.trim().length() == 0) {
      counters.empty.incrementAndGet();
      return null;
    }
    final Document document = new Document(); // Make a new, empty document.    
    document.add(new StringField(FIELD_ID, id, Field.Store.YES));  // Store the collection docid.
    document.add(new StringField(FIELD_DOCNUM, id, Field.Store.YES));
    document.add(new StringField(FIELD_ALL, id, Field.Store.YES));
    document.add(new SortedDocValuesField(FIELD_ID, new BytesRef(id))); // This is needed to break score ties by docid
    document.add(new SortedDocValuesField(FIELD_DOCNUM, new BytesRef(id)));
    document.add(new SortedDocValuesField(FIELD_ALL, new BytesRef(id)));
    if (args.storeRawDocs) {
      document.add(new StoredField(FIELD_RAW, src.content()));
      document.add(new StoredField(FIELD_ALL, src.content()));
    }
    FieldType fieldType = new FieldType();
    fieldType.setStored(args.storeTransformedDocs);
    if (args.storeDocvectors) { // Are we storing document vectors?
      fieldType.setStoreTermVectors(true);
      fieldType.setStoreTermVectorPositions(true);
    }
    // Are we building a "positional" or "count" index?
    if (args.storePositions) { fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS); } 
    else { fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS); }
    document.add(new Field(FIELD_BODY, contents, fieldType));
    document.add(new Field(FIELD_ALL, contents, fieldType));
// If this document has other fields, then we want to index it also Currently we just use all the settings of the main "content" field
    if (src instanceof MultifieldSourceDocument) {
      ((MultifieldSourceDocument) src).fields().forEach((k, v) -> {
        FieldType type = new FieldType();
        type.setStored(args.storeTransformedDocs);
        if (args.storeDocvectors) {
          type.setStoreTermVectors(true);
          type.setStoreTermVectorPositions(true);
        }
        if (args.storePositions) { type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS); }
        else { type.setIndexOptions(IndexOptions.DOCS_AND_FREQS); }
        document.add(new Field(k, v, fieldType));
        document.add(new Field(FIELD_ALL, v, fieldType));
      });
    }
    return document;
  }    
}