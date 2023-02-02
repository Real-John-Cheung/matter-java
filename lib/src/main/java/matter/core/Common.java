package matter.core;

import java.util.HashMap;
import java.util.List;

/**
 * Utility functions
 * 
 * @author JohnC
 */
public class Common {
    private static int _nextId = 0;
    private static double _seed = 0;
    private static long _nowStartTime = System.currentTimeMillis();
    private static HashMap<String, Object> _warnedOnce = new HashMap<String, Object>();

    /**
     * return the given value clamped between a minimum and maximum value
     * 
     * @param value value
     * @param min min
     * @param max max
     * @return the clamped value
     */
    static public double clamp(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
    
    /**
     * return the next unique sequential ID
     * @return unique ID
     */
    static public int nextId() {
        return Common._nextId++;
    }

    /**
     * helper for options
     * 
     * @param options options
     * @param key key to get
     * @param defaultV defaultValur
     * @return
     */
    static public Object parseOption(HashMap<String, Object> options, String key, Object defaultV) {
        if (options == null) return defaultV;
        return options.getOrDefault(key, defaultV);
    }

    /**
     * helpers for encoding options
     */
    public static final HashMap<String, Object> opts() {
		return new HashMap<String, Object>();
	}

	public static final HashMap<String, Object> opts(String key, Object val) {
		return opts(new String[] { key }, new Object[] { val });
	}

	public static final HashMap<String, Object> opts(String key1, Object val1, String key2, Object val2) {
		return opts(new String[] { key1, key2 }, new Object[] { val1, val2 });
	}

	public static final HashMap<String, Object> opts(String key1, Object val1, String key2, Object val2, String key3,
			Object val3) {
		return opts(new String[] { key1, key2, key3 }, new Object[] { val1, val2, val3 });
	}

	public static final HashMap<String, Object> opts(String key1, Object val1, String key2, Object val2, String key3,
			Object val3, String key4, Object val4) {
		return opts(new String[] { key1, key2, key3, key4 }, new Object[] { val1, val2, val3, val4 });
	}

	public static final HashMap<String, Object> opts(String key1, Object val1, String key2, Object val2, String key3,
			Object val3, String key4, Object val4, String key5, Object val5) {
		return opts(new String[] { key1, key2, key3, key4, key5 }, new Object[] { val1, val2, val3, val4, val5 });
	}

    public static final HashMap<String, Object> opts(String[] keys, Object[] vals) {
        if (keys.length != vals.length)
            throw new RuntimeException("Bad Args");
        HashMap<String, Object> data = new HashMap<String, Object>();
        for (int i = 0; i < keys.length; i++) {
            data.put(keys[i], vals[i]);
        }
        return data;
    }
    
    /**
     * print warning to console
     * 
     * @param message message
     */
    public static void warn(String message) {
        System.out.println("Matter warning: " + message);
    }

    /**
     * helper for indexOf
     * 
     * @param <T> any non primative object type
     * @param array array
     * @param object the object to be found
     * @return int
     */
    public static <T> int indexOf(T[] array, T object) {
        return java.util.Arrays.asList(array).indexOf(object);
    }

    public static <T> int indexOf(List<T> list, T object) {
        return list.indexOf(object);
    }

    /**
     * return the current timestamp since the time origin 
     * @return the current timestamp in milliseconds
     */
    public static double now() {
        return System.currentTimeMillis() - Common._nowStartTime;
    }
}