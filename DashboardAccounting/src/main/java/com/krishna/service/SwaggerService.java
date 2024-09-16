package com.krishna.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SwaggerService {
	
	@Autowired RestTemplate restTemplate;
	
	
	  @Value("${dashAccounts.swagger.docs.url}") 
	  String url;
	 
	public Object testHome() throws Exception {
	   Map<String, Object> res = (Map<String, Object>) this.restTemplate.getForObject(url, Object.class);
	   return this.writeFile("spec.json", res );
	}

	public Object writeFile(String fileName, Map<String, Object> content) {
		 Map<String, Object> res = new HashMap<String, Object>();
		 Map<String, Object> paths = (Map<String, Object>) content.get("paths");
		 int val=0;
		 paths.entrySet().stream().forEach(p->{
			 List<String> tag = new ArrayList();

			 String key=p.getKey();
			 Map<String, Object> value=(Map<String, Object>) p.getValue();
			 Map<String, Object> tags= new HashMap<String, Object>();/*(Map<String, Object>) p.getValue();*/
			 if(value.get("post")!=null)
				tags= (Map<String, Object>) value.get("post");
			 else if(value.get("get")!=null)
					tags= (Map<String, Object>) value.get("get");
			 else if(value.get("put")!=null)
					tags= (Map<String, Object>) value.get("put");
			 else if(value.get("delete")!=null)
					tags= (Map<String, Object>) value.get("delete");
			 else if(value.get("patch")!=null)
					tags= (Map<String, Object>) value.get("patch");
			 tag=(List<String>) tags.get("tags");
			
			 if(res.get(tag.get(0))!=null && Integer.valueOf(res.get(tag.get(0)).toString())!=0 )
			 {
				 res.put(tag.get(0), Integer.valueOf(res.get(tag.get(0)).toString())+1);
			 }
			 else
			 res.put(tag.get(0), 1);
		
			
		 });
		 return res;
	}
		

}