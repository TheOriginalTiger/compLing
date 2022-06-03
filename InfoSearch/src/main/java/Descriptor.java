import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
* name: NAME
* synset: WORD1 WORD2, WORD3, WORD4
* hyperonims: WORD5, WORD6, WORD7 WORD10
* hyponims: WORD8 WORD11, WORD9
* */
public class Descriptor {
    ArrayList<Context> synset;
    //higher
    //Do I actually need it ?
    ArrayList<Descriptor> hyperonimsDesc;
    ArrayList<String> hyperonims;
    //lower
    ArrayList<Descriptor> hyponimsDesc;
    ArrayList<String> hyponims;

    String name;

    double IDF;

    private ArrayList<Context> contextFromString(ArrayList<String> source,HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        ArrayList<Context> res = new ArrayList<>();
        for(String str: source)
        {
            ArrayList<String> words = new ArrayList<>( List.of(str.trim().split("\\s+")));
            res.add(new Context(Utils.lemmatizeLine(words, hash,dict)));
        }
        return res;
    }

    public Descriptor(String name, ArrayList<String> synset, ArrayList<String> hyperonims, ArrayList<String> hyponims, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        this.name = name;
        this.synset = contextFromString(synset, hash, dict);
        this.hyponims = hyponims;
        this.hyperonims = hyperonims;
        this.hyperonimsDesc = new ArrayList<>();
        this.hyponimsDesc = new ArrayList<>();
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append("name:");
        str.append(name);
        str.append("\nsynset: ");
        str.append(synset.toString());
        str.append("\n hyperonims: ");
        str.append(hyperonims.toString());
        str.append("\n hyponims: ");
        str.append(hyponims.toString());
        return str.toString();
    }
}
