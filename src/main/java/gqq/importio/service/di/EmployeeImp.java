package gqq.importio.service.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmployeeImp implements Employee {
	Logger logger = LoggerFactory.getLogger(EmployeeImp.class);

	
	private String name = "Peter2";

	
	private int id;
	
	public EmployeeImp() {
		logger.info("Dependency Injection call EmployeeImp constructor automatically");
	}
	
	@Override
	public void showEmployeeInfo() {
		System.out.println("Inside showEmployeeInfo() method.");
	}

	@Override
	public String getEmployee() {
		return String.format("{id : %d, name : %s}", id, name);
	}

}
