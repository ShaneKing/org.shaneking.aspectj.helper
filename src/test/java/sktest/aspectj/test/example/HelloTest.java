package sktest.aspectj.test.example;

import org.junit.Assert;
import org.junit.Test;
import org.shaneking.aspectj.test.SKAspectJUnit;

/**
 * @author David Zhang
 */
public class HelloTest extends SKAspectJUnit {
  @Test
  public void getLiveGreeting() {
    String expected = "Hello DAVID!";
    HelloService helloService = new HelloService();
    String greeting = helloService.sayHello("David");
    Assert.assertEquals("UpperCaseAspect must convert name to upper case.", expected, greeting);
  }
}
