import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Word {
    boolean isSpecial;
    ArrayList<Lemma> value1;
    String value2;

    public Word(boolean isSpecial, @Nullable ArrayList<Lemma> value1, @Nullable String value2)
    {
        this.isSpecial = isSpecial;
        this.value1 = value1;
        this.value2 = value2;
    }


    public boolean isEqual(ArrayList<Lemma> obj)
    {
        if (isSpecial)
        {
            Lemma tmp = obj.get(0);
            if (tmp.init != null && tmp.init.grammemes != null) {
                return tmp.init.grammemes.contains(value2);
            }
            else
                return false;
        }
        else
        {
            return obj.equals(value1);
        }
    }
}
