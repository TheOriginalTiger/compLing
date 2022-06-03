import java.util.*;

public class SemanticsWords {
    ArrayList<Word> words;
    HashSet<ArrayList<Lemma>> wordsSet;
    //проблема в том, что особые ключевые слова нельзя трактовать как обычные леммы
    //весь класс Word существует, потому что иногда слова могут быть ключевыми
    //если таких нет, то мы спокойно можем пользоваться ими как контекстом
    private Context context;
    private boolean containsSpecial = false;

    public SemanticsWords(String source, HashMap<String, ArrayList<Lemma>> hash, Dict dict)
    {
        this.words = new ArrayList<>();
        ArrayList<ArrayList<Lemma>> ujas = new ArrayList<>();
        ArrayList<String> special = new ArrayList<>(List.of(new String[]{"NOUN", "ADJF", "VERB", "INFN", "NUMR"}));
        ArrayList<String> words =  new ArrayList<>( List.of(source.trim().split("\\s+")));
        for (String word : words)
        {
            Word wrd;
            if (special.contains(word))
            {
                wrd = new Word(true, null, word);
                containsSpecial = true;
            }
            else
            {
                word = word.toLowerCase();
                ArrayList<Lemma> lemma =  Utils.lemmatizeWord(word, hash, dict);
                wrd = new Word(false,lemma, null);
                ujas.add(lemma);
            }
            this.words.add(wrd);
        }
        if (!containsSpecial)
            context = new Context(ujas);
    }

    public boolean contains(Sentence s)
    {
        boolean res = false;

        if (!containsSpecial)
        {
            var ngrams = Utils.findAllNgrams(s.words, words.size());
            if (ngrams.get(context) != null)
                res = true;
        }
        else
        {
            var first = words.get(0);
            for (int i = 0 ; i < s.words.size(); i++)
            {
                var wordInText = s.words.get(i);
                int j = 0;
                boolean isEq = false;
                if (first.isEqual(wordInText))
                {
                    isEq = true;
                    while(isEq && j < words.size())
                    {
                        Word current = words.get(j);
                        if(!current.isEqual(s.words.get(i+j)))
                        {
                            isEq = false;
                            break;
                        }
                        j++;
                    }
                }
                if (isEq)
                {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for ( Word elem : words)
        {
            String str;
            if (elem.isSpecial) {
                str = elem.value2;
            }
            else {
                str = elem.value1.get(0).init.str;
            }
            res.append(str).append(" ");
        }
        return res.toString();
    }
}
