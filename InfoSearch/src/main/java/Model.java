import java.util.ArrayList;

public class Model {
    ArrayList<Semantics> semantics;
    public Model(ArrayList<Semantics> semantics)
    {
        this.semantics = semantics;
    }

    public boolean testSentence(Sentence s)
    {
        for (var sem: this.semantics)
        {
            if (! sem.contains(s))
                return false;
        }
        return true;
    }

    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for ( var elem : semantics)
        {
            res.append(elem.sem.get(0).init.str).append(" ");
        }
        return res.toString();
    }
}
