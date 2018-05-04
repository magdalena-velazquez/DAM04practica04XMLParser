package mx.unam.aragon.fes.diplo.dam04practica04xmlparser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {

    private final String XML_FILE = "users.xml";
    private final String USER = "user";
    private final String NAME = "name";
    private final String DESIGNATION = "designation";
    private final String LOCATION = "location";
    String[] from = new String[]{NAME,DESIGNATION,LOCATION};
    int[] to = new int[]{R.id.name, R.id.designation,R.id.location};
    ArrayList<HashMap<String, String>> userList;
    HashMap<String,String> user;
    Button bDOM, bSax, bPull;
    ListView listView;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView);
        bDOM = (Button) findViewById(R.id.buttonDOM);
        bSax = (Button) findViewById(R.id.buttonSAX);
        bPull = (Button) findViewById(R.id.buttonPULL);
        listView = (ListView) findViewById(R.id.listView);



    }
    public void DomParserWork(View v){
        try{
            userList = new ArrayList<>();
            InputStream istream = getAssets().open(XML_FILE);
            DocumentBuilderFactory builderFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder =

            builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(istream);
            NodeList nList = doc.getElementsByTagName(USER);
            Log.i(XML_FILE, nList.getLength()+" Length");
            for(int i =0;i<nList.getLength();i++){
                if(nList.item(0).getNodeType() ==
                        Node.ELEMENT_NODE){
                    user = new HashMap<String, String>();
                    Element elm = (Element)nList.item(i);
                    Log.i(NAME + i, getNodeValue(NAME,elm));
                    user.put(NAME, getNodeValue(NAME,elm) + " [DOM]");
                            user.put(DESIGNATION, getNodeValue(DESIGNATION,elm));
                    user.put(LOCATION, getNodeValue(LOCATION,elm));
                    userList.add(user);
                }
            }
            ListAdapter adapter = new SimpleAdapter(
                    getApplicationContext(), userList,
                    R.layout.list_row,from, to);
            listView.setAdapter(adapter);
            tv.setText("DOM Parser");
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    protected String getNodeValue(String tag, Element element) {
    NodeList nodeList = element.getElementsByTagName(tag);
    Node node = nodeList.item(0);
 if(node!=null){
        if(node.hasChildNodes()){
            Node child = node.getFirstChild();
            while (child!=null){
                if(child.getNodeType() == Node.TEXT_NODE){
                    return child.getNodeValue();
                }
            }
        }
    }
 return "";
}

    public void SaxParserWork(View v){
        try{
            userList = new ArrayList<>();
            user = new HashMap<>();
            SAXParserFactory parserFactory =
                    SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            DefaultHandler handler = new DefaultHandler(){
                String currentValue = "";
                boolean currentElement = false;
                public void startElement(String uri, String
                        localName,String qName, Attributes attributes)
                        throws SAXException {
                    currentElement = true;
                    currentValue = "";
                    if(localName.equals(USER)){
                        user = new HashMap<>();
                    }
                }
                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                    currentElement = false;
                    if (localName.equalsIgnoreCase(NAME))
                        user.put(NAME, currentValue + " [SAX]");
                    else if (localName.equalsIgnoreCase(DESIGNATION))
                        user.put(DESIGNATION, currentValue);
                    else if (localName.equalsIgnoreCase(LOCATION))
                        user.put(LOCATION, currentValue);
                    else if (localName.equalsIgnoreCase(USER))
                        userList.add(user);
                }
                @Override
                public void characters(char[] ch, int start, int
                        length) throws SAXException {
                    if (currentElement) {
                        currentValue = currentValue + new String(ch,
                                start, length);
                    }
                }
            };
            InputStream istream = getAssets().open(XML_FILE);
            Log.i(XML_FILE, istream.toString());
            parser.parse(istream,handler);
            ListAdapter adapter = new SimpleAdapter(this, userList, R.layout.list_row,from, to);
            listView.setAdapter(adapter);
            tv.setText("SAX Parser");
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void PullParserWork(View v){
        try{
            userList = new ArrayList<>();
            user = new HashMap<>();
            InputStream istream = getAssets().open(XML_FILE);
            XmlPullParserFactory parserFactory =
                    XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput(istream,null);
            String tag = "" , text = "";
            int event = parser.getEventType();
            while (event!= XmlPullParser.END_DOCUMENT){
                tag = parser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        if(tag.equals(USER))
                            user = new HashMap<>();
                        break;
                    case XmlPullParser.TEXT:
                        text=parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (tag){
                            case NAME: user.put(NAME,text + " [PULL]");
                                break;
                            case DESIGNATION:
                                user.put(DESIGNATION,text);
                                break;
                            case LOCATION: user.put(LOCATION,text);
                                break;
                            case USER:
                                if(user!=null)
                                    userList.add(user);
                                break;
                        }
                        break;
                }
                event = parser.next();
            }
            ListAdapter adapter = new SimpleAdapter(
                    this, userList, R.layout.list_row,from, to);
            listView.setAdapter(adapter);
            tv.setText("PULL Parser");
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
