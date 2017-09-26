package gqq.importio.service.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DepartmentImpl implements Department {

	Logger logger = LoggerFactory.getLogger(Department.class);

	public DepartmentImpl() {
		logger.info("Dependency Injection call DepartmentImpl constructor automatically");
	}

	@Override
	public void showDepartmentInfo() {
		logger.info("This is Department A!");
	}

}
