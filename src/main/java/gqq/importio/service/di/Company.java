package gqq.importio.service.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Component annotation indicates that an annotated class is a "component".
 * 
 *            Such classes are considered as candidates for auto-detection
 * 
 *            when using annotation-based configuration and classpath scanning.
 * @author gqq
 *
 */
@Component
public class Company {
	private Employee employee;

	private Department department;
	
	// Constructor based DI
	@Autowired
	public Company(Employee employee) {
		this.employee = employee;
	}

	public void showEmployeeInfo() {
		employee.showEmployeeInfo();
	}

	public String getEmployee() {
		return employee.getEmployee();
	}

	public void showDepartmentInfo() {
		department.showDepartmentInfo();
	}

	@Autowired
	public void setDepartment(Department department) {
		this.department = department;
	}
}
