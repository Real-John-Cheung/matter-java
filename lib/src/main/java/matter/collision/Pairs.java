package matter.collision;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * methods for creating and manipulating collision pair sets
 * 
 */
public class Pairs {

    public HashMap<String, Pair> table;

    public ArrayList<Pair> list;

    public ArrayList<Pair> collisionStart;
    public ArrayList<Pair> collisionActive;
    public ArrayList<Pair> collisionEnd;

    @SuppressWarnings("unchecked")
    public Pairs(HashMap<String, Object> options) {
        this.table = (HashMap<String, Pair>) options.getOrDefault("table", new HashMap<String, Pair>());
        this.list = (ArrayList<Pair>) options.getOrDefault("list", new ArrayList<Pair>());
        this.collisionStart = (ArrayList<Pair>) options.getOrDefault("collisionStart", new ArrayList<Pair>());
        this.collisionActive = (ArrayList<Pair>) options.getOrDefault("collisionActive", new ArrayList<Pair>());
        this.collisionEnd = (ArrayList<Pair>) options.getOrDefault("collisionEnd", new ArrayList<Pair>());

    }

    /**
     * create new pairs
     * 
     * @param options options
     * @return a new pairs
     */
    static public Pairs create(HashMap<String, Object> options) {
        return new Pairs(options);
    }

    /**
     * update pairs given a list of collisions
     * 
     * @param pairs      pairs
     * @param collisions collisions
     * @param timestamp  timestamp
     */
    static public void update(Pairs pairs, Collision[] collisions, double timestamp) {
        ArrayList<Pair> pairsList = pairs.list;
        int pairsListLength = pairsList.size();
        HashMap<String, Pair> pairsTable = pairs.table;
        int collisionsLength = collisions.length;
        ArrayList<Pair> collisionStart = pairs.collisionStart,
                collisionEnd = pairs.collisionEnd,
                collisionActive = pairs.collisionActive;
        Collision collision;
        int pairIndex;
        Pair pair;

        collisionStart.clear();
        collisionEnd.clear();
        collisionActive.clear();

        for (int i = 0; i < pairsListLength; i++) {
            pairsList.get(i).confirmedActive = false;
        }

        for (int i = 0; i < collisions.length; i++) {
            collision = collisions[i];
            pair = collision.pair;
            if (pair != null) {
                if (pair.isActive) {
                    collisionActive.add(pair);
                } else {
                    collisionStart.add(pair);
                }
                Pair.update(pair, collision, timestamp);
                pair.confirmedActive = true;
            } else {
                pair = Pair.create(collision, timestamp);
                pairsTable.put(pair.id, pair);
                collisionStart.add(pair);
                pairsList.add(pair);
            }
        }

        ArrayList<Integer> removePairIndex = new ArrayList<>();
        pairsListLength = pairsList.size();
        for (int i = 0; i < pairsListLength; i++) {
            pair = pairsList.get(i);
            if (!pair.confirmedActive) {
                Pair.setActive(pair, false, timestamp);
                collisionEnd.add(pair);

                if (!pair.collision.bodyA.isSleeping && !pair.collision.bodyB.isSleeping) {
                    removePairIndex.add(i);
                }
            }
        }

        for (int i = 0; i < removePairIndex.size(); i++) {
            pairIndex = removePairIndex.get(i);
            pair = pairsList.get(pairIndex);
            pairsList.remove(pairIndex);
            pairsTable.remove(pair.id);
        }
    }
    
    /**
     * clear the pairs
     * 
     * @param pairs pairs
     * @return pairs
     */
    static public Pairs clear(Pairs pairs) {
        pairs.table.clear();
        pairs.list.clear();
        pairs.collisionStart.clear();
        pairs.collisionActive.clear();
        pairs.collisionEnd.clear();
        return pairs;
    }
}