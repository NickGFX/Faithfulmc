package com.faithfulmc.util.buycraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

public class HTTPRequest {
    private final URL url;
    private HttpURLConnection httpURLConnection;

    public HTTPRequest(String url) throws MalformedURLException{
        this.url = new URL(url);
    }

    public HTTPRequest open() throws IOException{
        httpURLConnection = (HttpURLConnection) url.openConnection();
        return this;
    }

    public HTTPRequest requestMethod(String method) throws ProtocolException{
        httpURLConnection.setRequestMethod(method);
        return this;
    }

    public HTTPRequest setRequestProperty(String property, String value){
        httpURLConnection.setRequestProperty(property, value);
        return this;
    }

    public HTTPRequest useCaches(boolean useCaches){
        httpURLConnection.setUseCaches(useCaches);
        return this;
    }

    public HTTPRequest doInput(boolean doInput){
        httpURLConnection.setDoInput(doInput);
        return this;
    }

    public HTTPRequest doOutput(boolean doOutput){
        httpURLConnection.setDoOutput(doOutput);
        return this;
    }

    public HTTPRequest outputStream(HTTPRequestConsumer<OutputStream> function) throws IOException{
        function.accept(httpURLConnection.getOutputStream());
        return this;
    }

    public HTTPRequest inputStream(HTTPRequestConsumer<InputStream>  function) throws IOException{
        function.accept(httpURLConnection.getInputStream());
        return this;
    }

    public HTTPRequest errorStream(Function<InputStream, ?> function) throws IOException{
        function.apply(httpURLConnection.getErrorStream());
        return this;
    }

    public HTTPRequest disconnect(){
        httpURLConnection.disconnect();
        return this;
    }

    public <T> T disconnectAndReturn(HTTPRequestFunction<T> function) throws IOException{
        return function.apply(this);
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    @FunctionalInterface
    public interface HTTPRequestFunction<T>{
        T apply(HTTPRequest httpRequest) throws IOException;
    }

    @FunctionalInterface
    public interface HTTPRequestConsumer<T>{
        void accept(T t) throws IOException;
    }
}
