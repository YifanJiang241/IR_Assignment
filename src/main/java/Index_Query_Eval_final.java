

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.lang.model.util.Elements;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.charfilter.BaseCharFilter;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
// import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AxiomaticF1EXP;
import org.apache.lucene.search.similarities.AxiomaticF1LOG;
import org.apache.lucene.search.similarities.AxiomaticF2EXP;
import org.apache.lucene.search.similarities.AxiomaticF2LOG;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.NodeList;
import org.apache.lucene.document.Field;
import org.jsoup.Jsoup;

import org.xml.sax.SAXException;

public class Index_Query_Eval_final {

    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "index";

    private static int MAX_RESULTS = 1000;

    private static Analyzer globAnalyzer = null;

    private static IndexWriter iwriter;

    private final static String DOCNO = "DOCNO";
    private final static String PROFILE = "PROFILE";
    private final static String DATE = "DATE";
    private final static String HEADLINE = "HEADLINE";
    private final static String BYLINE = "BYLINE";
    private final static String DATELINE = "DATELINE";
    private final static String TEXT = "TEXT";
    private final static String PUB = "PUB";
    private final static String PAGE = "PAGE";
    private final static String XX = "XX";
    private final static String CO = "CO";
    private final static String CN = "CN";
    private final static String IN = "IN";
    private final static String TP = "TP";
    private final static String PE = "PE";
    private final static String[] tags = { DOCNO, PROFILE, DATE, HEADLINE, BYLINE, DATELINE, TEXT, PUB, PAGE, XX, CO,
            CN,
            IN, TP, PE };

    public Directory getDirectory() {
        return directory;
    }

    public static void setDirectory() throws IOException {
        directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
    }

    private static Directory directory = null;

    public static Analyzer getGlobAnalyzer() {
        return globAnalyzer;
    }

