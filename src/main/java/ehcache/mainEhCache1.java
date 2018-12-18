package ehcache;

import java.io.File;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

public class mainEhCache1 {

	static Cache cache1;

	public static void main(String[] args) {

		String CONFIG_DIR = "src/main/resources/";
		CacheManager manager1;

		manager1 = new CacheManager(getConfiguration(CONFIG_DIR + "ehcache-1.xml").name("cm1"));

		cache1 = manager1.getCache("Entidade");

		cache1.put(new Element(1, "registro1"));
		cache1.put(new Element(2, "registro2"));

		Thread tCache = new Thread() {
			public void run() {

				System.out.println("Thread Running");

				while (true) {
					System.out.println("Quantide de Itens... " + cache1.getSize());
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
