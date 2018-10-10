package fr.codechill.spring.model;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Test;
import org.junit.Before;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.List;
@SpringBootTest
public class LanguageTest {
    private long id;
    private String name;
    private Docker docker;
    private Language language;
    private List <Docker> dockers;
    @Before 
    public void setUp() {
        this.id = 1L;
        this.name = "testLanguage";
        this.docker = new Docker ("test");
        this.dockers = new ArrayList<>();
        dockers.add(docker);
    }

    @Test
    public void testGetName() {
        this.language = new Language("test");
        this.language.setName(this.name);
        assertEquals(this.name, language.getName());;
    }

    @Test
    public void testSetName() {
        this.language = new Language("test");
        this.language.setName("testLanguage");
        assertEquals(this.name, this.language.getName());
    }

    @Test
    public void testgetDockers() {
        this.language = new Language ("test");
        this.language.setDockers(dockers);
        assertNotNull(this.language.getDockers());
    }

    @Test
    public void testSetDockers() {
        this.dockers.clear();
        this.dockers.add(this.docker);
        this.language = new Language("Test");
        this.language.setDockers(dockers);
        assertNotNull(this.language.getDockers());
    }
}