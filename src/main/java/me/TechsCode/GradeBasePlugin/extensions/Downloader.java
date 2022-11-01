package me.TechsCode.GradeBasePlugin.extensions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class Downloader {

    private String basicAuth = null;

    public void authorize(String username, String password) {
        String userCredentials = username + ":" + password;
        this.basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
    }

    public File download(URL url, File dstFile) {
        CloseableHttpClient httpclient = HttpClients.custom()
                .build();
        try {
            HttpGet get = new HttpGet(url.toURI());

            if (basicAuth != null) {
                get.setHeader("Authorization", basicAuth);
            }

            return httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(httpclient);
        }
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {

        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }

    }

}
