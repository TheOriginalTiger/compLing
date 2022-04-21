import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ContextWrapper {
    Context context;
    ArrayList<Integer> freq;
    Integer UniqueTexts;
    public ContextWrapper(Context context, ArrayList<Integer> freq)
    {
        this.context = context;
        this.freq = freq;
    }
    public void countUnique(ArrayList<Pair<Integer>> indicies)
    {
        HashSet<Pair<Integer>> encounters = new HashSet<>();
        for (Integer ind : freq)
        {
            for (Pair<Integer> pair : indicies)
            {
                if (ind >= pair.a && ind < pair.b) {
                    encounters.add(pair);
                    break;
                }
            }
        }
        UniqueTexts = encounters.size();
    }

    @Override
    public String toString()
    {
        return context.toString() + " textsFreq " + UniqueTexts + " corpusFreq " + freq.size();
    }
}
