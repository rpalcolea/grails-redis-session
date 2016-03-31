package grails.plugin.redissession

import org.codehaus.groovy.grails.web.servlet.GrailsFlashScope

import java.util.concurrent.ConcurrentHashMap


public class RedisGrailsFlashScope extends GrailsFlashScope{

    private static final long serialVersionUID = 1457772347769500476L
    public Map current = new ConcurrentHashMap()
    public Map next = new ConcurrentHashMap()
    public static final String ERRORS_PREFIX = "org.codehaus.groovy.grails.ERRORS_"
    private static final String ERRORS_PROPERTY = "errors"

    @Override
    public void next() {
        current.clear()
        current = new ConcurrentHashMap(next)
        next.clear()
        reassociateObjectsWithErrors(current)
    }

    public Map getCurrent() {
        return current
    }

    public Map getNext() {
        return next
    }

    public void setCurrent(Map currentMap) {
        current = new ConcurrentHashMap(currentMap)
    }

    public void setNext(Map nextMap) {
        next = new ConcurrentHashMap(nextMap)
    }

    @Override
    public Map getNow() {
        return current
    }

    private void reassociateObjectsWithErrors(Map scope) {
        for (Object key : scope.keySet()) {
            Object value = scope.get(key)
            if (value instanceof Map) {
                reassociateObjectsWithErrors((Map) value)
            }
            reassociateObjectWithErrors(scope, value)
        }
    }

    private void reassociateObjectWithErrors(Map scope, Object value) {
        if (value instanceof Collection) {
            Collection values = (Collection)value
            for (Object val : values) {
                reassociateObjectWithErrors(scope, val)
            }
        }
        else {
            String errorsKey = ERRORS_PREFIX + System.identityHashCode(value)
            Object errors = scope.get(errorsKey)
            if (value!=null && errors != null) {
                MetaClass mc = GroovySystem.getMetaClassRegistry().getMetaClass(value.getClass())
                if (mc.hasProperty(value, ERRORS_PROPERTY) != null) {
                    mc.setProperty(value, ERRORS_PROPERTY, errors)
                }
            }
        }
    }

    @Override
    public int size() {
        return current.size() + next.size()
    }

    @Override
    public void clear() {
        current.clear()
        next.clear()
    }

    @Override
    public boolean isEmpty() {
        return size() == 0
    }

    @Override
    public boolean containsKey(Object key) {
        return (current.containsKey(key) || next.containsKey(key))
    }

    @Override
    public boolean containsValue(Object value) {
        return (current.containsValue(value) || next.containsValue(value))
    }

    @Override
    public Collection values() {
        Collection c = new ArrayList()
        c.addAll(current.values())
        c.addAll(next.values())
        return c
    }

    @Override
    public void putAll(Map t) {
        for (Map.Entry<Object, Object> entry : ((Map<Object,Object>)t).entrySet()) {
            put(entry.getKey(), entry.getValue())
        }
    }

    @Override
    public Set entrySet() {
        Set entrySet = new HashSet()
        entrySet.addAll(current.entrySet())
        entrySet.addAll(next.entrySet())
        return entrySet
    }

    @Override
    public Set keySet() {
        Set keySet = new HashSet()
        keySet.addAll(current.keySet())
        keySet.addAll(next.keySet())
        return keySet
    }

    @Override
    public Object get(Object key) {
        if (next.containsKey(key)) {
            return next.get(key)
        }
        if ("now".equals(key)) {
            return getNow()
        }
        return current.get(key)
    }

    @Override
    public Object remove(Object key) {
        if (current.containsKey(key)) {
            return current.remove(key)
        }

        return next.remove(key)
    }

    @Override
    public Object put(Object key, Object value) {

        if (current.containsKey(key)) {
            current.remove(key)
        }
        storeErrorsIfPossible(next,value)

        if (value == null) {
            return next.remove(key)
        }

        return next.put(key,value)
    }

    private void storeErrorsIfPossible(Map scope,Object value) {
        if (value == null) {
            return
        }

        if (value instanceof Collection) {
            Collection values = (Collection)value
            for (Object val : values) {
                storeErrorsIfPossible(scope, val)
            }
        }
        else if (value instanceof Map) {
            Map map = (Map)value
            Collection keys = new LinkedList(map.keySet())
            for (Object key : keys) {
                Object val = map.get(key)
                storeErrorsIfPossible(map, val)
            }
        }
        else {
            MetaClass mc = GroovySystem.getMetaClassRegistry().getMetaClass(value.getClass())
            if (mc.hasProperty(value, ERRORS_PROPERTY) != null) {
                Object errors = mc.getProperty(value, ERRORS_PROPERTY)
                if (errors != null) {
                    scope.put(ERRORS_PREFIX + System.identityHashCode(value), errors)
                }
            }
        }
    }

}
