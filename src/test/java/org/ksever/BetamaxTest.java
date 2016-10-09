package org.ksever;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URI;

import software.betamax.MatchRules;
import software.betamax.junit.Betamax;
import software.betamax.junit.RecorderRule;

import static org.junit.Assert.assertEquals;

/**
 * @author "Kostiantyn Severynov" <kostiantyn.severynov@gmail.com>
 */
public class BetamaxTest {

  private static final Logger LOG = LoggerFactory.getLogger(BetamaxTest.class);

  @Rule
  public RecorderRule recorderRule = new RecorderRule();

  @BeforeClass
  public static void setUp() throws Exception {
    // Enabling j.u.l logger bridge ot Slf4j API, http://www.slf4j.org/api/org/slf4j/bridge/SLF4JBridgeHandler.html
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Test
  @Betamax(tape = "test_http", match = {MatchRules.uri, MatchRules.method})
  public void betamaxHttpTest() throws Exception {
    HttpResponse response = callHttpEndpoint("http://google.com");
    verifyResponseIsOK(response);
  }

  @Test
  @Betamax(tape = "test_https", match = {MatchRules.uri, MatchRules.method})
  public void betamaxHttpsTest() throws Exception {
    // in case of HTTPS we still need endpoint to be available, since betamax
    // initiates CONNECT request to real endpoint even if it plays back from tape
    // see https://github.com/betamaxteam/betamax/issues/117
    HttpResponse response = callHttpEndpoint("https://google.com");
    verifyResponseIsOK(response);
  }

  private HttpResponse callHttpEndpoint(String uri) throws IOException {
    HttpClient client = HttpClientBuilder.create()
        // Use http.proxyHost and http.proxyPort from system properties, needed for betamax integration
        .useSystemProperties()
        .build();
    HttpGet get = new HttpGet(URI.create(uri));

    return client.execute(get);
  }

  private void verifyResponseIsOK(HttpResponse response) throws IOException {
    assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

    if (LOG.isDebugEnabled()) {
      String content = IOUtils.toString(response.getEntity().getContent());
      LOG.debug("HTTP response content: {}", content);
    }
  }
}
