package kek.kappa;

import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

public class Networking {
    public static String getStudentHTML(Student student)
    {
        String idnp = student.getIDNP();

        String url = "http://api.ceiti.md/date/login";

        HttpPost request = new HttpPost(url);

        List<NameValuePair> parametri = new ArrayList<>();
        parametri.add(new BasicNameValuePair("idnp", idnp));

        request.setEntity(new UrlEncodedFormEntity(parametri));

        HttpClient client = HttpClientBuilder.create().build();
        
        try {
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);

            HttpEntity ent = response.getEntity();
            return EntityUtils.toString(ent, "UTF-8");
        } catch (Exception e) {
            System.out.println("Eroare la executarea request-ului.");
        }

        return null;
    }

    
}