    public static void setGlobAnalyzer(Analyzer globAnalyzer) {
        Index_Query_Eval_final.globAnalyzer = globAnalyzer;
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, SAXException, ParserConfigurationException, ParseException {
        // setGlobAnalyzer(new StandardAnalyzer());
        // setGlobAnalyzer(new WhitespaceAnalyzer());
        // setGlobAnalyzer(new SimpleAnalyzer());
        setGlobAnalyzer(new EnglishAnalyzer());
        // setGlobAnalyzer(CustomAnalyzer());

        // deleteINDEX();
        // setDirectory();
        // intializeIWriter();
        // Index_LA();
        // Index_FT();
         Index_FBIS();
        // iwriter.close();
        // directory.close();

        // Query();
        // Query();

        Eval();
    }

    public static void deleteINDEX() {
        if (new File(INDEX_DIRECTORY).exists()) {
            new File(INDEX_DIRECTORY).delete();
        }
    }

    public static void intializeIWriter() throws IOException {
        Analyzer analyzer = getGlobAnalyzer();

        // // ArrayList of documents in the corpus

        // // Open the directory that contains the search index
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // // Set up an index writer to add process and save documents to the index
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        iwriter = new IndexWriter(directory, config);
    }

    public static Analyzer CustomAnalyzer() throws IOException {

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer("standard")
                .addTokenFilter("lowercase")
                .addTokenFilter("stop")
                .addTokenFilter("removeduplicates")
                .addTokenFilter("trim")
                .build();

        return analyzer;
    }

    private static void indexDocs(Document doc, String filename)
            throws IOException, ParserConfigurationException, SAXException {

        NodeList children = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element e1 = (Element) child;
                NodeList nodeList = e1.getElementsByTagName("*");
                // NodeList nodeList = e1.getChildNodes();
                org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
                for (int idx = 0; idx < nodeList.getLength(); idx++) {
                    // Get element
                    Element element = (Element) nodeList.item(idx);
                    String tagname = element.getNodeName();
                    // System.out.println(tagname+"--"+element.getParentNode().getNodeName());
                    if (element.getParentNode().getNodeName() != "DOC") {
                        continue;
                    }
                    String tagvalue = e1.getElementsByTagName(element.getNodeName()).item(0).getTextContent().trim();
                    luceneDocument.add(new TextField(tagname, tagvalue, Field.Store.YES));
                    // fields.put(tagname,filename+e1.getElementsByTagName("DOCNO").item(0).getTextContent());
                }
                luceneDocument.add(new StringField("filename", filename, Field.Store.YES));
                iwriter.addDocument(luceneDocument);
            }
        }

    }

    public static void Index_LA() throws IOException, SAXException, ParserConfigurationException {

        // Analyzer that is used to process TextField

        //
        File files[] = new File("/Users/akashgarg/Downloads/Lucene-Information-Retrieval-2/Files/docs/latimes")
                .listFiles();
        assert files != null;
        for (File file : files) {
            List<InputStream> streams = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
                    new FileInputStream(file), new ByteArrayInputStream("</root>".getBytes()));
            if (!file.getAbsolutePath().contains("read")) {
                InputStream is = new SequenceInputStream(Collections.enumeration(streams));
                Document doc1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                indexDocs(doc1, file.getAbsolutePath());
            }
        }
        // iwriter.close();

        System.out.println("Indexing Complete LA");
    }

    public static ArrayList<HashMap<String, String>> parseQueries(String parsedQuery) throws IOException {
        ArrayList<HashMap<String, String>> res = new ArrayList<HashMap<String, String>>();
        File f = new File(parsedQuery);
        FileReader freader = new FileReader(f);
        BufferedReader br = new BufferedReader(freader);
        HashMap<String, String> item = new HashMap<String, String>();
        String line = "";
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("<") && line.endsWith(">")) {
                item.put(line, br.readLine());
                item.put(br.readLine(), br.readLine());
                res.add(item);
                item = new HashMap<String, String>();
            }
        }
        freader.close();
        return res;
    }

    public static void Index_FBIS() throws IOException, SAXException, ParserConfigurationException {

        // Analyzer that is used to process TextField

        //
        File files[] = new File("/Users/akashgarg/Downloads/Lucene-Information-Retrieval-2/Files/docs/fbis")
                .listFiles();
        assert files != null;
        for (File file : files) {

            String contents = new String(Files.readAllBytes(Paths.get(file.getPath())));

            contents = contents.replaceAll("&.*?;", "");
            contents = contents.replaceAll(" P=[0-9]+", "");
            contents = contents.replaceAll(" ID=[A-Z0-9]*-[A-Z0-9]*-[A-Z0-9]*-[A-Z0-9]*", "");
            contents = contents.replaceAll("<3>", "");
            contents = contents.replaceAll("</3>", "");
            contents = contents.replaceAll("&-\\|", "");
            contents = contents.replaceAll("\\|amp;", "");
            contents = contents.replaceAll("&\\|", "");
            contents = contents.replaceAll("\\|yen;", "");
            InputStream inputStream = new ByteArrayInputStream(contents.getBytes(Charset.forName("UTF-8")));
            List<InputStream> streams = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()), inputStream,
                    new ByteArrayInputStream("</root>".getBytes()));
            if (!file.getAbsolutePath().contains("read")) {
                InputStream is = new SequenceInputStream(Collections.enumeration(streams));
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                indexDocs(doc,file.getAbsolutePath());
            }
        }

        System.out.println("Indexing Complete FBIS");
    }


    public static void Query() throws IOException {

        // Initialize the IndexSearcher and Analyzer
        FSDirectory indexx = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexSearcher isearcher = new IndexSearcher(DirectoryReader.open(indexx));
        isearcher.setSimilarity(new ClassicSimilarity());
        Analyzer analyzer = new EnglishAnalyzer();

        // Create a multi-field parser
        String[] fields = { "DOCNO", "PROFILE", "DATE", "HEADLINE", "BYLINE", "DATELINE", "TEXT", "PUB", "PAGE" };
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);

        FileWriter output = null;
        String outputfn = "result";
        output = new FileWriter(outputfn);

        // Read the queries from the file line by line
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader("/Users/akashgarg/Desktop/WebSearch/topics"));
            // Format of the topics
            // <top>
            // <num> Number: 1
            // <desc> Description:
            // <narr> Narrative:
            // </top>

            // Create queries from the topics
            String line;
            int count = 0;
            StringBuilder content = new StringBuilder();
            String parseid = "";

            while ((line = bufferedReader.readLine()) != null) {
                // System.out.println(line);
                // Top tag
                if (line.contains("<top>")) {
                    while (!line.contains("<num>")) {
                        line = bufferedReader.readLine();
                    }
                }

                // Num tag
                if (line.contains("<num>")) {
                    line = line.substring("<num> Number: ".length()); // remove tags
                    content.append(line).append(" ");
                    // System.out.println("num " + content.toString());
                    parseid = content.toString();
                    line = bufferedReader.readLine();
                }

                // title tag
                if (line.contains("<title>")) {
                    line = line.substring("<title> ".length()); // remove tags
                    while (!line.contains("<desc>")) {
                        content.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    // System.out.println("title " + content.toString());
                }

                // Desc tag
                if (line.contains("<desc>")) {
                    // line = line.substring("<desc> Description: ".length()); // remove tags
                    line = bufferedReader.readLine();
                    while (!line.contains("<narr>")) {
                        content.append(line).append(" ");
                        line = bufferedReader.readLine();

                    }
                    // System.out.println("desc " + content.toString());

                }

                // Narr tag
                if (line.contains("<narr>")) {
                    // line = line.substring("<narr> Narrative: ".length()); // remove tags$
                    line = bufferedReader.readLine();
                    while (!line.contains("</top>")) {
                        content.append(line).append(" ");
                        line = bufferedReader.readLine();
                    }
                    // System.out.println("narr " + content.toString());

                }

                // End top tag
                if (line.contains("</top>")) {
                    line = bufferedReader.readLine();

                    // Delete question mark
                    if (content.toString().contains("?")) {
                        content = new StringBuilder(content.toString().replace("?", ""));
                    }
                    // Delete slash
                    if (content.toString().contains("/")) {
                        content = new StringBuilder(content.toString().replace("/", ""));
                    }
                    // Delete slash
                    if (content.toString().contains("\"")) {
                        content = new StringBuilder(content.toString().replace("\"", ""));
                    }

                    // Delete spaces at the beginning and at the end
                    content = new StringBuilder(content.toString().trim());
                    // System.out.println(content.toString());

                    // Create a query from the content
                    Query query = parser.parse(content.toString());
                    // Search the index
                    TopDocs results = isearcher.search(query, MAX_RESULTS);
                    // Display results
                    // System.out.println("Results for query: " + content.toString());
                    for (int i = 0; i < results.scoreDocs.length; i++) {
                        org.apache.lucene.document.Document hitDoc = isearcher.doc(results.scoreDocs[i].doc);
                        output.write(
                                parseid + " Q0 " + hitDoc.get("DOCNO") + " " + i + " " + results.scoreDocs[i].score
                                        + " STANDARD" + "\n");

                        // System.out.println(hitDoc.get("DATE"));
                        // System.out.println(hitDoc.get("DOCNO"));
                        // System.out.println(hitDoc.get("DOCNO") + " " + results.scoreDocs[i].score);
                    }

                    // System.out.println("------------------------------------------------");
                    // Reset content
                    content = new StringBuilder();
                }

            }
            output.close();

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public static void Eval() throws IOException, InterruptedException {
        // Command and arguments
        String command = "/Users/akashgarg/Desktop/WebSearch/trec_eval-9.0.7/trec_eval";
        // String argument1 = "/Users/akashgarg/Desktop/WebSearch/refactor_cranqel";
        String argument1 = "/Users/akashgarg/Downloads/Lucene-Information-Retrieval-2/Files/qrels.assignment2.part1";

        String argument2 = "/Users/akashgarg/Desktop/WebSearch/result";
        // String argument2 = "/Users/akashgarg/Desktop/WebSearch/results";

        // Create a process builder
        ProcessBuilder processBuilder = new ProcessBuilder(command, argument1, argument2);

        // Redirect standard output and error to the console
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to complete
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.err.println("Command failed with exit code: " + exitCode);
        }
        System.out.println("Evaluation Done");
    }

    public static void Index_FT() throws IOException {

        // Read all the files from the directory ft911
        File folder = new File("/Users/akashgarg/Downloads/Lucene-Information-Retrieval-2/Files/docs/ft");
        File[] listOfFolders = folder.listFiles();

        // To store an index in memory
        // Directory directory = new RAMDirectory();
        // To store an index on disk

        // Loop through all the files in the directory
        // listOfFolders = Arrays.asList(listOfFolders).subList(7,
        // listOfFolders.length).toArray(
        // new File[listOfFolders.length - 7]);

        for (File file : listOfFolders) {

            if (file.isDirectory()) {

                // Loop through all the files in the directory
                for (File file2 : file.listFiles()) {
                    // System.out.println(file2.getName());
                    if (file2.isFile()) {
                        // Parse the file using Jsoup
                        org.jsoup.nodes.Document document = Jsoup.parse(file2, "UTF-8");

                        // Select all the DOC tags
                        org.jsoup.select.Elements elements = document.select("DOC");

                        // Loop through all the DOC tags
                        for (org.jsoup.nodes.Element element : elements) {
                            // Create a new Lucene document
                            org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

                            // Loop through all the tags
                            for (String tag : tags) {
                                // Get the text of the tag
                                String text = element.select(tag).text();

                                // If the text is not empty
                                if (!text.isEmpty()) {
                                    doc.add(new TextField(tag, text, Field.Store.YES));
                                }
                            }

                            // Add the document to the index
                            iwriter.addDocument(doc);
                        }
                    }
                }
            }
        }

        // Commit changes and close the index writer and directory to finish indexing
        // the documents to avoid memory
        // leaks and unnecessary consumption of resources
        // iwriter.close();
        System.out.println("Indexing Done FT");
    }

}
