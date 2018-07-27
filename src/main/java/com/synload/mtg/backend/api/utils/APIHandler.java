package com.synload.mtg.backend.api.utils;


import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class APIHandler {
    RestTemplate restTemplate = new RestTemplate();
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
    public Object get(Object o){
        String data = process(o);
        System.out.println(data);
        return null;
    }
    public String process(Object o) {
        if(o.getClass().isAnnotationPresent(MultipleAPIRequest.class)){
            MultipleAPIRequest mapir = o.getClass().getAnnotation(MultipleAPIRequest.class);
            APIRequest[] requests = mapir.value();
            for(APIRequest request: requests){
                String data = handleRequest(request,o);
                if(data!=null){
                    return data;
                }
            }
        } else if(o.getClass().isAnnotationPresent(APIRequest.class)){
            APIRequest request = o.getClass().getAnnotation(APIRequest.class);
            String data = handleRequest(request,o);
            if(data!=null){
                return data;
            }
        }
        return null;
    }
    String handleRequest(APIRequest request, Object o) {
        boolean useAPIRequest = true;
        String[] keys = request.key().split(",");
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            for (String key : keys) {
                String[] fields = key.split("\\.");
                Object tmp = o;
                int i;
                int size = fields.length;
                for(i=0;i<size;i++){
                    Field f = o.getClass().getField(fields[i]);
                    Object x = f.get(tmp);
                    if (tmp==null || tmp.equals("")) {
                        useAPIRequest = false;
                        break;
                    } else if (i+1 == size) {
                        values.put(key, x);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            useAPIRequest = false;
        }
        if(!useAPIRequest){
            return null;
        }
        String url = request.url();
        for(Map.Entry<String, Object> entry: values.entrySet()){
            url = url.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        };
        ResponseEntity<String> rest =  restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String data = rest.getBody();
        return data;
    }
}
