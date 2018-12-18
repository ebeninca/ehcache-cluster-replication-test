package ehcache;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Direction;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.SearchException;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Classe que isola o framework ehcache e cria métodos auxiliares para
 * utilizá-lo.
 */
public class GerenciadorDeCache {

	/**
	 * recuperar um cache por seu id
	 * 
	 * @param nomeCache
	 * @return
	 */
	static Cache getCache(String nomeCache) {
		Cache cache = CacheManager.getInstance().getCache(nomeCache);
		if (cache == null) {
			throw new IllegalArgumentException("Nome de cache inválido: " + nomeCache);
		}
		return cache;
	}

	/**
	 * Adiciona um objeto no cache Default. Por padrão essa classe assume que o
	 * cache default do objeto é o valor de sua classe.getName().
	 * 
	 * @param chave
	 *            chave do objeto no cache
	 * @param valor
	 *            objeto a ser adicionado no cache
	 */
	static void adicionarNoCacheDefault(Object chave, Object valor) {
		getCache(valor.getClass().getName()).put(new Element(chave, valor));
	}

	/**
	 * Adiciona um objeto no cache.
	 * 
	 * @param nomeCache
	 *            nome do cache que se pretende fazer a busca
	 * @param chave
	 *            chave do objeto no cache
	 * @param valor
	 *            objeto a ser adicionado no cache
	 */
	static void adicionarNoCache(String nomeCache, Object chave, Object valor) {
		getCache(nomeCache).put(new Element(chave, valor));
	}

	/**
	 * Busca por um objeto no cache especificado. Esse método permite buscar um
	 * objeto em cache não apenas usando a chave. A busca pode ser feita por
	 * qualquer campo do objeto, desde que o campo seja configurado ao criar o
	 * cache.
	 * 
	 * @param nomeCache
	 *            nome do cache que se pretende fazer a busca
	 * @param campo
	 *            campo do objeto em cache para fazer a busca.
	 * @param valorCampo
	 *            valor do campo da busca.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> T buscarTONoCache(String nomeCache, String campo, Object valorCampo) {
		Results results = buscarNoCache(nomeCache, campo, valorCampo);
		return (T) (results == null ? null : converteResultsEmTO(results));
	}

	/**
	 * Busca por um objeto no cache especificado, por sua chave (chave que foi
	 * usada para adicionar no cache).
	 * 
	 * @param nomeCache
	 *            nome do cache que se pretende fazer a busca.
	 * @param chave
	 *            Valor da chave do objeto em cache.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> T buscarTONoCachePelaChave(String nomeCache, Object chave) {
		Element element = getCache(nomeCache).get(chave);
		return (T) (element == null ? null : element.getValue());
	}

	/**
	 * Busca por uma lista de objetos no cache especificado. Esse método permite
	 * buscar objetos usando qualquer campo dele, desde que o campo seja
	 * configurado ao criar o cache.
	 * 
	 * @param nomeCache
	 *            nome do cache que se pretende fazer a busca
	 * @param campo
	 *            campo do objeto em cache para fazer a busca.
	 * @param valorCampo
	 *            valor do campo da busca.
	 * @return
	 */
	static <T> List<T> buscarListaNoCache(String nomeCache, String campo, Integer valorCampo) {
		return buscarListaNoCache(nomeCache, campo, valorCampo, null);
	}

	/**
	 * Busca por uma lista de objetos no cache especificado ordenadas pelo campo
	 * nomeAtributoOrderBy. Esse método permite buscar objetos usando qualquer
	 * campo dele, desde que o campo seja configurado ao criar o cache.
	 * 
	 * @param nomeCache
	 *            nome do cache que se pretende fazer a busca
	 * @param campo
	 *            campo do objeto em cache para fazer a busca.
	 * @param valorCampo
	 *            valor do campo da busca.
	 * @param nomeAtributoOrderBy
	 *            nome do campo do objeto em cache pelo qual a lista será
	 *            ordenada.
	 * @return
	 */
	static <T> List<T> buscarListaNoCache(String nomeCache, String campo, Object valorCampo,
			String nomeAtributoOrderBy) {
		Results results = buscarNoCache(nomeCache, campo, valorCampo, nomeAtributoOrderBy);
		return converteResultsEmLista(results);
	}

	static Results buscarNoCache(String nomeCache, String campo, Object valorCampo) {
		return buscarNoCache(nomeCache, campo, valorCampo, null);
	}

	// @SuppressWarnings({ "rawtypes", "unchecked" })
	static <T> Results buscarNoCache(String nomeCache, String campo, Object valorCampo,
			String nomeAtributoOrderBy) {

		if (valorCampo == null) {
			return null;
		}
		Cache cache = getCache(nomeCache);

		Query query = cache.createQuery();
		if (valorCampo instanceof Number) {
			Attribute<Number> campoAttr = cache.getSearchAttribute(campo);
			query.includeValues().addCriteria(campoAttr.eq((Number) valorCampo));
		} else if (valorCampo instanceof String) {
			Attribute<String> campoAttr = cache.getSearchAttribute(campo);
			query.includeValues().addCriteria(campoAttr.eq((String) valorCampo));
		}

		if (nomeAtributoOrderBy != null) {
			Attribute<?> atributo = cache.getSearchAttribute(nomeAtributoOrderBy);
			query.addOrderBy(atributo, Direction.ASCENDING);
		}
		Results results = query.execute();
		return results;
	}

