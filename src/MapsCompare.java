import java.util.Map;

public class MapsCompare {
    private final int[] array;
    private final Map<Integer, Integer> map;

    public MapsCompare(int[] array, Map<Integer, Integer> map) {
        this.array = array;
        this.map = map;
    }

    public void measureWrite() {
        for (int i = 0; i < array.length; i++) {
            map.put(i, array[i]);
        }
    }

    public void measureRead() {
        for (int i = 0; i < array.length; i++) {
            int a = map.get(i);
        }
    }
}
