import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws XMLStreamException, IOException {
        
        String pathToDict = "F:\\nlpDatasets\\dict.opcorpora.xml";
        String strPathToCorpus = "F:\\nlpDatasets\\news.txt";
        String strPathToOutput = "C:\\Users\\vvpvo\\Desktop\\nsu\\Lematizer\\output.txt";
        int windowSize = 5;
        double threshold = 0.9;

        ArrayList<ArrayList<Lemma>> textLemmas = new ArrayList<>();
        ArrayList<Pair<Integer>> textIndices = new ArrayList<>();
        int prev = 0;
        ArrayList<ArrayList<Lemma>> phraseLemmas = null;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(strPathToCorpus))) {
            Dict dict = Utils.parseDict(pathToDict);
            HashMap<String, ArrayList<Lemma>> hash = Utils.dictToMap(dict);
            String line = reader.readLine();

            while (line != null) {
                var tokens = Utils.tokenizeLine(line);
                var lemmas = Utils.lemmatizeLine(tokens, hash);
                for (int i = 0; i < lemmas.size(); i++)
                {
                    ArrayList<Lemma> elem = lemmas.get(i);
                    if (elem == null)
                    {
                        String str = tokens.get(i);
                        Lemma unknown = new Lemma();
                        unknown.init = new Lemma.WordForm(str);
                        dict.lemmata.add(unknown);
                        ArrayList<Lemma> toAdd = new ArrayList<>();
                        toAdd.add(unknown);
                        lemmas.set(i, toAdd);
                    }
                }
                textLemmas.addAll(lemmas);
                textIndices.add(new Pair<>(prev, prev + lemmas.size()));
                prev += lemmas.size();
                line = reader.readLine();
            }

        }
        catch (XMLStreamException | InvalidPathException | IOException e )
        {
            e.printStackTrace();
        }
        System.out.println("Looking for ngrams");
        HashMap<Context, ArrayList<Integer>> stats = Utils.findAllNgrams(textLemmas, windowSize);
        System.out.println("Done!");
        ArrayList<ContextWrapper> tmp = new ArrayList<>();
        for (Context key : stats.keySet())
        {
            if (stats.get(key).size() > 1)
                tmp.add(new ContextWrapper(key,stats.get(key)));
        }
        System.out.println(tmp.size());
        System.out.println("Scanning for left and right max extensions");
        List<ContextWrapper> res = tmp.parallelStream().filter(x->Utils.isPersistant(x, textLemmas, threshold)).collect(Collectors.toList());
        res.forEach(x->x.countUnique(textIndices));

        System.out.println("Done!");
        HashMap<ContextWrapper, Integer> statistics = new HashMap<>();
        for (ContextWrapper c: res)
            statistics.put(c, c.freq.size());

        System.out.println(res.size());
        LinkedList<Map.Entry<ContextWrapper, Integer>> list = new LinkedList<>(statistics.entrySet());
        list.sort(Map.Entry.comparingByValue());
        for (int i = 0; i < 10; i++) {
            ContextWrapper elem = list.pollLast().getKey();
            System.out.println(elem);
        }
    }
}
