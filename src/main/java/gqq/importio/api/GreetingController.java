package gqq.importio.api;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gqq.importio.service.restful.Greeting;

/**
 * This code uses Spring 4’s new @RestController annotation,
 * 
 * which marks the class as a controller where every method returns a domain object instead of a view.
 * 
 * It’s shorthand for @Controller and @ResponseBody rolled together.
 * 
 * @author gqq
 *
 */
@RestController
public class GreetingController {
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "Spring Boot Restful!") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
}
