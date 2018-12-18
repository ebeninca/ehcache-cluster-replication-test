package ehcache;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

public class mainEhCache2 {

	static Cache cache2;

	public static void main(String[] args) {

		String CONFIG_DIR = "src/main/resources/";
		CacheManager manager2;

		manager2 = new CacheManager(getConfiguration(CONFIG_DIR + "ehcache-2.xml").name("cm2"));

		cache2 = manager2.getCache("Entidade");

		cache2.put(new Element(3, "registro3"));

		Thread tCache = new Thread() {
			public void run() {

				System.out.println("Thread Running");

				while (true) {
					System.out.println("Quantide de Itens... " + cache2.getSize());
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		tCache.start();

	}

	protected static Configuration getConfiguration(String fileName) {
		return ConfigurationFactory.parseConfiguration(new File(fileName));
	}
}
