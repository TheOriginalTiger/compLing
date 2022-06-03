import java.util.ArrayList;

public class Context {
    ArrayList<ArrayList<Lemma>> lemmas;

    public Context(ArrayList<ArrayList<Lemma>> lemmas)
    {
        this.lemmas = lemmas;
    }
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("[ ");
        for(var lem : lemmas )
        {
            if (lem != null)
            {
                Lemma lm = lem.get(0);
                str.append(lm.init.str);
            }
            else
            {
                str.append("UNKNOWN");
            }
            str.append(" ");
        }
        str.append("]");
        return str.toString();
    }
    @Override
    public int hashCode()
    {
        return this.lemmas.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Context)
        {
            Context o = (Context) obj;
            return this.lemmas.equals(o.lemmas);
        }
        else
            return false;
    }
}
