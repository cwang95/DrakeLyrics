import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Cwang on 10/31/16.
 */
public class WordCounter {

    File dir;

    public WordCounter( String myDirectoryPath ) {
        this.dir = new File(myDirectoryPath);
    }

    public HashMap<String, Integer> countWords(){
        File[] directoryListing = this.dir.listFiles();
        if (directoryListing != null) {

            List<String> allWords = new ArrayList<>();

            HashMap<String, Integer> wordCounts = new HashMap<>();

            for (File child : directoryListing) {
                // Do something with child
                System.out.println(child.toString());
                Scanner input = null;
                try {
                    input = new Scanner(child);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // count occurrences
                while (input.hasNext()) {
                    String next = input.next().toLowerCase();
                    if (!wordCounts.containsKey(next)) {
                        wordCounts.put(next, 1);
                    } else {
                        wordCounts.put(next, wordCounts.get(next) + 1);
                    }
                }
                Scanner console = new Scanner(System.in);

                // get cutoff and report frequencies
                System.out.println("Total words = " + wordCounts.size());
            }
            return sortHashMapByValues(wordCounts);

        } else {
            System.out.println("Something went wrong with myDirectoryPath");
        }
        return null;
    }

    /**
     * Return a sorted map given unsorted map
     * From: http://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
     * @param passedMap Map to be sorted
     * @return passedMap sorted by value, ie number of appearances
     */
    public LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap =
                new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    /*
    public static void main( String[] args ){
        WordCounter wc = new WordCounter("/Users/Cwang/Downloads/DrakeEngine/Lyrics/Soft Songs");
        HashMap<String, Integer> wordCounts = wc.countWords();

        for (String word : wordCounts.keySet()) {
            int count = wordCounts.get(word);
            System.out.println(count + "\t" + word);
        }
    }
    */
}
