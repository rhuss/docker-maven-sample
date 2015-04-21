package org.jolokia.docker.maven.sample.jolokia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * @author roland
 * @since 15.05.14
 */
public class LogServiceIT {

    @Test
    public void testLog() {
        long nonce = (int) (Math.random() * 10000);

        String url = System.getProperty("log.url");
        RestAssured.baseURI = url != null ? url : fetchUrl();
        RestAssured.defaultParser = Parser.TEXT;

        given()
                .param("mimeType", "application/json")
                .get("/" + nonce)
        .then().assertThat()
                .header("content-type", containsString("text/plain"))
                .body(containsString(nonce + ""));
    }

    private String fetchUrl() {
        String host = prepareHost(System.getProperty("log.host"));
        String port = System.getProperty("log.port");
        return "http://" + host + ":" + port;
    }

    private String prepareHost(String host) {
        Pattern dockerHostPattern = Pattern.compile("^tcp://(.*?)(:\\d+)?$");
        Matcher matcher = dockerHostPattern.matcher(host);
        return matcher.matches() ? matcher.group(1) : host;
    }
}
