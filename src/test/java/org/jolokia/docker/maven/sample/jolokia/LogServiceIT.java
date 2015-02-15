package org.jolokia.docker.maven.sample.jolokia;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * @author roland
 * @since 15.05.14
 */
public class LogServiceIT {

    @Test
    public void testLog() {
        long nonce = (int) (Math.random() * 10000);

        RestAssured.baseURI = System.getProperty("log.url");
        RestAssured.defaultParser = Parser.TEXT;

        given()
                .param("mimeType", "application/json")
                .get("/" + nonce)
        .then().assertThat()
                .header("content-type", containsString("text/plain"))
                .body(containsString(nonce + ""));
    }
}
