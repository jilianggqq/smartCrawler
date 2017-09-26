package gqq.importio.service.di;

/**
 *  No qualifying bean of type 'gqq.importio.service.di.Department' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.inject(AutowiredAnnotationBeanPostProcessor.java:588)
	at org.springframework.beans.factory.annotation.InjectionMetadata.inject(InjectionMetadata.java:88)
	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.postProcessPropertyValues(AutowiredAnnotationBeanPostProcessor.java:366)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1264)
 * @author gqq
 *
 */
public interface Department {
	void showDepartmentInfo();
}
