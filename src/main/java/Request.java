import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String body;
    private String query;
    private List<NameValuePair> params;

    public Request(String method, String path, String body, String query) throws URISyntaxException {
        this.method = method;
        this.path = path;
        this.query = query;
        this.body = body;
        this.params = parse(path, query);
    }

    public List<NameValuePair> getQueryParam(String name) {
        List<NameValuePair> res = new ArrayList<NameValuePair>();
        for (NameValuePair n : params) {
            if (n.getName().equals(name)){
                res.add(new BasicNameValuePair(n.getName(), n.getValue()));
            }
        }
        return res;
    }

    public List<NameValuePair> getQueryParams() {
        return this.params;
    }

    public List<NameValuePair> parse(String s, String url) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));
        return params;
    }


    public String toString() {
        return "Request: METHOD: " + method + " PATH: " + path + " BODY: " + body + " QUERY: " + query;
    }


    public String getPath() {
        return path;
    }


    public String getBody() {
        return body;
    }


    public String getMethod() {
        return method;
    }

    public String getQuery() {
        return query;
    }

}
