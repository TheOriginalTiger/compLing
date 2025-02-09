import org.jetbrains.annotations.Nullable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
enum parentTag{grammeme, restr, lemma, none, lemma_l, lemma_f}

public class Utils {

    public static void printExplainedVector(PrintWriter printWriter, Text text, DescriptorManager dm)
    {
        double[] vec = text.vector;
        printWriter.print("[ ");
        for(int j = 0; j < vec.length; j++ )
        {
            if (vec[j]!=0)
            {
                printWriter.print(dm.descriptorsAr.get(j).name + " ");
                printWriter.print(vec[j]+ " ");
            }
        }
        printWriter.println(" ]");
    }
    public static double[] normalizeVector(double[] vector)
    {
        double magnitude = 0;
        double[] res = new double[vector.length];
        for (double v : vector) {
            magnitude += (v*v);
        }
        if (magnitude == 0 )
            return vector;
        magnitude = Math.sqrt(magnitude);
        for(int i = 0 ; i < vector.length; i++)
        {
            res[i] = vector[i] / magnitude;
        }
        return res;
    }

    public static double lenght(double[] vec)
    {
        double res = 0;
        for (double v : vec) {
            res += (v * v);
        }
        assert(res >= 0);
        return Math.sqrt(res);
    }
    public static double countCos(double[] vec1, double[] vec2)
    {
        double cons = 0.000000001;
        double scalar = 0 ;
        for (int i = 0 ; i < vec1.length; i++)
        {
            scalar += (vec1[i] * vec2[i]);
        }
        double lenght = (lenght(vec1)*lenght(vec2));
        if (lenght == 0 || lenght < cons)
            return 0;
        return scalar/ lenght;

    }
    public static void countTFIDF(ArrayList<Text> texts, DescriptorManager dm)
    {
        ArrayList<Descriptor> descs = dm.descriptorsAr;
        for(Text text: texts)
        {
            for (int i = 0 ;i  < descs.size(); i++ )
            {
                Descriptor desc = descs.get(i);
                double tf = text.countTF(desc);
                text.vector[i] = tf * desc.IDF;
            }
        }
    }

    public static HashMap<String, ArrayList<Lemma>> dictToMap(Dict dict)
    {
        HashMap<String, ArrayList<Lemma>> res = new HashMap<>();
        Lemma comma = new Lemma();
        comma.init = new Lemma.WordForm(",");
        comma.forms = new ArrayList<>();
        Lemma dot = new Lemma();
        dot.init = new Lemma.WordForm(".");
        dot.forms = new ArrayList<>();
        dict.lemmata.add(comma);
        dict.lemmata.add(dot);
        for (Lemma lemma : dict.lemmata)
        {
            Set<String> set = new HashSet<>();
            for (Lemma.WordForm wordForm : lemma.forms)
            {
                set.add(wordForm.str);
            }
            set.add(lemma.init.str);
            for (String str : set)
            {
                if (!res.containsKey(str))
                {
                    ArrayList<Lemma> tmp = new ArrayList<>();
                    tmp.add(lemma);
                    res.put(str, tmp);
                }
                else
                {
                    ArrayList<Lemma> tmp = res.get(str);
                    tmp.add(lemma);
                }
            }
        }
        return res;
    }

