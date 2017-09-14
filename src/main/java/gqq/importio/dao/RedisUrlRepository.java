package gqq.importio.dao;

import org.springframework.data.repository.CrudRepository;

import gqq.importio.dao.model.RedisUrl;

/**
 * Created by jt on 1/10/17.
 */
public interface RedisUrlRepository extends CrudRepository<RedisUrl, String> {
}
