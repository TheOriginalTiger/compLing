import java.util.ArrayList;

public class Text {
    ArrayList<ArrayList<Lemma>> text;
    double[] vector;

    public Text(ArrayList<ArrayList<Lemma>> text, int vectorSize)
    {
        this.text = text;
        vector = new double[vectorSize];
    }


    public boolean containesDes(Descriptor d)
    {
        for(Context elem : d.synset)
        {
            if (elem.lemmas.size()==1)
            {
                if (text.contains(elem.lemmas.get(0)))
                    return true;
            }
            else
            {
                var hashmap = Utils.findAllNgrams(text, elem.lemmas.size());
                if( hashmap.containsKey(elem))
                    return true;
            }
        }
        return false;
    }

    public int countOcc(Descriptor d)
    {
        int counter = 0;
        for(Context elem : d.synset)
        {
            if(elem.lemmas.size() == 1)
            {
                ArrayList<Lemma> lemma = elem.lemmas.get(0);
                for(var word: text)
                {
                    if (word.equals(lemma))
                        counter++;
                }
            }
            else
            {
                var hashmap = Utils.findAllNgrams(text, elem.lemmas.size());
                if( hashmap.containsKey(elem))
                {
                    counter += hashmap.get(elem).size();
                }
            }
        }
        return counter;
    }

    public double countTF(Descriptor d){
        int counter = countOcc(d);

        if (d.hyponimsDesc.size() !=0 )
        {
            for (Descriptor sun: d.hyponimsDesc)
            {
                counter += countOcc(sun);
            }
        }
        return (double) counter / text.size();
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for(var elem: text)
        {
            str.append(elem.get(0).init.str);
            str.append(" ");
        }
        return str.toString();
    }
}
