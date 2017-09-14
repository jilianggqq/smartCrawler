package gqq.importio.dao.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gqq.importio.dao.RedisUrlRepository;
import gqq.importio.dao.model.RedisUrl;

/**
 * Created by jt on 1/10/17.
 */
@Service
public class RedisUrlServiceImpl implements RedisUrlService {

    private RedisUrlRepository productRepository;

    @Autowired
    public RedisUrlServiceImpl(RedisUrlRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public List<RedisUrl> listAll() {
        List<RedisUrl> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add); //fun with Java 8
        return products;
    }

    @Override
    public RedisUrl getById(String id) {
        return productRepository.findOne(id);
    }

    @Override
    public RedisUrl saveOrUpdate(RedisUrl product) {
        productRepository.save(product);
        return product;
    }

    @Override
    public void delete(String id) {
        productRepository.delete(id);

    }


	@Override
	public boolean exists(String id) {
		return productRepository.exists(id);
	}
}
