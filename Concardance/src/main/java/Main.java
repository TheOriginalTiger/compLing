import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws XMLStreamException, IOException {
        
        String pathToDict = "F:\\nlpDatasets\\dict.opcorpora.xml";
        String strPathToCorpus = "F:\\nlpDatasets\\news.txt";
        String strPathToOutput = "C:\\Users\\vvpvo\\Desktop\\nsu\\Lematizer\\output.txt";
        String tempDebugLine = "компания";
        int windowSize = 3;


        ArrayList<ArrayList<Lemma>> textLemmas = new ArrayList<>();

        ArrayList<ArrayList<Lemma>> phraseLemmas = null;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(strPathToCorpus))) {
            Dict dict = Utils.parseDict(pathToDict);
            // Если забить на эфективность по памяти и оставить только по времени,
            // то что может быть эффективнее хэшмапы?
            HashMap<String, ArrayList<Lemma>> hash = Utils.dictToMap(dict);
            phraseLemmas = Utils.lemmatizeLine(Utils.tokenizeLine(tempDebugLine), hash);
            String line = reader.readLine();

            while (line != null) {
                var tokens = Utils.tokenizeLine(line);
                var lemmas = Utils.lemmatizeLine(tokens, hash);
                for (int i = 0; i < lemmas.size(); i++ )
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

                line = reader.readLine();
            }

        }
        catch (XMLStreamException | InvalidPathException | IOException e )
        {
            e.printStackTrace();
        }


        ArrayList<Integer> indices = Utils.findEntries(textLemmas, phraseLemmas);
        ArrayList<Context> leftContexts = Utils.findLeftContext(textLemmas, indices, windowSize, phraseLemmas);
        ArrayList<Context> rightContexts = Utils.findRightContext(textLemmas, indices, windowSize, phraseLemmas);


        HashMap<Context, Integer> stats = new HashMap<>();
        leftContexts.addAll(rightContexts);
        for (Context context : leftContexts)
        {
            if (stats.containsKey(context))
            {
                stats.put(context, stats.get(context) + 1);
            }
            else
            {
                stats.put(context, 1);
            }
        }

        LinkedList<Map.Entry<Context, Integer>> list = new LinkedList<>(stats.entrySet());
        list.sort(Map.Entry.comparingByValue());
        for (int i = 0; i < 10; i++) {
            System.out.println(list.peekLast().getKey());
            System.out.println(list.pollLast().getValue());
        }
    }
}
