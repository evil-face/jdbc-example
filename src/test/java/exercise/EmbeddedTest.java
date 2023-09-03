package exercise;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedTest {

    private static Tomcat tomcat;
    private final String baseUrl = "http://localhost:8091/jdbc";

    @BeforeAll
    public static void setUp() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setBaseDir("build/tomcat");
        tomcat.setPort(8091);

        File docBase = new File("src/main/webapp");
        StandardContext ctx = (StandardContext)tomcat.addWebapp("/jdbc", docBase.getAbsolutePath());

        File additionWebInfClasses = new File("build/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources,
                "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(),
                "/"));
        ctx.setResources(resources);

        tomcat.init();
        tomcat.start();
    }

    @AfterAll
    public static void tearDown() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Test
    public void testWelcomePage() throws IOException, ParseException {
        CloseableHttpResponse response;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(baseUrl);
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String content = EntityUtils.toString(entity);

            assertThat(response.getCode()).isEqualTo(200);
            assertThat(content).contains("Welcome сервлет работает");
            assertThat(content).contains("Статьи");
        }
    }

    @Test
    public void testArticleFirstPage() throws IOException, ParseException {
        CloseableHttpResponse response;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(baseUrl + "/articles");
            response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String content = EntityUtils.toString(entity);

            assertThat(response.getCode()).isEqualTo(200);
            assertThat(content).contains("Tiger! Tiger!");
            assertThat(content).contains("In Dubious Battle");
            assertThat(content).doesNotContain("A Monstrous Regiment of Women");
        }
    }

    @Test
    public void testArticleSecondPage() throws IOException, ParseException {
        CloseableHttpResponse response;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request2 = new HttpGet(baseUrl + "/articles?page=2");
            CloseableHttpResponse response2 = client.execute(request2);

            HttpEntity entity2 = response2.getEntity();
            String content2 = EntityUtils.toString(entity2);

            assertThat(response2.getCode()).isEqualTo(200);

            assertThat(content2).contains("?page=1");
            assertThat(content2).contains("?page=3");

            assertThat(content2).contains("A Monstrous Regiment of Women");
            assertThat(content2).doesNotContain("Down to a Sunless Sea");
        }
    }

    @Test
    public void testShowArticlePage() throws IOException, ParseException {
        CloseableHttpResponse response;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(baseUrl + "/articles/39");
            response = client.execute(request);

            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            assertThat(response.getCode()).isEqualTo(200);
            assertThat(content).contains("A Passage to India");
            assertThat(content).contains("When you play a game of thrones you win or you die.");
        }
    }

    @Test
    public void testArticleNotFound() throws IOException, ParseException {
        CloseableHttpResponse response;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(baseUrl + "/articles/200");
            response = client.execute(request);

            assertThat(response.getCode()).isEqualTo(404);
        }
    }
}