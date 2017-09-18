/**
 * Created by Cwang on 10/31/16.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class LyricProcessor {


    public static final String FIELD_PATH = "path";
    public static final String FIELD_CONTENTS = "contents";

    private static HashMap<String, Float> softScores = new HashMap<>();
    private static HashMap<String, Float> hardScores = new HashMap<>();

    private String[] neutralWords = {
        "her", "girl", "woman", "women", "she", "ladies", "lady", "sex"
    };

    // Remove from hard drake if these appear
    // Take away all 1 pt hard points
    private static String[] SOFT_KEYS = {
        "Rihanna", "Stevie Wonder", "jealous", "ex", "exes", "feelings", "lipstick", "lovin", "married", "love", "lovin",
            "girl", "she", "hate", "emotion", "proud", "women"
    };

    // Take away 1 pt soft points
    private static String[] HARD_KEYS = {
         "faded", "Lil Wayne", "trigger", "nigga", "niggas", "shit", "fuck", "tellin", "man",
            "drink", "money", "yolo", "daddy",
    };

    public LyricProcessor(String album){
        try {
            initialize(album);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        LyricProcessor lp = new LyricProcessor("/Users/Cwang/Downloads/DrakeEngine/Lyrics/Take Care");
    }


    /**
     * Creates indices needed for Lucene search and loads all text
     * documents into the index
     *
     * Then calls getAllKeyWordScores with the index in order to perform a
     * search, analysis, and population of the hashmaps hardScores and softScores
     *
     * @param album
     * @throws IOException
     * @throws ParseException
     */
    private void initialize(String album) throws IOException, ParseException {
        //    Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // Specify the directory
        File dir = new File(album);

        // Create the index
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        // Adding all the documents to the search engine reader
        IndexWriter w = new IndexWriter(index, config);

        File[] files = dir.listFiles();
        for (File file : files) {
            Document document = new Document();

            String path = file.getName();
            document.add(new TextField(FIELD_PATH, path, Field.Store.YES));

            Reader reader = new FileReader(file);
            document.add(new TextField(FIELD_CONTENTS, reader));

            w.addDocument(document);
        }

        w.close();

        getAllKeywordScores(index);

    }

    /**
     * Taking in an index to search through all keywords,
     * @param index
     * @throws ParseException
     * @throws IOException
     */
    private static void getAllKeywordScores(Directory index) throws ParseException, IOException {

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.

        BooleanQuery hardQuery = getBooleanQuery(HARD_KEYS);
        BooleanQuery softQuery = getBooleanQuery(SOFT_KEYS);

        System.out.println("OK");
        // 3. search
        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs hardDocs = searcher.search(hardQuery, hitsPerPage);
        TopDocs softDocs = searcher.search(softQuery, hitsPerPage);


        populateKeywordScoreHash(searcher, softDocs, softScores);
        populateKeywordScoreHash(searcher, hardDocs, hardScores);

        reader.close();
    }

    /**
     * Given the TopDocs returned by Lucene, populate the hashMap of hard scores
     * and soft scores
     * @param searcher Index Lucene searched
     * @param softDocs Doc containing the score
     * @param scores Hash map to add to
     * @throws IOException
     */
    private static void populateKeywordScoreHash(IndexSearcher searcher, TopDocs softDocs, HashMap<String, Float> scores)
            throws IOException {

        // Iterate through all matches
        for(ScoreDoc match: softDocs.scoreDocs){
            Document d = searcher.doc(match.doc);
            String song = d.get("path");
            System.out.println("PATH: " + d.get("path") );

            if (!scores.containsKey(song)) {
                scores.put(song, match.score);
            } else {
                scores.put(song, scores.get(song) + match.score);
            }
            System.out.println("SCORE:   " + match.score );
            System.out.println("-----------------");
        }
        System.out.println("\n\n\n");
    }

    /**
     * Construct a boolean query based on the array of strings
     * Will be used to search using Lucene and return a score
     * @param keys
     * @return
     */
    private static BooleanQuery getBooleanQuery(String [] keys){
        BooleanQuery.Builder categoryQuery = new BooleanQuery.Builder();

        // Add all keys to the BooleanQuery
        for (String hardKey: keys ){
            Term t = new Term("contents", hardKey);
            Query q = new TermQuery(t);
            categoryQuery.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
        }

        return categoryQuery.build();
    }

}
