import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * name: NAME
 * synset: WORD1 WORD2, WORD3, WORD4
 * hyperonims: WORD5, WORD6, WORD7 WORD10
 * hyponims: WORD8 WORD11, WORD9
 * */

public class DescriptorManager  {
    HashMap<String, Descriptor> descriptors;
    ArrayList<Descriptor> descriptorsAr;

    public DescriptorManager(String pathToDesc, HashMap<String, ArrayList<Lemma>> hash, Dict dict) throws IOException
    {
        descriptors = new HashMap<>();
        descriptorsAr = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(pathToDesc)))
        {
            String line = reader.readLine();
            //its so horrible...
            while(line!= null)
            {
                String name = line.trim().split(":")[1];
                line = reader.readLine();
                var synset = getSecondArgSplited(line);
                line = reader.readLine();
                var hyperonims = getSecondArgSplited(line);
                line = reader.readLine();
                var hyponims = getSecondArgSplited(line);
                line = reader.readLine();
                Descriptor desc = new Descriptor(name, synset, hyperonims, hyponims, hash, dict);
                descriptors.put(name, desc);
                descriptorsAr.add(desc);
            }
            //connections
            for(Descriptor d: descriptorsAr)
            {
                if (d.hyperonims.size() != 0 )
                {
                    for (String elem: d.hyperonims)
                    {
                        if (descriptors.containsKey(elem))
                        {
                            d.hyperonimsDesc.add(descriptors.get(elem));
                        }
                    }
                }
                if (d.hyponims.size() != 0 )
                    for (String elem: d.hyponims)
                    {
                        if (descriptors.containsKey(elem))
                        {
                            d.hyponimsDesc.add(descriptors.get(elem));
                        }
                    }
            }
        }
    }

    private ArrayList<String> getSecondArgSplited(String str)
    {
        String[] tmpAr = str.trim().split(":");
        if (tmpAr.length > 1)
        {
            String tmp = tmpAr[1];
            return new ArrayList<>( List.of(tmp.trim().split(",")));
        }
        else
        {
            return new ArrayList<>();
        }
    }
    public void countIDF(ArrayList<Text> texts)
    {
        for (Descriptor des: descriptorsAr)
        {
            int counter = 0 ;
            for(Text text: texts)
            {
                if (text.containesDes(des))
                    counter++;

                else if(des.hyponimsDesc.size() != 0)
                {
                    for(Descriptor sun : des.hyponimsDesc)
                    {
                        if (text.containesDes(sun))
                            counter++;
                    }
                }
            }
            if(counter!= 0 )
                des.IDF = Math.log((double) texts.size() / counter);
            else
                des.IDF = Math.log(texts.size());
        }
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for(Descriptor elem: descriptorsAr)
        {
            str.append(elem.toString());
            str.append("\n");
        }
        return str.toString();
    }
}
