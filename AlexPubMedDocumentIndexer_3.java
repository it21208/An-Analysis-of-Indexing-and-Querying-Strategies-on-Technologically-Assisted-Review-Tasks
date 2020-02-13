package lucene4ir.indexer;

import java.io.BufferedReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.xml.sax.InputSource;
/**
 *
 * @author alexandros
 */
public class AlexPubMedDocumentIndexer_3 extends DocumentIndexer {
        public DocumentBuilderFactory builderFactory;
    public DocumentBuilder builder;
    public XPath xPath;
    
    public AlexPubMedDocumentIndexer_3(String indexPath, String tokenFilterFile, Boolean positional){
            super(indexPath, tokenFilterFile, positional);
            builderFactory = DocumentBuilderFactory.newInstance();
            try { builder = builderFactory.newDocumentBuilder();  }
            catch (ParserConfigurationException e){ System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage()); }
            xPath =  XPathFactory.newInstance().newXPath();
    }
    
    public void indexDocumentsFromFile(String filename){
        String line = "";
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = openDocumentFile(filename);
            try {
                line = br.readLine();
                while (line != null){
                    line = line.replaceAll("^\\s+","");
                    if (line.startsWith("<PubmedArticle>")) { text = new StringBuilder(); }
                    text.append(line + "\n");
                    if (line.startsWith("</PubmedArticle>")){
                        indexPubMedDocument(text.toString());
                        text.setLength(0);  
                    }
                    line = br.readLine();
                }
            } finally { br.close(); }
        } catch (Exception e){ System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());  }
    }

    public void indexPubMedDocument(String text){
        try {
        org.w3c.dom.Document xmlDocument = builder.parse(new InputSource(new StringReader(text)));
        Document doc = new Document();
        String docid = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/PMID");
        Field docnumField = new StringField("docnum", docid, Field.Store.YES);
        doc.add(docnumField);
        String pubyear = getStringFromXml(xmlDocument,"/PubmedArticle/MedlineCitation/DateCreated/Year");
        if (pubyear.isEmpty()) {    System.out.println(docid + " " + pubyear);  }
        Field yearField = new StringField("year", pubyear, Field.Store.YES);
        doc.add(yearField);
        String title = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/ArticleTitle");
        String content = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Abstract");
        String journal = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/Journal/Title");        
        String authors = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/Article/AuthorList");
        String mesh = getStringFromXml(xmlDocument, "/PubmedArticle/MedlineCitation/MeshHeadingList");
        addTextFieldToDoc(doc, "title", title);        
        addTextFieldToDoc(doc, "content", content);     
        addTextFieldToDoc(doc, "journal", journal);      
        addTextFieldToDoc(doc, "authors", authors);           
        addTextFieldToDoc(doc, "mesh", mesh); 
       addTextFieldToDoc(doc, Lucene4IRConstants.FIELD_ALL, title + " " + authors + " " + journal + " " + content +" " + mesh ); 
        System.out.println("Indexing: "+ docid);
        addDocumentToIndex(doc);
        } catch (Exception e) { System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage()); }
    }

    public void addTextFieldToDoc(Document doc, String fieldname, String fielddata){
        Field field;
        if (indexPositions) { field = new TermVectorEnabledTextField(fieldname, fielddata, Field.Store.YES); }
        else { field = new TextField(fieldname, fielddata, Field.Store.YES); }
        doc.add(field);
    }

    public String getStringFromXml(org.w3c.dom.Document xmlDocument, String expression){
        String text = "";
        try { text = xPath.compile(expression).evaluate(xmlDocument).trim(); } 
        catch (XPathExpressionException e) { System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage()); }
        return text;
    }
}
