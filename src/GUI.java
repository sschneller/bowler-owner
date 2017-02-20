import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import components.JSwitchBox;
import net.miginfocom.swing.MigLayout;
import org.bson.Document;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.mongodb.client.model.Filters.eq;

public class GUI extends JFrame implements ActionListener {

    private final String USER_AGENT = "Mozilla/5.0";
    private JTextField jTextField;
    private JList<String> iplist;
    private JButton openMenu;
    private DefaultListModel model = new DefaultListModel();
    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private MongoDatabase originDB;
    private MongoCollection<Document> sequences, connections;

    public static void main(String[] args) {
        final GUI gui = new GUI();
        SwingUtilities.invokeLater(() -> gui.setVisible(true));
    }

    private GUI() {
        setTitle("Owner Station");
        setLayout(new MigLayout("", "[grow,fill][]", "[][grow,fill][]"));
        setSize(400, 200);
        add(jTextField = new JTextField(""), "growx");
        JButton send = new JButton("SEND");
        send.addActionListener(this);
        add(send, "wrap");
        connect();
        retrieveConnections();
        iplist = new JList<>(model);
        add(iplist, "span, wrap");
        openMenu = new JButton("OPEN CONTROLS");
        openMenu.addActionListener(this);
        add(openMenu, "span");
    }

    private void connect() {
        originDB = mongoClient.getDatabase("league");
        sequences = originDB.getCollection("sequences");
        connections = originDB.getCollection("connections");
    }

    private void retrieveConnections() {
        model.removeAllElements();
        for(Document d : connections.find()) {
            model.addElement("Lane " + d.getInteger("laneid").toString()/* + " - " + d.getString("ip")*/);
        }
    }

    private String getIp(int laneid) {
        return connections.find(eq("laneid", laneid)).first().getString("ip");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(((JButton)e.getSource()).getText().equals("SEND")) {
            System.out.println("Testing 1 - Send Http GET request");
            boolean inArray = false;
            for (int i = 0; i < model.getSize(); i++) {
                if (((String) model.getElementAt(i)).contains(jTextField.getText())) {
                    inArray = true;
                }
            }
            if (!inArray) {
                try {
                    sendGet(jTextField.getText());
                    Document doc = new Document("ip", jTextField.getText()).append("laneid", getSequence("laneid"));
                    connections.insertOne(doc);
                    incrementSequence("laneid");
                    retrieveConnections();
                    iplist.repaint();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        else {
            JFrame freme = new JFrame();
            freme.setVisible(true);
            freme.setTitle(getIp(Integer.parseInt(iplist.getSelectedValue().substring(5))));
            freme.setLayout(new MigLayout("", "[grow,fill][grow,fill]", "[grow,fill][][]"));
            freme.add(new JLabel("DATA"));
            freme.add(new JSwitchBox("Free", "League"), "growx, wrap");
            JTextField paramField = new JTextField();
            freme.add(paramField, "span, growx");
            JButton getButton = new JButton("GET EXAMPLE");
            getButton.addActionListener(e12 -> {
                try {
                    sendGet(freme.getTitle(), paramField.getText());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            freme.add(getButton);
            freme.add(new JButton("POST EXAMPLE"), "wrap");

            freme.pack();
        }
    }

    private int getSequence(String sequence) {
        for(Document d : sequences.find()) {
            if(d.getString("sequence").equals(sequence)) {
                return d.getInteger("value");
            }
        }
        return -1;
    }

    private void incrementSequence(String sequence) {
        sequences.updateOne(eq("sequence", sequence), new Document("$set", new Document("value", getSequence(sequence) + 1)));
    }

    private void sendGet(String s) throws Exception {
        String url = "http://" + s + ":4567/";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }

    private void sendGet(String s, String p) throws Exception {
        String url = "http://" + s + ":4567" + p;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
    }
}
