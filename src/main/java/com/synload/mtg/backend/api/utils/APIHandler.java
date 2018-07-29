package com.synload.mtg.backend.api.utils;


import ch.qos.logback.core.net.ObjectWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synload.mtg.backend.database.models.mtg.MTGSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import org.json.*;

public class APIHandler {
  RestTemplate restTemplate = new RestTemplate();

  static TreeMap<String, TreeMap<String, TreeMap<Object, Object[]>>> cache = new TreeMap<>();

  public class Data {
    String data;
    public Data(String data) {
      this.data = data;
    }
    public String getData() {
      return data;
    }
    public void setData(String data) {
      this.data = data;
    }
  }

  public Object get(Object o) {
    Object oCached = checkCache(o);
    if(oCached != null){
      System.out.println("Cache Hit");
      return oCached;
    }
    String data = process(o);
    mapJsonToObject(o, new JSONObject(data));
    addCache(o);
    ObjectMapper om = new ObjectMapper();
    try {
      System.out.println(om.writeValueAsString(cache));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return o;
  }

  void mapJsonToObject(Object o, JSONObject obj){
    for (Field field : o.getClass().getFields()) {
      if (field.isAnnotationPresent(APIMapping.class)) {
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
                    JSONArray array = ((JSONArray)object);
                    List items = new ArrayList<>();
                    for(int j=0;j<array.length();j++){
                      items.add(array.get(j));
                    }
                    field.set(o, items);
                    used = true;
                  } else if(JSONObject.class.isInstance(object) && Map.class==field.getType()){
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
          if(used){
            break;
          }
        }
      }else if(field.isAnnotationPresent(APIClassMapping.class)){
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

                  if(JSONObject.class.isInstance(object)){
                    Object itemObj = clazz.newInstance();
                    mapJsonToObject( itemObj, (JSONObject)object);
                    field.set(o, itemObj);
                    used = true;
                    break;
                  } else if(JSONArray.class.isInstance(object)){
                    List items = new ArrayList();
                    JSONArray array =(JSONArray)object;
                    for(int j = 0; j < array.length(); j++ ){
                      if(JSONObject.class.isInstance(array.get(j))) {
                        Object itemObj = clazz.newInstance();
                        mapJsonToObject(itemObj, (JSONObject)array.get(j));
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
          if(used){
            break;
          }
        }
      }else if(field.isAnnotationPresent(APIRequestMapping.class)){
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
                  if(JSONArray.class.isInstance(object)){
                    List items = new ArrayList();
                    JSONArray array =(JSONArray)object;
                    for(int j = 0; j < array.length(); j++ ){
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
          if(used){
            break;
          }
        }
      }
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
      if(request.cache()==0){
        return null;
      }
      String data = handleGetRequest(request, o);
      if (data != null) {
        return data;
      }
    }
    return null;
  }
  Object checkCacheInner(Object o, String keySet){
    try {
      Class clazz = o.getClass();
      String clazzName = clazz.getName();
      if(cache.containsKey(clazzName)){
        TreeMap<String, TreeMap<Object, Object[]>> clazzObjects = cache.get(clazzName);
        if(clazzObjects.containsKey(keySet)){
          TreeMap<Object, Object[]> clazzKeyObjects = clazzObjects.get(keySet);
          String key = keyBuilder(keySet, o);
          if(key!=null) {
            if (clazzKeyObjects.containsKey(key)) {
              Object[] objCache = clazzKeyObjects.get(key);
              long timeout = (long) objCache[0];
              if (timeout > System.currentTimeMillis()) {
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
        if(request.cache()==0){
          continue;
        }
        Object found = checkCacheInner(o, request.key());
        if (found != null) {
          return found;
        }
      }
    } else if (clazz.isAnnotationPresent(APIRequest.class)) {
      APIRequest request = o.getClass().getAnnotation(APIRequest.class);
      Object found = checkCacheInner(o, request.key());
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
    for(int i=0;i<keys.length;i++){
      String[] objs = keys[i].split("\\.");
      Object tmp=o;
      for(int j=0;j<objs.length;j++){
        try {
          tmp=tmp.getClass().getField(objs[j]).get(tmp);
          if (tmp==null) {
            return null;
          }
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
      if(tmp==null){
        return null;
      }
      items.add(tmp);
    }
    try {
      return om.writeValueAsString(items);
    }catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }

  Object addCacheInner(Object o, String keySet, long cacheTimeout){
    try {
      Class clazz = o.getClass();
      String clazzName = clazz.getName();
      if(!cache.containsKey(clazzName)) {
        cache.put(clazzName, new TreeMap<String, TreeMap<Object, Object[]>>());
      }
      TreeMap<String, TreeMap<Object, Object[]>> clazzObjects = cache.get(clazzName);
      if(!clazzObjects.containsKey(keySet)) {
        clazzObjects.put(keySet, new TreeMap<Object, Object[]>());
      }
      TreeMap<Object, Object[]> clazzKeyObjects = clazzObjects.get(keySet);
      String key = keyBuilder(keySet, o);
      if(key!=null) {
        clazzKeyObjects.put(key, new Object[]{System.currentTimeMillis()+cacheTimeout, o});
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  void addCache(Object o) {
    Class clazz = o.getClass();
    if (clazz.isAnnotationPresent(MultipleAPIRequest.class)) {
      MultipleAPIRequest mapir = o.getClass().getAnnotation(MultipleAPIRequest.class);
      APIRequest[] requests = mapir.value();
      for (APIRequest request : requests) {
        if(request.cache()==0){
          continue;
        }
        addCacheInner(o, request.key(), request.cache());
      }
    } else if (clazz.isAnnotationPresent(APIRequest.class)) {
      APIRequest request = o.getClass().getAnnotation(APIRequest.class);
      if(request.cache()==0){
        return;
      }
      addCacheInner(o, request.key(), request.cache());
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
      System.out.println(entry.getKey() + " : " + entry.getValue());
      url = url.replace("{" + entry.getKey() + "}", entry.getValue().toString());
    }
    final HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Mobile Safari/537.36");
    final HttpEntity<String> entity = new HttpEntity<String>(headers);
    try {
      ResponseEntity<String> rest = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      String data = rest.getBody();
      return data;
    }catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }
}
