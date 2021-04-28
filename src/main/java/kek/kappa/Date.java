package kek.kappa;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Date {
    private Document html;
    
    public Date(Document html)
    {
        this.html = html;
    }
    
    public JSONObject toJSON() {
        JSONObject DateJSON = OrderedJSONObject();
        String[] atribute = tablistAttributeValues();
        for (String atribut : atribute) {
            String nume_titlu = html.select("a[aria-controls=" + atribut + "]").text();
            DateJSON.put(nume_titlu, "");
            if (!html.select("#" + atribut + " .panel-title").isEmpty()) { // Daca sunt submeniuri
                Elements nume_subcategorii = html.select("#" + atribut + " .panel-title");
                Elements tabele = html.select("#" + atribut + " div[role=tabpanel] table");
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
            Element tabel = html.select("div[id=" + atribut + "] table").first();
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
        return DateJSON;
    }
    
    private String[] tablistAttributeValues() {
        Elements ul = html.select("ul[role=tablist] li");
        String[] attributes = new String[ul.toArray().length];
        for (int i = 0; i < ul.toArray().length; i++)
        attributes[i] = ul.get(i).select("> a").attr("aria-controls");
        
        return attributes;
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
    
    
}