    public static ArrayList<String> tokenizeLine(String line)
    {
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(line.trim().toLowerCase().replaceAll("[!?\"'()@#$%^&*<>/«»]","").split("\\s+")));
        ArrayList<String> res = new ArrayList<>();
        for (String word : tmp){
            StringTokenizer st = new StringTokenizer(word, "[.,]", true);
            while (st.hasMoreTokens()){
                res.add(st.nextToken());
            }
        }
        return res;
    }

    public static ArrayList<Lemma> lemmatizeWord(String word, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        ArrayList<Lemma> lemma = hash.get(word);
        if (lemma == null) {
            lemma = new ArrayList<>();
            Lemma unknown = new Lemma();
            unknown.init = new Lemma.WordForm(word);
            dict.lemmata.add(unknown);
            lemma.add(unknown);
        }
        return lemma;
    }
    public static ArrayList<ArrayList<Lemma>> lemmatizeLine(ArrayList<String> line, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        ArrayList<ArrayList<Lemma>> lemmas = new ArrayList<>();
        for (String word : line) {
            ArrayList<Lemma> lemma = lemmatizeWord(word, hash, dict);
            lemmas.add(lemma);
        }
        return lemmas;
    }

    public static ArrayList<Integer> findEntries(ArrayList<ArrayList<Lemma>> text, ArrayList<ArrayList<Lemma>> phrase)
    {
        ArrayList<Integer> entryIndices = new ArrayList<>();
        for (int i = 0 ; i < text.size() - phrase.size(); i++)
        {
            int j = 0;
            for( ; j < phrase.size(); j++)
            {
                ArrayList<Lemma> temp = text.get(i+j);
                if (temp != null){
                    temp = (ArrayList<Lemma>) text.get(i+j).clone();
                    temp.retainAll(phrase.get(j));
                    if (temp.isEmpty())
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
            if (j == phrase.size() )
                entryIndices.add(i);
        }
        return entryIndices;
    }
    public static @Nullable Integer getMostFrequentContextEntries(ArrayList<Context> contexts)
    {
        HashMap<Context, Integer> stats = new HashMap<>();

        for (Context context : contexts)
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
        return list.pollLast().getValue();
    }
    
    public static HashMap<Context, ArrayList<Integer>> findAllNgrams(ArrayList<ArrayList<Lemma>> text, int N)
    {
        HashMap<Context, ArrayList<Integer>> res = new HashMap<>();
        for(int i = 0 ; i < text.size() - (N-1); i++)
        {
            ArrayList<ArrayList<Lemma>> ngram = new ArrayList<>();
            for (int j = 0 ; j < N ; j++)
                ngram.add(text.get(i+j));
            Context context = new Context(ngram);
            if (res.containsKey(context))
            {
                res.get(context).add(i);
            }
            else
            {
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(i);
                res.put(context, tmp);
            }
        }
        return res;
    }
    public static ArrayList<Context> findLeftContext(ArrayList<ArrayList<Lemma>> text, ArrayList<Integer> indices, int windowSize, ArrayList<ArrayList<Lemma>> phrase)
    {
        ArrayList<Context> res = new ArrayList<>();
        for (Integer ind : indices)
        {
            for (int i = 0 ; i < windowSize;i++)
            {
                ArrayList<ArrayList<Lemma>> context = new ArrayList<>();
                for (int j = i + 1 ; j > 0; j-- )
                {
                    if (ind - j >= 0)
                        context.add(text.get(ind - j));
                }
                addPhrase(text, phrase, ind, context);
                res.add(new Context(context));
            }

        }
        return res;
    }

    public static ArrayList<Context> findRightContext(ArrayList<ArrayList<Lemma>> text, ArrayList<Integer> indices, int windowSize, ArrayList<ArrayList<Lemma>> phrase)
    {
        ArrayList<Context> res = new ArrayList<>();
        for (Integer ind : indices)
        {

            int endOfPhrase = ind + phrase.size();
            for(int i = 0 ; i < windowSize; i++)
            {
                ArrayList<ArrayList<Lemma>> context = new ArrayList<>();
                addPhrase(text, phrase, ind, context);
                for(int j = 0; j <=i; j++)
                {
                    if (endOfPhrase + j < text.size() )
                        context.add(text.get(endOfPhrase + j));
                }
                res.add(new Context(context));
            }

        }
        return res;
    }

    private static void addPhrase(ArrayList<ArrayList<Lemma>> text, ArrayList<ArrayList<Lemma>> phrase, Integer ind, ArrayList<ArrayList<Lemma>> context) {
        for (int j = 0 ; j < phrase.size(); j++)
        {
            // just a sanity check
            assert text.get(ind + j)!= null;
            assert text.get(ind + j).get(0) == phrase.get(j).get(0);
            context.add(text.get(ind + j));
        }
    }

    public static Boolean isPersistant(ContextWrapper context, ArrayList<ArrayList<Lemma>> textLemmas, double threshold)
    {

        Integer mostFrequentLeft = getMostFrequentContextEntries(findLeftContext(textLemmas,context.freq, 1,context.context.lemmas ));
        Integer mostFrequentRight = getMostFrequentContextEntries(findRightContext(textLemmas,context.freq, 1,context.context.lemmas ));
        context.leftPersistence = (double) mostFrequentLeft/context.freq.size();
        context.rightPersistence = (double) mostFrequentRight/context.freq.size();
        return ( context.leftPersistence <= threshold &&  context.rightPersistence <= threshold);
    }
    // XMl parser. Was completely stolen from data storage lab
    public static Dict parseDict(String path_to_dict) throws IOException, XMLStreamException
    {
        XMLInputFactory streamFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = streamFactory.createXMLStreamReader(new FileInputStream(path_to_dict));

        Dict dict = new Dict();
        Grammeme curGramemme = new Grammeme();

        Lemma curLemma = new Lemma();

        parentTag current = parentTag.none;
        for (; reader.hasNext(); reader.next())
        {
            int eventType = reader.getEventType();
            switch (eventType)
            {
                case XMLStreamConstants.START_ELEMENT -> {
                    switch (reader.getLocalName())
                    {
                        case "dictionary" -> {
                            assert reader.getAttributeCount() == 2;
                            System.out.println("version: " + reader.getAttributeValue(0) + ", revision: " + reader.getAttributeValue(1));
                        }
                        case "grammemes" -> {
                            System.out.println("started grammemes");
                        }
                        case "restrictions" -> {
                            System.out.println("started restrictions");
                        }
                        case "lemmata" -> {
                            System.out.println("started lemmata");
                        }
                        case "restr" -> current = parentTag.restr;
                        case "grammeme" -> {
                            current = parentTag.grammeme;
                            curGramemme = new Grammeme();
                        }
                        case "lemma" -> {
                            current = parentTag.lemma;
                            curLemma = new Lemma();
                            assert reader.getAttributeCount() == 2 && reader.getAttributeLocalName(0).equals("id");
                            curLemma.id = reader.getAttributeValue(0);
                        }
                        case "name" -> {
                            assert current == parentTag.grammeme;
                            reader.next();
                            assert reader.getEventType() == XMLStreamConstants.CHARACTERS;
                            curGramemme.name = reader.getText().trim();
                        }
                        case "alias" -> {
                            assert current == parentTag.grammeme;
                            reader.next();
                            assert reader.getEventType() == XMLStreamConstants.CHARACTERS;
                            curGramemme.alias = reader.getText().trim();
                        }
                        case "description" -> {
                            assert current == parentTag.grammeme;
                            reader.next();
                            assert reader.getEventType() == XMLStreamConstants.CHARACTERS;
                            curGramemme.description = reader.getText().trim();
                        }

                        case "left", "right" -> {
                            assert current == parentTag.restr;
                        }
                        case "l" -> {
                            current = parentTag.lemma_l;
                            assert reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("t");
                            curLemma.init = new Lemma.WordForm(reader.getAttributeValue(0));
                            curLemma.forms = new ArrayList<>();
                        }
                        case "g" -> {
                            assert current == parentTag.lemma_l || current == parentTag.lemma_f;
                            assert reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("v");
                            if(current == parentTag.lemma_l){
                                curLemma.init.grammemes.add(reader.getAttributeValue(0));
                            }
                            else{
                                curLemma.forms.get(curLemma.forms.size()-1).grammemes.add(reader.getAttributeValue(0));
                            }
                        }
                        case "f" -> {
                            current = parentTag.lemma_f;
                            assert reader.getAttributeCount() == 1 && reader.getAttributeLocalName(0).equals("t");
                            curLemma.forms.add(new Lemma.WordForm(reader.getAttributeValue(0)));
                        }
                        default -> System.out.println("Unknown property: " + reader.getLocalName());
                    }
                }
                case XMLStreamConstants.END_ELEMENT -> {
                    switch (reader.getLocalName())
                    {
                        case "grammeme" -> {
                            assert current == parentTag.grammeme;
                            dict.grammemes.add(curGramemme);
                            current = parentTag.none;
                        }
                        case "lemma" -> {
                            assert current == parentTag.lemma;
                            dict.lemmata.add(curLemma);
                            current = parentTag.none;
                        }
                        case "restr" -> {
                            assert current == parentTag.restr;
                            current = parentTag.none;
                        }
                        case "grammemes" -> {
                            System.out.println("finished grammemes, cnt: " + dict.grammemes.size());
                        }
                        case "restrictions" -> {
                            System.out.println("finished restrictions");
                        }
                        case "lemmata" -> {
                            System.out.println("finished lemmata, cnt: " + dict.lemmata.size());
                            return dict;
                        }
                        case "l", "f" -> {
                            current = parentTag.lemma;
                        }
                        case "g" -> {}
                        case "left", "right" -> {
                            assert current == parentTag.restr;
                        }
                        case "alias", "description", "name" -> {
                            assert current == parentTag.grammeme;
                        }
                        default -> {
                            System.out.println("Unknown end tag: " + reader.getLocalName());
                        }
                    }
                }
            }
        }
        return dict;
    }
}
