@Component
Tell the spring that you need to manage the instance of its following class. Sprint will create that instance of that class for you.
@autowird
spring will look for matching instance among the instances it manages.

Beans:different Components that are managed by spring frameworks.
autowird: spring finds its dependencies, the matches of the dependencies and initiate its dependencies.
DI: we are injecting the properties as dependencies into the class.
IOC(Inversion of control): we take control from the class that needs the dependency and give the control to the spring framework.
IOC container: represent anything that is implementing Inversion of control.
