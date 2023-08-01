package me.TechsCode.GradeBasePlugin.extensions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

public class Downloader {
    
    private String basicAuth = null;
    
    public void authorize(String username, String password) {
        this.basicAuth = "Basic "
                + Base64.getEncoder().encodeToString((username + ':' + password).getBytes());
    }
    
    public File download(URL url, File dstFile) {
        CloseableHttpClient httpclient = HttpClients.custom().build();
        
        try {
            HttpGet get = new HttpGet(url.toURI());
            
            if (basicAuth != null) {
                get.setHeader("Authorization", basicAuth);
            }

            return httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            try {
                httpclient.close();
            }
            catch (IOException e) {}
        }
    }
    
    static class FileDownloadResponseHandler implements HttpClientResponseHandler<File> {
        
        private final File target;
        
        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }
        
        @Override
        public File handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), target);
            return target;
        }
    }
}
