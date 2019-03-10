package edge.tester;

import com.google.gson.Gson;
import org.abelsromero.parallels.jobs.ExecutionDetails;
import org.abelsromero.parallels.jobs.JobsDetails;
import org.abelsromero.parallels.jobs.ParallelExecutor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

import static java.lang.String.format;

public class Tester {

    public static final String TOKEN = "";

    private final Gson gson = new Gson().newBuilder().create();

    public Tester() {
    }

    public Tester run() throws NoSuchAlgorithmException, KeyManagementException {


        final HttpClient httpClient = HttpClientFactory();

        final int executionsCount = 20000;
        final int threadCount = 20;

        final ParallelExecutor executor = new ParallelExecutor(threadCount, executionsCount);
        final ExecutionDetails result = executor.run(() -> {
            try {
                return sendPost(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }
        });


        System.out.println("Execution time (ms):\t" + result.getTime());
        final JobsDetails successfulJobs = result.getSuccessfulJobs();
        final JobsDetails failedJobs = result.getFailedJobs();

        System.out.println("Ratio (total ops/sec):\t" + result.getJobsPerSecond());
        System.out.println(format("Created: %s", successfulJobs.getCount()));
        System.out.println(format("Min: %s", successfulJobs.getMinTime()));
        System.out.println(format("Max: %s", successfulJobs.getMaxTime()));
        System.out.println(format("Avg: %s", successfulJobs.getAvgTime()));
        System.out.println(format("Failed: %s", failedJobs.getCount()));

        return this;
    }

    private HttpClient HttpClientFactory() throws NoSuchAlgorithmException, KeyManagementException {

        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)  // this is the default
                .sslContext(sc)
                .build();
    }

    private boolean sendPost(HttpClient httpClient) throws Exception {

        var data = Map.of(
                "cordinate", Map.of(
                        "latitude", 12.33,
                        "longitude", 24.66
                ),
                "altitude", 1.2,
                "horizontalAccuracy", 2.2,
                "verticalAccuracy", 2.2,
                "timestamp", System.currentTimeMillis()
        );
        var sensor = Map.of(
                "name", "sensor_name",
                "data", Arrays.asList(data)
        );
        var request = Map.of(
                "user_id", "myself",
                "sensors", Arrays.asList(sensor)
        );

        final HttpRequest mainRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8443/track"))
                .header("Content-Type", "application/json")
                .header("token", TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .build();

        HttpResponse<String> send = httpClient.send(mainRequest, HttpResponse.BodyHandlers.ofString());
        return send.statusCode() == 200;
    }

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
        new Tester()
                .run();
    }

}

