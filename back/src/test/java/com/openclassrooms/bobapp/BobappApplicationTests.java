package com.openclassrooms.bobapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BobappApplicationTests {

	@Test
	void contextLoads() {
		int expected = 42;
        int actual = 24;
        assertEquals(expected, actual, "Ce test est censé échouer");
	}

}
