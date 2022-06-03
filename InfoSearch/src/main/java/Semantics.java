import java.util.*;

public class Semantics {
    ArrayList<Lemma> sem;
    ArrayList<ArrayList<Lemma>> words;
    HashSet<ArrayList<Lemma>> wordsSet;
    ArrayList<SemanticsWords> actuallyWords;

    public Semantics(ArrayList<String> source, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        this.words = Utils.lemmatizeLine(source, hash, dict);
        this.sem = this.words.get(0);
        this.wordsSet = new HashSet<>(words);
    }
    public Semantics(String source, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        actuallyWords = new ArrayList<>();
        ArrayList<String> firstSplit = new ArrayList<> (List.of(source.trim().split(":")));
        this.sem = Utils.lemmatizeWord(firstSplit.get(0), hash, dict);
        ArrayList<String> SecondSplit = new ArrayList<>( List.of(firstSplit.get(1).trim().split(",")));
        for (String words: SecondSplit)
        {
            actuallyWords.add(new SemanticsWords(words, hash, dict));
        }
    }

    public boolean containshui(Sentence sentence)
    {
        HashSet<ArrayList<Lemma>> set = new HashSet<>(sentence.words);
        set.retainAll(wordsSet);
        return set.size() != 0;
    }
    public boolean contains(Sentence sentence)
    {
        for(SemanticsWords wrd: actuallyWords)
        {
            if (wrd.contains(sentence))
                return true;
        }
        return false;
    }

    public String toString()
    {
        return "semantics: " + sem.get(0).init.str + " words: " + actuallyWords.toString();
    }
}

// descriptor: SemanticsWord1 SemanticsWord2, SemanticsWord3, SemanticsWord4 SemanticsWord5 SemanticsWord6