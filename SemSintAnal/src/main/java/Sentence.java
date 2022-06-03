import java.util.ArrayList;

public class Sentence {
    ArrayList<ArrayList<Lemma>> words;
    public Sentence(ArrayList<ArrayList<Lemma>> words)
    {
        this.words = words;
    }

    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for ( var elem : words)
        {
            res.append(elem.get(0).init.str).append(" ");
        }
        return res.toString();
    }
}
