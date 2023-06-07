package Manager;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoModelView extends JPanel {
    private JTextField tagNameField;
    private JTextField statusField;
    private JButton button;

    public InfoModelView(GSM1 gsm) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create text fields
        tagNameField = new JTextField(20);
        statusField = new JTextField(20);

        // Create a panel for the text fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Tag Name: "));
        inputPanel.add(tagNameField);
        inputPanel.add(new JLabel("Timestamp: "));
        inputPanel.add(statusField);

        // Create a button
        button = new JButton("Trigger Function");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tagName = tagNameField.getText();
                String timestamp = statusField.getText();

                // Call your function with the provided arguments
                gsm.setInfoElement(tagName, timestamp);

                // Refresh the panel
                refreshPanel(gsm);
            }
        });

        // Create a panel for the button
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);

        // Add the input panel and button panel to the main panel
        add(inputPanel);
        add(buttonPanel);

        refreshPanel(gsm);
    }

    private void refreshPanel(GSM1 gsm) {
        removeAll();

        // Add the input panel to the main panel
        add(tagNameField.getParent());
        add(statusField.getParent());
        add(button);

        try {

            // Get the root element
            Element rootElement = gsm.getInfoModel().getDocumentElement();

            // Get the child elements under the root element
            NodeList nodeList = rootElement.getChildNodes();

            // Iterate through the child elements
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                // Check if the node is an element
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    // Extract the values from the XML
                    String tagName = element.getTagName();
                    String timestamp = element.getElementsByTagName("timestamp").item(0).getTextContent();
                    String status = element.getElementsByTagName("status").item(0).getTextContent();

                    // Create a custom component for each data item
                    JPanel itemPanel = new JPanel();
                    itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                    itemPanel.add(new JLabel("Tag Name: " + tagName));
                    itemPanel.add(new JLabel("Timestamp: " + timestamp));
                    itemPanel.add(new JLabel("Status: " + status));

                    add(itemPanel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        revalidate();
        repaint();
    }


}
