import jdk.jshell.execution.Util;

import javax.xml.stream.XMLStreamException;
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


public class Main{
    public static void main(String[] args){

        String pathToDict = "F:\\nlpDatasets\\dict.opcorpora.xml";
        String strPathToCorpus = "F:\\nlpDatasets\\news\\";//"F:\\nlpDatasets\\debugNews\\";
        String strPathToDesc = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\InfoSearch\\sem\\descs.txt";
        String strPathToRequest = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\InfoSearch\\sem\\request.txt";
        String strPathToOutput = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\InfoSearch\\output.txt";
        int amountOfTexts = 2000;
        int amountOfRequests = 0;
        Dict dict = null;
        HashMap<String, ArrayList<Lemma>> hash = null;
        HashMap<String, Semantics> semHash = new HashMap<>();

        //dict
        try {
            dict = Utils.parseDict(pathToDict);
            hash = Utils.dictToMap(dict);
        }
        catch(IOException | XMLStreamException e )
        {
            System.out.println("DictReadingError");
        }

        assert(dict!= null);
        DescriptorManager dm = null;
        try {
            dm = new DescriptorManager(strPathToDesc, hash, dict);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assert(dm!=null);
        System.out.println(dm.descriptorsAr.size());
        ArrayList<Text> texts = new ArrayList<>();
        System.out.println("reading texts");
        for(int i = 0; i < amountOfTexts; i++)
        {
            String textName = strPathToCorpus + i + ".txt";
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(textName)))
            {
                String line = reader.readLine();
                ArrayList<ArrayList<Lemma>> lemmatizedText = new ArrayList<>();
                while(line != null )
                {
                    var tokens = Utils.tokenizeLine(line);
                    ArrayList<ArrayList<Lemma>> lemmas = Utils.lemmatizeLine(tokens, hash, dict);
                    lemmatizedText.addAll(lemmas);
                    line = reader.readLine();
                }
                texts.add(new Text(lemmatizedText, dm.descriptors.size()));
            }
            catch (IOException e )
            {
                System.out.println("could not open text");
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(strPathToRequest))) {
            String line = reader.readLine();
            while (line != null) {
                var tokens = Utils.tokenizeLine(line);
                ArrayList<ArrayList<Lemma>> lemmas = Utils.lemmatizeLine(tokens, hash, dict);
                texts.add(new Text(lemmas, dm.descriptors.size()));
                amountOfRequests++;
                line = reader.readLine();
            }
        }
        catch (IOException e )
        {
            System.out.println("Couldn't read request");
        }

        System.out.println("Done");
        //indexing texts
        System.out.println("counting IDFs");
        dm.countIDF(texts);
        System.out.println("Done");
        System.out.println("counting TFIDF");
        Utils.countTFIDF(texts, dm);
        System.out.println("Done");
        double constanta = 0.000000001;
        for(var text: texts)
        {
            text.vector = Utils.normalizeVector(text.vector);
            double summ = 0 ;
            for(double elem: text.vector)
            {
                summ+=elem;
            }
            if (!(summ == 1 || summ > 1-constanta || summ == 0))
            {
                System.out.println("hueta");
                System.out.println(summ);
            }

        }



        try (FileWriter fileWriter = new FileWriter(strPathToOutput)) {
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for(int r = amountOfRequests - 1 ; r>=0 ; r--)
            {
                int req = texts.size() - r - 1 ;
                Text request = texts.get(req);
                HashMap<Text, Double> res = new HashMap<>();
                for(int i = 0; i < texts.size() - amountOfRequests - 1; i++)
                {
                    Text txt = texts.get(i);
                    Double cos = Utils.countCos(txt.vector, request.vector);
                    res.put(txt, cos);
                }
                LinkedList<Map.Entry<Text, Double>> list = new LinkedList<>(res.entrySet());
                list.sort(Map.Entry.comparingByValue());
                printWriter.print("request: ");
                printWriter.println(request);
                Utils.printExplainedVector(printWriter,request,dm);
                for (int i = 0; i < 10; i++) {
                    var elem = list.pollLast();
                    printWriter.println(elem.getKey());
                    printWriter.println(elem.getValue());
                    Utils.printExplainedVector(printWriter,elem.getKey(),dm);
                }
                printWriter.println();
            }
        }
        catch (IOException e )
        {
            e.printStackTrace();
        }
    }
}
