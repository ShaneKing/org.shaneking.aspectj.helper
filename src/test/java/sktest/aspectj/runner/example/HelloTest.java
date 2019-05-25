package sktest.aspectj.runner.example;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shaneking.aspectj.runner.AspectJConfig;
import org.shaneking.aspectj.runner.AspectJUnit4Runner;

/**
 * @author David Zhang
 */
@AspectJConfig(classpathAdditions = "src/test/aspectj-resources")
@RunWith(AspectJUnit4Runner.class)
public class HelloTest {

  @Test
  public void getLiveGreeting() {
    String expected = "Hello DAVID!";
    HelloService helloService = new HelloService();
    String greeting = helloService.sayHello("David");
    Assert.assertEquals("UpperCaseAspect must convert name to upper case.", expected, greeting);
  }

}
