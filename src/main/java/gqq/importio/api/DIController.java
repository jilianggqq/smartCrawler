package gqq.importio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gqq.importio.service.di.Company;

@RestController
public class DIController {
	Logger logger = LoggerFactory.getLogger(DIController.class);

	@Autowired
	private Company company;
	
	@RequestMapping("/di")
	public String testdi() {
		company.showEmployeeInfo();
		logger.info(company.getEmployee());
		company.showDepartmentInfo();
		return "test ok";
	}
}
