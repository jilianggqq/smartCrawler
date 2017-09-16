package gqq.importio.dao.service;

import java.util.List;

import gqq.importio.dao.model.RedisUrl;

/**
 * Created by jt on 1/10/17.
 */
public interface RedisUrlService {

    List<RedisUrl> listAll();

    RedisUrl getByUrl(String id);

    RedisUrl saveOrUpdate(RedisUrl url);

    void delete(String id);
    
    boolean exists(String id);
}
