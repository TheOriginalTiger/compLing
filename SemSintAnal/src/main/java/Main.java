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


public class Main {
    public static void main(String[] args){
        
        String pathToDict = "F:\\nlpDatasets\\dict.opcorpora.xml";
        String strPathToCorpus = "F:\\nlpDatasets\\news.txt";
        String pathToSemantics = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\SemSintAnal\\sem\\semant.txt";
        String pathToModel = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\SemSintAnal\\sem\\models.txt";
        String strPathToOutput = "C:\\Users\\vvpvo\\Desktop\\nsu\\compLing\\SemSintAnal\\result.txt";
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

        //semantics
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(pathToSemantics)))
        {
            String line = reader.readLine();
            while(line!= null)
            {
                Semantics sem = new Semantics(line, hash, dict);
                semHash.put(line.trim().split(":")[0],sem);
                line = reader.readLine();
            }
        } catch (InvalidPathException | IOException e) {
            e.printStackTrace();
        }

        ArrayList<Model> models = new ArrayList<>();

        //models
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(pathToModel)))
        {
            String line = reader.readLine();
            while(line != null)
            {
                ArrayList<String> tokens = Utils.tokenizeLine(line);
                Model curModel = new Model(new ArrayList<>());
                for (String token : tokens)
                {
                    Semantics sem = semHash.get(token);
                    if (sem == null) {
                        System.out.println("WARNING: unknown name of the semantics:" + token);
                        continue;
                    }
                    curModel.semantics.add(sem);
                }
                models.add(curModel);
                line = reader.readLine();
            }
        }
        catch (IOException | InvalidPathException e )
        {
            e.printStackTrace();
        }
        Model firstModel = models.get(0);

        //corpus by sentences
        ArrayList<Sentence> sentences = new ArrayList<>();
        Sentence currentSentence = new Sentence(new ArrayList<>());
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(strPathToCorpus))) {

            String line = reader.readLine();

            while (line != null) {
                var tokens = Utils.tokenizeLine(line);
                var lemmas = Utils.lemmatizeLine(tokens, hash, dict);
                for (var lemma : lemmas)
                {
                    if (lemma.get(0).init.str.equals("."))
                    {
                        sentences.add(currentSentence);
                        currentSentence = new Sentence(new ArrayList<>());
                    }
                    else
                    {
                        currentSentence.words.add(lemma);
                    }
                }
                line = reader.readLine();
            }
        }
        catch (InvalidPathException | IOException e )
        {
            e.printStackTrace();
        }

        try (FileWriter fileWriter = new FileWriter(strPathToOutput)) {
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for (Model model: models) {

                ArrayList<Sentence> containing = new ArrayList<>();
                for (Sentence s : sentences) {
                    if (model.testSentence(s))
                        containing.add(s);
                }
                printWriter.printf("Model: %s%n", model.toString());
                printWriter.printf("freq: %f%n", ((float) containing.size()/sentences.size())*100);
                for (Sentence s : containing)
                    printWriter.println(s);
                printWriter.println();
            }


        }
        catch(IOException e )
        {
            e.printStackTrace();
        }

    }
}
