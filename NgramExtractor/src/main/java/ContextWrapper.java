import java.util.ArrayList;

public class ContextWrapper {
    Context context;
    ArrayList<Integer> freq;
    public ContextWrapper(Context context, ArrayList<Integer> freq)
    {
        this.context = context;
        this.freq = freq;
    }
}
