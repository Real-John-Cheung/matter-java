package matter.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import matter.body.Body;
import matter.body.Composite;

/**
 * methods to fire and listen to events on other objects
 * 
 * @author JohnC
 */
public class Events {

    /**
     * subcribe a callback to the given body
     * 
     * @param object     body
     * @param eventNames eventNames
     * @param callback   callback
     * @return callback
     */
    static public Consumer<HashMap<String, Object>> on(Body object, String eventNames,
            Consumer<HashMap<String, Object>> callback) {
        String[] names = eventNames.split(" ");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (object.events == null)
                object.events = new HashMap<>();
            if (object.events.get(name) == null)
                object.events.put(name, new ArrayList<>());
            object.events.get(name).add(callback);
        }

        return callback;
    }

    /**
     * subcribe a callback to the given composite
     * 
     * @param object     composite
     * @param eventNames eventNames
     * @param callback   callback
     * @return callback
     */
    static public Consumer<HashMap<String, Object>> on(Composite object, String eventNames,
            Consumer<HashMap<String, Object>> callback) {
        String[] names = eventNames.split(" ");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (object.events == null)
                object.events = new HashMap<>();
            if (object.events.get(name) == null)
                object.events.put(name, new ArrayList<>());
            object.events.get(name).add(callback);
        }

        return callback;
    }

    /**
     * subcribe a callback to the given engine
     * 
     * @param object     engine
     * @param eventNames eventNames
     * @param callback   callback
     * @return callback
     */
    static public Consumer<HashMap<String, Object>> on(Engine object, String eventNames,
            Consumer<HashMap<String, Object>> callback) {
        String[] names = eventNames.split(" ");

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            if (object.events == null)
                object.events = new HashMap<>();
            if (object.events.get(name) == null)
                object.events.put(name, new ArrayList<>());
            object.events.get(name).add(callback);
        }

        return callback;
    }

    /**
     * remove all callback from object
     * 
     * @param object object
     */
    static public void off(Body object) {
        Events.off(object, null, null);
    }

    /**
     * remove all callback under the event name
     * 
     * @param object object
     * @param eventName eventName
     */
    static public void off(Body object, String eventName) {
        Events.off(object, eventName, null);
    }

    /**
     * remove the given callback
     * 
     * @param object object
     * @param eventNames eventNames
     * @param callback callback
     */
    static public void off(Body object, String eventNames, Consumer<HashMap<String, Object>> callback) {
        if (eventNames == null && callback == null) {
            object.events = new HashMap<>();
            return;
        } else if (eventNames == null) {
            eventNames = String.join(" ", object.events.keySet().toArray(String[]::new));
        }

        String[] names = eventNames.split(" ");
        for (int i = 0; i < names.length; i++) {
            if (object.events.get(names[i]) == null)
                continue;
            if (callback == null)
                object.events.get(names[i]).clear();
            object.events.get(names[i]).remove(callback);
        }
    }

    /**
     * remove all callback from object
     * 
     * @param object object
     */
    static public void off(Composite object) {
        Events.off(object, null, null);
    }

    /**
     * remove all callback under the event name
     * 
     * @param object object
     * @param eventName eventName
     */
    static public void off(Composite object, String eventName) {
        Events.off(object, eventName, null);
    }

    /**
     * remove the given callback
     * 
     * @param object object
     * @param eventNames eventNames
     * @param callback callback
     */
    static public void off(Composite object, String eventNames, Consumer<HashMap<String, Object>> callback) {
        if (eventNames == null && callback == null) {
            object.events = new HashMap<>();
            return;
        } else if (eventNames == null) {
            eventNames = String.join(" ", object.events.keySet().toArray(String[]::new));
        }

        String[] names = eventNames.split(" ");
        for (int i = 0; i < names.length; i++) {
            if (object.events.get(names[i]) == null)
                continue;
            if (callback == null)
                object.events.get(names[i]).clear();
            object.events.get(names[i]).remove(callback);
        }
    }

    /**
     * remove all callback from object
     * 
     * @param object object
     */
    static public void off(Engine object) {
        Events.off(object, null, null);
    }

    /**
     * remove all callback under the event name
     * 
     * @param object object
     * @param eventName eventName
     */
    static public void off(Engine object, String eventName) {
        Events.off(object, eventName, null);
    }

    /**
     * remove the given callback
     * 
     * @param object object
     * @param eventNames eventNames
     * @param callback callback
     */
    static public void off(Engine object, String eventNames, Consumer<HashMap<String, Object>> callback) {
        if (eventNames == null && callback == null) {
            object.events = new HashMap<>();
            return;
        } else if (eventNames == null) {
            eventNames = String.join(" ", object.events.keySet().toArray(String[]::new));
        }

        String[] names = eventNames.split(" ");
        for (int i = 0; i < names.length; i++) {
            if (object.events.get(names[i]) == null)
                continue;
            if (callback == null)
                object.events.get(names[i]).clear();
            object.events.get(names[i]).remove(callback);
        }
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     */
    static public void trigger(Body object, String eventNames) {
        Events.trigger(object, eventNames, null);
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     * @param event event
     */
    @SuppressWarnings("unchecked")
    static public void trigger(Body object, String eventNames, HashMap<String, Object> event) {
        String[] names;
        String name;
        ArrayList<Consumer<HashMap<String, Object>>> callbacks;
        HashMap<String, Object> eventClone;

        if (object.events != null && object.events.size() > 0) {
            if (event == null)
                event = new HashMap<>();
            names = eventNames.split(" ");
            for (int i = 0; i < names.length; i++) {
                name = names[i];
                callbacks = object.events.get(name);
                if (callbacks != null && callbacks.size() > 0) {
                    eventClone = (HashMap<String, Object>) event.clone();
                    eventClone.put("name", name);
                    eventClone.put("source", object);

                    for (int j = 0; j < callbacks.size(); j++) {
                        callbacks.get(j).accept(eventClone);
                    }
                }
            }
        }
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     */
    static public void trigger(Composite object, String eventNames) {
        Events.trigger(object, eventNames, null);
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     * @param event event
     */
    @SuppressWarnings("unchecked")
    static public void trigger(Composite object, String eventNames, HashMap<String, Object> event) {
        String[] names;
        String name;
        ArrayList<Consumer<HashMap<String, Object>>> callbacks;
        HashMap<String, Object> eventClone;

        if (object.events != null && object.events.size() > 0) {
            if (event == null)
                event = new HashMap<>();
            names = eventNames.split(" ");
            for (int i = 0; i < names.length; i++) {
                name = names[i];
                callbacks = object.events.get(name);
                if (callbacks != null && callbacks.size() > 0) {
                    eventClone = (HashMap<String, Object>) event.clone();
                    eventClone.put("name", name);
                    eventClone.put("source", object);

                    for (int j = 0; j < callbacks.size(); j++) {
                        callbacks.get(j).accept(eventClone);
                    }
                }
            }
        }
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     */
    static public void trigger(Engine object, String eventNames) {
        Events.trigger(object, eventNames, null);
    }

    /**
     * fire all the callbacks subscribed to the given eventName
     * 
     * @param object object
     * @param eventNames eventNames
     * @param event event
     */
    @SuppressWarnings("unchecked")
    static public void trigger(Engine object, String eventNames, HashMap<String, Object> event) {
        String[] names;
        String name;
        ArrayList<Consumer<HashMap<String, Object>>> callbacks;
        HashMap<String, Object> eventClone;

        if (object.events != null && object.events.size() > 0) {
            if (event == null)
                event = new HashMap<>();
            names = eventNames.split(" ");
            for (int i = 0; i < names.length; i++) {
                name = names[i];
                callbacks = object.events.get(name);
                if (callbacks != null && callbacks.size() > 0) {
                    eventClone = (HashMap<String, Object>) event.clone();
                    eventClone.put("name", name);
                    eventClone.put("source", object);

                    for (int j = 0; j < callbacks.size(); j++) {
                        callbacks.get(j).accept(eventClone);
                    }
                }
            }
        }
    }
}