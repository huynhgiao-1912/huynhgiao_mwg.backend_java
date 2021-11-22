package tool.apichecker;

import java.net.URL;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.xml.XmlConfiguration;

public class MainTest {
	public static void main(String[] args) {
//		testCacheWithJavaConfig();
		testCacheWithXmlConfig();
	}
	
	public static void testCacheWithJavaConfig() {
		try(CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("preconfig", 
						CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)))
				.build();){
			cacheManager.init();
			
//			Cache<Long, String> preConfig = cacheManager.getCache("preconfig", Long.class, String.class);
			Cache<Long, String> mycache = cacheManager.createCache("mycache", 
					CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)));
			mycache.put(1L, "da one");
			String value = mycache.get(1L);
			System.out.println(value);
			mycache.remove(1L);
			
		}
	}
	
	public static void testCacheWithXmlConfig() {
		URL configURL = MainTest.class.getResource("/cache-config.xml");
		Configuration configObj = new XmlConfiguration(configURL); 
		try(CacheManager cacheManager = CacheManagerBuilder.newCacheManager(configObj)){
			
			cacheManager.init();
			
			Cache<Long, String> mycache = cacheManager.createCache("mycache", 
					CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)));
			mycache.put(1L, "da one");
			String value = mycache.get(1L);
			System.out.println(value);
			mycache.remove(1L);
		}
		
	}
}
