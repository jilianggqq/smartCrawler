package gqq.importio.service.di;

/**
 * We must use just one Implement for this interface.
 * 
 * No qualifying bean of type 'gqq.importio.service.di.Employee' available:
 * 
 * expected single matching bean but found 2: employeeImp,employeeImp2
 * 
 * @author gqq
 *
 */
public interface Employee {
	void showEmployeeInfo();

	String getEmployee();
}
