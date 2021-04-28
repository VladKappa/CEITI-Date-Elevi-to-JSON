package kek.kappa;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Student {
    private String idnp;
    
    public Student(String idnp) {
        this.idnp = idnp;
    }
    
    public JSONObject getJSON() {
        Date date = new Date(getDocument());
        return date.toJSON();
    }
    
    public String getIDNP(){
        return this.idnp;
    }
    
    public Document getDocument(){
        String html = Networking.getStudentHTML(this);
        Document document = Jsoup.parse(html);
        return document;
    }
}