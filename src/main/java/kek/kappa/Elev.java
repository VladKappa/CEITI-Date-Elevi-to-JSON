package kek.kappa;


import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Elev {
    String idnp;
    String date_html;
    DateElev date;

    public Elev(String idnp) {
        this.idnp = idnp;
        getDate();
    }

    public JSONObject getJSON() {
        return date.getJSON();
    }

    private void getDate() {
        File f = new File("date.html");
        if (f.exists() && !f.isDirectory()) {
            try {
                this.date_html = Files.readString(Path.of(f.getName()));
                this.date = new DateElev(this.date_html);

                if (!this.date_html.trim().equals(""))
                    return;
            } catch (Exception e) {
                System.out.println("Nu am putut citi din fisier, fac un request.");
            }
        }
        HttpClientBuilder client = HttpClientBuilder.create();
        // client.setRedirectStrategy(new LaxRedirectStrategy());
        HttpPost request = new HttpPost("http://api.ceiti.md/date/login");

        List<NameValuePair> parametri = new ArrayList<>();
        parametri.add(new BasicNameValuePair("idnp", this.idnp));
        request.setEntity(new UrlEncodedFormEntity(parametri));

        CloseableHttpResponse response = null;
        try {
            response = client.build().execute(request);
        } catch (IOException e) {
            System.out.println("Nu am putut executa requestul. Ai acces la internet?");
        }

        try {
            assert response != null;
            HttpEntity ent = response.getEntity();
            this.date_html = EntityUtils.toString(ent, "UTF-8");
            this.date = new DateElev(this.date_html);
        } catch (Exception e) {
            System.out.println("Nu am putut lua datele.");
        }

        try (PrintWriter out = new PrintWriter("date.html")) {
            out.println(this.date_html);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class DateElev {
        Document soup;
        JSONObject DateJSON;

        public DateElev(String html) {
            this.soup = Jsoup.parse(html);
            JSONify();
        }

        public JSONObject getJSON() {
            return this.DateJSON;
        }

        // Hack to create ordered JSON Object :D
        private JSONObject OrderedJSONObject() {
            JSONObject obj = new JSONObject();
            try {
                Field changeMap = obj.getClass().getDeclaredField("map");
                changeMap.setAccessible(true);
                changeMap.set(obj, new LinkedHashMap<>());
                changeMap.setAccessible(false);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            return obj;
        }

        private void JSONify() {
            DateJSON = OrderedJSONObject();
            String[] atribute = getAttributes();
            for (String atribut : atribute) {
                String nume_titlu = soup.select("a[aria-controls=" + atribut + "]").text();
                DateJSON.put(nume_titlu, "");
                if (!soup.select("#" + atribut + " .panel-title").isEmpty()) { // Daca sunt submeniuri
                    Elements nume_subcategorii = soup.select("#" + atribut + " .panel-title");
                    Elements tabele = soup.select("#" + atribut + " div[role=tabpanel] table");
                    ArrayList<String> HeaderNames = new ArrayList<>();
                    JSONObject subcategorii = OrderedJSONObject();
                    for (int i = 0; i < nume_subcategorii.toArray().length; i++) {
                        JSONArray JSONArrayRow = new JSONArray();
                        Elements rows = tabele.get(i).select("tr");
                        for (Element r : rows) {
                            // We get the <th> header names
                            if (!r.select("th").isEmpty()) {
                                HeaderNames.clear();
                                Elements headers = r.select("th");
                                for (Element header : headers) {
                                    HeaderNames.add(header.text());
                                }
                            }
                            // Wrapper pentru absente :)
                            if(HeaderNames.get(0).equals("Absențe totale"))
                            {
                                JSONObject AbsenteOBJ = new JSONObject();
                                JSONArray AbsenteArr = new JSONArray();
                                JSONObject AbsenteOBJrow = OrderedJSONObject();
                                AbsenteOBJrow.put(HeaderNames.get(0),HeaderNames.get(1));
                                r = r.nextElementSibling();
                                AbsenteArr.put(AbsenteOBJrow);
                                for (int j=0;j<3;j++){
                                    AbsenteOBJrow = OrderedJSONObject();
                                    Elements TableData = r.select("td");
                                    AbsenteOBJrow.put(TableData.get(0).text(),TableData.get(1).text());
                                    AbsenteArr.put(AbsenteOBJrow);
                                    r = r.nextElementSibling();
                                }
                                AbsenteOBJ.put("Absente",AbsenteArr);
                                JSONArrayRow.put(AbsenteOBJ);
                                break;
                            }

                            Elements TableData = r.select("td");
                            JSONObject DataObject = OrderedJSONObject();
                            for (int j = 0; j < TableData.toArray().length; j++) {
                                if (HeaderNames.get(j).equals("Semestrul II"))
                                    HeaderNames.set(j,"Denumire");
                                if (HeaderNames.get(j).equals("Denumirea Obiectelor"))
                                    HeaderNames.set(j,"Denumire Obiect");

                                // Handle pentru note, le facem JSONArray :^)
                                if(HeaderNames.get(j).equals("Note"))
                                {
                                    JSONArray ArrayNote = new JSONArray();
                                    String[] Note = TableData.get(j).text().split("\\s*,\\s*");
                                    for(String nota : Note)
                                        ArrayNote.put(nota);
                                    DataObject.put(HeaderNames.get(j), ArrayNote);
                                    continue;
                                }

                                DataObject.put(HeaderNames.get(j), TableData.get(j).text());
                            }

                            if (!DataObject.isEmpty())
                                JSONArrayRow.put(DataObject);
                        }
                        subcategorii.put(nume_subcategorii.get(i).text(), JSONArrayRow);
                    }
                    DateJSON.put(nume_titlu, subcategorii);
                    continue;
                }
                Element tabel = soup.select("div[id=" + atribut + "] table").first();
                if(atribut.equals("date-personale"))
                {
                    Elements rows = tabel.select("tr");
                    JSONObject DatePersonale = OrderedJSONObject();
                    for (Element r : rows)
                        DatePersonale.put(r.child(0).text(),r.child(1).text());
                    DateJSON.put(nume_titlu,DatePersonale);
                    continue;
                }
                Elements rows = tabel.select("tr");
                ArrayList<String> HeaderNames = new ArrayList<>();
                JSONArray Arr = new JSONArray();
                for (Element r : rows) {
                    JSONObject Obj = OrderedJSONObject();
                    // We get the <th> header names
                    if (!r.select("th").isEmpty()) {
                        HeaderNames.clear();
                        Elements headers = r.select("th");
                        for (Element header : headers) {
                            HeaderNames.add(header.text());
                        }
                    }
                    Elements TableData = r.select("td");

                    for (int j = 0; j < TableData.toArray().length; j++) {
                        Obj.put(HeaderNames.get(j), TableData.get(j).text());
                    }
                    if (!Obj.isEmpty())
                        Arr.put(Obj);
                }
                DateJSON.put(nume_titlu,Arr);
            }
        }

        private String[] getAttributes() {
            Elements ul = soup.select("ul[role=tablist] li");
            String[] attributes = new String[ul.toArray().length];
            for (int i = 0; i < ul.toArray().length; i++)
                attributes[i] = ul.get(i).select("> a").attr("aria-controls");

            return attributes;
        }

    }
}