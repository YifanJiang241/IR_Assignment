package Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import Index.FbisIndexer;


public class IndexeAll {
    static ArrayList<Document> allDocuments = new ArrayList<>();
    static FbisIndexer fbisIn = new FbisIndexer();


    public static ArrayList<Document> dataIndexer() {
        allDocuments.addAll(fbisIn.getDocuments());

        return allDocuments;
    }

    public static void Indexer(Analyzer analyzer, Similarity similarity) throws IOException {
        dataIndexer();
        Directory directory;
        directory = FSDirectory.open(Paths.get(LuceneContstants.INDEX_LOC));
        // Configure the index writer
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // use the similarity_chosen
        iwc.setSimilarity(similarity);

        IndexWriter indexWriter = new IndexWriter(directory, iwc);
        //Add the documents parsed from the dataset into the indexWriter
        indexWriter.addDocuments(allDocuments);
        indexWriter.close();
        System.out.println(String.valueOf(allDocuments.size()) + " documents have been indexed \nIndexing complete");
    }

    public static void generateIndex(Analyzer analyzer, Similarity similarity) throws IOException {
        Indexer(analyzer, similarity);
    }
}