package com.synload.mtg.scryfall.api.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.json.*;


public class APIHandler {
  RestTemplate restTemplate = new RestTemplate();

  private static final Logger logger = LogManager.getLogger(APIHandler.class.getName());

  public TreeMap<String, TreeMap<String, TreeMap<Object, Object[]>>> cache = new TreeMap<>();
  public List<Object> objects = new ArrayList<>();
  public boolean changed = false;
  public boolean loaded = false;


  public APIHandler() {
    ObjectInputStream objectInputStream = null;
    try {
      if (new File("/data/data.db").exists()) {
        objectInputStream = new ObjectInputStream(new FileInputStream("/data/data.db"));
        List<Object> objects = (List<Object>) objectInputStream.readObject();
        this.objects = objects;
        List<Stack<Object>> loadOrder = new ArrayList<>();
        for(int x=0;x<9;x++) {
          loadOrder.add(new Stack<>());
        }
        objects.forEach(i -> {
          if(i.getClass().isAnnotationPresent(APIPriority.class)){
            int k = i.getClass().getAnnotation(APIPriority.class).value()-1;
            if(k==0) {
              JSONObject o = new JSONObject();
              for (Field f : i.getClass().getFields()) {
                if(f.isAnnotationPresent(APIClear.class)){
                  try {
                    if(f.getType()==List.class) {
                      f.set(i, new ArrayList<>());
                    }else{
                      f.set(i, null);
                    }
                  }catch (Exception e){
                    e.printStackTrace();
                  }
                }
                if (f.isAnnotationPresent(APIMapping.class)) {
                  if (f.getAnnotation(APIMapping.class).loadingVar()) {
                    try {
                      o.put(f.getName(), f.get(i));
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
              mapJsonToObject(i, o, false);
              addCache(i, true);
            }else{
              if(loadOrder.get(k)==null){
                loadOrder.add(k-1, new Stack<>());
              }
              loadOrder.get(k).add(i);
            }
          } else {
            loadOrder.get(5).add(i);
          }
        });
        loadOthers( loadOrder,1);
        //logger.info(new ObjectMapper().writeValueAsString(this.cache));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }finally{
      if(objectInputStream!=null) {
        try {
          objectInputStream.close();
        }catch (Exception e){
          e.printStackTrace();
        }
      }
    }
    loaded = true;
  }

  void loadOthers(List<Stack<Object>> loadOrder, int priority) {
    if(priority==9){
      return;
    }
    while(!loadOrder.get(priority).isEmpty()) {
      Object i = loadOrder.get(priority).pop();
      JSONObject o = new JSONObject();
      for (Field f : i.getClass().getFields()) {
        if(f.isAnnotationPresent(APIClear.class)){
          try {
            if(f.getType()==List.class) {
              f.set(i, new ArrayList<>());
            }else{
              f.set(i, null);
            }
          }catch (Exception e){
            e.printStackTrace();
          }
        }
        if (f.isAnnotationPresent(APIMapping.class)) {
          if (f.getAnnotation(APIMapping.class).loadingVar()) {
            try {
              o.put(f.getName(), f.get(i));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
      mapJsonToObject(i, o, false);
      addCache(i, true);
    }
    loadOthers(loadOrder, priority+1);
  }

  @Scheduled(fixedRate = 3000)
  void save() {
    if (changed) {
      changed = false;
      try {
        if(!new File("/data/").exists()) {
          new File("/data/").mkdir();
        }
        FileOutputStream fos = new FileOutputStream("/data/data.db");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(objects);
        oos.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public Object get(Object o) {
    if (!loaded) {
      return null;
    }
    Object oCached = checkCache(o);
    if (oCached != null) {
      logger.info("Cache Hit");
      return oCached;
    }
    String data = process(o);
    logger.info(data);
    if (data == null) {
      return null;
    }
    mapJsonToObject(o, new JSONObject(data), true);
    addCache(o, false);
    return o;
  }

  void mapJsonToObject(Object o, JSONObject obj, boolean apiData) {
    logger.info(o.getClass().getName() + " loaded");
    for (Field field : o.getClass().getFields()) {
      if (field.isAnnotationPresent(APIMapping.class) && apiData) {
        APIMapping map = field.getAnnotation(APIMapping.class);
        String[] keys = map.value();
        boolean used = false;
        for (String key : keys) {
          String[] paths = key.split("\\.");
          JSONObject tmp = obj;
          try {
            for (int i = 0; i < paths.length; i++) {
              if (!obj.has(paths[i])) {
                break;
              } else {
                if (i + 1 == paths.length) {
                  Object object = tmp.get(paths[i]);
                  if (field.getType().isInstance(object)) {
                    field.set(o, object);
                    used = true;
                  } else if (JSONArray.class.isInstance(object)) {
                    JSONArray array = ((JSONArray) object);
                    List items = new ArrayList<>();
                    for (int j = 0; j < array.length(); j++) {
                      items.add(array.get(j));
                    }
                    field.set(o, items);
                    used = true;
                  } else if (JSONObject.class.isInstance(object) && Map.class == field.getType()) {
                    JSONObject jsonObj = (JSONObject) object;
                    field.set(o, jsonObj.toMap());
                    used = true;
                  }
                } else {
                  tmp = obj.getJSONObject(paths[i]);
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (used) {
            break;
          }
        }
      } else if (field.isAnnotationPresent(APIClassMapping.class) && apiData) {
        APIClassMapping map = field.getAnnotation(APIClassMapping.class);
        String[] keys = map.value();
        Type type = field.getGenericType();
        Class clazz;
        if (type instanceof ParameterizedType) {
          ParameterizedType ptype = (ParameterizedType) type;
          if (ptype.getActualTypeArguments().length == 0) {
            break;
          }
          clazz = ((Class) ptype.getActualTypeArguments()[0]);
        } else {
          clazz = (Class) type;
        }
        boolean used = false;
        for (String key : keys) {
          String[] paths = key.split("\\.");
          JSONObject tmp = obj;
          try {
            for (int i = 0; i < paths.length; i++) {
              if (!obj.has(paths[i])) {
                break;
              } else {
                if (i + 1 == paths.length) {
                  Object object = tmp.get(paths[i]);

                  if (JSONObject.class.isInstance(object)) {
                    Object itemObj = clazz.newInstance();
                    mapJsonToObject(itemObj, (JSONObject) object, true);
                    field.set(o, itemObj);
                    used = true;
                    break;
                  } else if (JSONArray.class.isInstance(object)) {
                    List items = new ArrayList();
                    JSONArray array = (JSONArray) object;
                    for (int j = 0; j < array.length(); j++) {
                      if (JSONObject.class.isInstance(array.get(j))) {
                        Object itemObj = clazz.newInstance();
                        mapJsonToObject(itemObj, (JSONObject) array.get(j), true);
                        items.add(itemObj);
                      }
                    }
                    field.set(o, items);
                    used = true;
                    break;
                  }
                } else {
                  tmp = obj.getJSONObject(paths[i]);
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (used) {
            break;
          }
        }
      } else if (field.isAnnotationPresent(APIRequestMapping.class)) {
        APIRequestMapping map = field.getAnnotation(APIRequestMapping.class);
        String[] keys = map.value();
        String there = map.there();
        boolean used = false;
        for (String key : keys) {
          String[] paths = key.split("\\.");
          JSONObject tmp = obj;
          try {
            for (int i = 0; i < paths.length; i++) {
              if (!obj.has(paths[i])) {
                break;
              } else {
                if (i + 1 == paths.length) {
                  Object object = tmp.get(paths[i]);
                  if (JSONArray.class.isInstance(object)) {
                    List items = new ArrayList();
                    JSONArray array = (JSONArray) object;
                    for (int j = 0; j < array.length(); j++) {
                      Object oObject = field.getType().newInstance(); // Other Object
                      Field oField = oObject.getClass().getField(there);
                      oField.set(oObject, array.get(j));
                      oObject = get(oObject);
                      items.add(oObject);
                    }
                    field.set(o, items);
                    used = true;
                    break;
                  } else {
                    Object oObject = field.getType().newInstance(); // Other Object
                    Field oField = oObject.getClass().getField(there);
                    oField.set(oObject, object);
                    oObject = get(oObject);
                    field.set(o, oObject);
                    used = true;
                    break;
                  }
                } else {
                  tmp = obj.getJSONObject(paths[i]);
                }
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          if (used) {
            break;
          }
        }
      }
    }
    if (o.getClass().isAnnotationPresent(APILink.class)) {
      processLink(o, o.getClass().getAnnotation(APILink.class));
    }
  }

  public void processLink(Object o, APILink link) {
    String from = link.from();
    String to = link.to();
    String field = link.field();
    Class clazz = link.clazz();
    try {
      Field f1 = o.getClass().getField(from);
      Object data = f1.get(o);
      Object obj = clazz.newInstance();
      Field f2 = obj.getClass().getField(to);
      f2.set(obj, data);
      obj = checkCache(obj);
      if (obj != null) {
        Field addToField = clazz.getField(field);
        if (addToField != null) {
          if (addToField.getType() == List.class) {
            List<Object> objects = (List<Object>) addToField.get(obj);
            if (objects == null) {
              objects = new ArrayList<>();
            }
            objects.add(o);
            logger.info(o.getClass().getName() + " linked to " + obj.getClass().getName() + " (n)");
          } else if (addToField.getType() == clazz) {
            addToField.set(obj, o);
            logger.info(o.getClass().getName() + " linked to " + obj.getClass().getName() + " (1)");
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String process(Object o) {
    if (o.getClass().isAnnotationPresent(MultipleAPIRequest.class)) {
      MultipleAPIRequest mapir = o.getClass().getAnnotation(MultipleAPIRequest.class);
      APIRequest[] requests = mapir.value();
      for (APIRequest request : requests) {
        String data = handleGetRequest(request, o);
        if (data != null) {
          return data;
        }
      }
    } else if (o.getClass().isAnnotationPresent(APIRequest.class)) {
      APIRequest request = o.getClass().getAnnotation(APIRequest.class);
      if (request.cache() == 0) {
        return null;
      }
      String data = handleGetRequest(request, o);
      if (data != null) {
        return data;
      }
    }
    return null;
  }

  Object checkCacheInner(Object o, String keySet, boolean useCache) {
    try {
      Class clazz = o.getClass();
      String clazzName = clazz.getName();
      if (cache.containsKey(clazzName)) {
        TreeMap<String, TreeMap<Object, Object[]>> clazzObjects = cache.get(clazzName);
        if (clazzObjects.containsKey(keySet)) {
          TreeMap<Object, Object[]> clazzKeyObjects = clazzObjects.get(keySet);
          String key = keyBuilder(keySet, o);
          if (key != null) {
            if (clazzKeyObjects.containsKey(key)) {
              Object[] objCache = clazzKeyObjects.get(key);
              long timeout = (long) objCache[0];
              if (timeout > System.currentTimeMillis() || useCache) {
                return objCache[1];
              }
              clazzKeyObjects.remove(key);
              return null;
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  Object checkCache(Object o) {
    Class clazz = o.getClass();
    if (clazz.isAnnotationPresent(MultipleAPIRequest.class)) {
      MultipleAPIRequest mapir = o.getClass().getAnnotation(MultipleAPIRequest.class);
      APIRequest[] requests = mapir.value();
      for (APIRequest request : requests) {
        if (request.cache() == 0) {
          continue;
        }
        Object found = checkCacheInner(o, request.key(), (request.cache() == -1) ? true : false);
        if (found != null) {
          return found;
        }
      }
    } else if (clazz.isAnnotationPresent(APIRequest.class)) {
      APIRequest request = o.getClass().getAnnotation(APIRequest.class);
      Object found = checkCacheInner(o, request.key(), (request.cache() == -1) ? true : false);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  String keyBuilder(String keySet, Object o) {
    ObjectMapper om = new ObjectMapper();
    String[] keys = keySet.split(",");
    List<Object> items = new ArrayList<>();
    for (int i = 0; i < keys.length; i++) {
      String[] objs = keys[i].split("\\.");
      Object tmp = o;
      for (int j = 0; j < objs.length; j++) {
        try {
          //logger.info("adding field "+ objs[j] + " from "+new ObjectMapper().writeValueAsString(tmp));
          tmp = tmp.getClass().getField(objs[j]).get(tmp);
          if (tmp == null) {
            return null;
          }
        } catch (Exception e) {
          //e.printStackTrace();
          return null;
        }
      }
      if (tmp == null) {
        return null;
      }
      items.add(tmp);
    }
    try {
      return om.writeValueAsString(items);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  boolean addCacheInner(Object o, String keySet, long cacheTimeout) {
    try {
      Class clazz = o.getClass();
      String clazzName = clazz.getName();
      if (!cache.containsKey(clazzName)) {
        cache.put(clazzName, new TreeMap<String, TreeMap<Object, Object[]>>());
      }
      TreeMap<String, TreeMap<Object, Object[]>> clazzObjects = cache.get(clazzName);
      if (!clazzObjects.containsKey(keySet)) {
        clazzObjects.put(keySet, new TreeMap<Object, Object[]>());
      }
      TreeMap<Object, Object[]> clazzKeyObjects = clazzObjects.get(keySet);
      String key = keyBuilder(keySet, o);
      if (key != null) {
        clazzKeyObjects.put(key, new Object[]{System.currentTimeMillis() + cacheTimeout, o});
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  void addCache(Object o, boolean forceNoAdd) {
    Class clazz = o.getClass();
    boolean cached = false;
    if (clazz.isAnnotationPresent(MultipleAPIRequest.class)) {
      MultipleAPIRequest mapir = o.getClass().getAnnotation(MultipleAPIRequest.class);
      APIRequest[] requests = mapir.value();
      for (APIRequest request : requests) {
        if (request.cache() == 0) {
          continue;
        } else if (request.cache() == -1) {
          cached = true;
        }
        addCacheInner(o, request.key(), request.cache());
      }
    } else if (clazz.isAnnotationPresent(APIRequest.class)) {
      APIRequest request = o.getClass().getAnnotation(APIRequest.class);
      if (request.cache() == 0) {
        return;
      } else if (request.cache() == -1) {
        cached = true;
      }
      cached = addCacheInner(o, request.key(), request.cache());
    }
    if (cached) {
      if (!forceNoAdd) {
        try {
          logger.info("Added to cache " + o.getClass().getName());
        } catch (Exception e) {
          e.printStackTrace();
        }
        objects.add(o);
      }
      changed = true;
    }
  }

  String handleGetRequest(APIRequest request, Object o) {
    boolean useAPIRequest = true;
    String[] keys = request.key().split(",");
    Map<String, Object> values = new HashMap<String, Object>();
    try {
      for (String key : keys) {
        String[] fields = key.split("\\.");
        Object tmp = o;
        int i;
        int size = fields.length;
        for (i = 0; i < size; i++) {
          tmp = tmp.getClass().getField(fields[i]).get(tmp);
          if (tmp == null) {
            return null;
          }
        }
        values.put(key, tmp);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    String url = request.url();
    for (Map.Entry<String, Object> entry : values.entrySet()) {
      url = url.replace("{" + entry.getKey() + "}", entry.getValue().toString());
    }
    final HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Mobile Safari/537.36");
    final HttpEntity<String> entity = new HttpEntity<String>(headers);
    try {
      ResponseEntity<String> rest = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      String data = rest.getBody();
      return data;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
