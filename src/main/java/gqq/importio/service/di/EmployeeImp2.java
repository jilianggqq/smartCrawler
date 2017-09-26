package gqq.importio.service.di;

import org.springframework.stereotype.Service;

//@Service
public class EmployeeImp2 implements Employee {

	private String name = "Peter";
	private int id;
	
	@Override
	public void showEmployeeInfo() {
		System.out.println("Inside showEmployeeInfo() method.");
	}

	@Override
	public String getEmployee() {
		return String.format("{id : %d, name : %s}", id, name);
	}

}
