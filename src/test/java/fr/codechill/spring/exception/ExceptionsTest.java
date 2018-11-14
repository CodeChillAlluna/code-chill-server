package fr.codechill.spring.exception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import edu.umd.cs.findbugs.classfile.ResourceNotFoundException;
import fr.codechill.spring.CodeChillApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=CodeChillApplication.class)
@WebAppConfiguration
public class ExceptionsTest {

    @Test(expected = AccessDeniedException.class)
    public void testAccessDeniedException() throws Exception {
        throw new AccessDeniedException("test");
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestException() throws Exception {
        throw new BadRequestException("test");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testResourceNotFoundException() throws Exception {
        throw new ResourceNotFoundException("test");
    }

    @Test(expected = ServerErrorException.class)
    public void testServerErrorException() throws Exception {
        throw new ServerErrorException("test");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUnauthorizedException() throws Exception {
        throw new UnauthorizedException("test");
    }

}