	/**
	 * Método que recebe o objeto Results do ehcache e retorna uma lista de
	 * valores no formato passado
	 * 
	 * @param t
	 * @param results
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> List<T> converteResultsEmLista(Results results) {
		List<T> retorno = new ArrayList<T>();

		if (results != null && results.size() > 0) {
			List<Result> all = results.all();
			for (Result result : all) {
				retorno.add((T) result.getValue());
			}
		}
		return retorno;
	}

	/**
	 * Método que recebe o objeto Results do ehcache e retorna uma TO com o
	 * valor no formato passado
	 * 
	 * @param t
	 * @param results
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static <T> T converteResultsEmTO(Results results) {
		List<Result> all = results.all();
		T t = null;
		if (all.size() > 0) {
			t = (T) all.get(0).getValue();
		}

		return t;
	}

	/**
	 * Cria um cache com indices de busca, os quais são passados pelo parâmetro
	 * "camposDeBusca".
	 * 
	 * @param nome
	 * @param camposDeBusca
	 *            deve ser o valor do campo get do objeto que ficará em cache.
	 *            Por exemplo, se o método é getNome, o valor deve ser Nome
	 * @return
	 */
	static Cache criarCache(String nome, boolean eterno, Long timeToLiveSeconds, String... camposDeBusca) {
		return criarCache(nome, eterno, timeToLiveSeconds, null, camposDeBusca);
	}

	static Cache criarCache(String nome, boolean eterno, Long timeToLiveSeconds, Searchable searchable,
			String... camposDeBusca) {
		return criarCache(nome, eterno, timeToLiveSeconds, false, searchable, camposDeBusca);
	}

	static Cache criarCache(String nome, boolean eterno, Long timeToLiveSeconds, boolean cacheReplication,
			String... camposDeBusca) {
		return criarCache(nome, eterno, timeToLiveSeconds, cacheReplication, null, camposDeBusca);
	}

	static Cache criarCache(String nome, boolean eterno, Long timeToLiveSeconds, boolean cacheReplication,
			Searchable searchable, String... camposDeBusca) {

		if (camposDeBusca != null) {
			searchable = new Searchable();
			for (String campo : camposDeBusca) {
				searchable.addSearchAttribute(
						new SearchAttribute().name(campo).expression("value.get" + campo + "()"));
			}
		}

		timeToLiveSeconds = (timeToLiveSeconds == null ? (60L * 60 * 4) : timeToLiveSeconds);
		CacheConfiguration cacheConfiguration = new CacheConfiguration(nome, -1)
				.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).eternal(eterno)
				.timeToLiveSeconds(timeToLiveSeconds).timeToIdleSeconds(timeToLiveSeconds / 2)
				.diskExpiryThreadIntervalSeconds(60L * 5).diskPersistent(false);

		if (searchable != null) {
			cacheConfiguration.searchable(searchable);
		}

		Cache cache = criarCache(nome, cacheConfiguration, cacheReplication);
		return cache;
	}

	/**
	 * Cria um cache com as definições passadas no parâmetro cacheConfiguration.
	 * 
	 * @param nome
	 * @param cacheConfiguration
	 * @return
	 */
	private static Cache criarCache(String nome, CacheConfiguration cacheConfiguration,
			boolean cacheReplication) {
		Cache cache = CacheManager.getInstance().getCache(nome);
		if (cache != null) {
			return cache;
		}

		Cache newCache = new Cache(cacheConfiguration);

		CacheManager.getInstance().addCache(newCache);

		// notificando a entrada de novo cache para replicação RMI em cluster
		if (cacheReplication) {

			Properties replicationProperties = new Properties();
			replicationProperties.put("replicatePuts", "true");
			replicationProperties.put("replicateUpdates", "true");
			replicationProperties.put("replicateRemovals", "true");
			replicationProperties.put("replicateUpdatesViaCopy", "false");
			replicationProperties.put("replicateAsynchronously", "true");
			replicationProperties.put("asynchronousReplicationIntervalMillis", "500");

			newCache.getCacheEventNotificationService().registerListener(
					new RMICacheReplicatorFactory().createCacheEventListener(replicationProperties));
			CacheManager.getInstance().getCacheManagerEventListenerRegistry().notifyCacheAdded(nome);
		}

		return newCache;
	}

	static <T> List<T> buscarListaTotalNoCache(String name, String nomeAtributoOrderBy)
			throws SearchException {
		Cache cache = getCache(name);
		Query query = cache.createQuery().includeValues();

		if (nomeAtributoOrderBy != null) {
			Attribute<?> atributo = cache.getSearchAttribute(nomeAtributoOrderBy);
			query.addOrderBy(atributo, Direction.ASCENDING);
		}

		Results results = query.execute();
		return converteResultsEmLista(results);
	}

	static <T> List<T> buscarListaTotalNoCache(String name) {
		return buscarListaTotalNoCache(name, null);
	}

	static void removerTodos(String cacheName) {
		getCache(cacheName).removeAll();
	}

	static void removerDoCache(String cacheName, Object key) {
		getCache(cacheName).remove(key);
	}

	static <T> void removerItensDoCache(String nomeCache, String campo, Object valorCampo) {

		Results results = buscarNoCache(nomeCache, campo, valorCampo, null);

		if (results != null) {
			List<Result> all = results.all();
			Cache cache = getCache(nomeCache);
			for (Result result : all) {
				cache.remove(result.getKey());
			}
		}
	}

}