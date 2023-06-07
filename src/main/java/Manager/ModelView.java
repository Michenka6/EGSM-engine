package Manager;

import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ModelView extends JPanel {
    private JPanel stagePanel;
    private JTextField textField;

//    public ModelView(Model.GSM gsm) {
//        setLayout(new BorderLayout());
//
//        stagePanel = new JPanel();
//        stagePanel.setLayout(new BoxLayout(stagePanel, BoxLayout.Y_AXIS));
//        JScrollPane scrollPane = new JScrollPane(stagePanel);
//        add(scrollPane, BorderLayout.CENTER);
//
//        loadXML(gsm);
//    }

    public ModelView(GSM1 gsm) {
        setLayout(new BorderLayout());

        stagePanel = new JPanel();
        stagePanel.setLayout(new BoxLayout(stagePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(stagePanel);
        add(scrollPane, BorderLayout.CENTER);

        textField = new JTextField();
        JButton button = new JButton("Trigger");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newText = textField.getText();
                gsm.triggerEvent(newText);
                updateXML(gsm);
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(textField);
        inputPanel.add(button);
        add(inputPanel, BorderLayout.NORTH);

        loadXML(gsm);
    }

    private void updateXML(GSM1 gsm) {
        // Update the XML data using the newText
        // ...
        // Reload and redisplay the XML
        stagePanel.removeAll();
        loadXML(gsm);
        revalidate();
        repaint();
    }

    private void loadXML(GSM1 gsm) {

        // Get the root element
        Element rootElement = gsm.getModel().getDocumentElement();

        // Get the stages
        NodeList stageList = rootElement.getElementsByTagName("ca:Stage");

        // Iterate through the stages
        for (int i = 0; i < stageList.getLength(); i++) {
            Element stageElement = (Element) stageList.item(i);

            // Get the stage information
            String stageId = stageElement.getAttribute("id");
            String stageStatus = stageElement.getAttribute("status");
            String stageCompliance = stageElement.getAttribute("compliance");
            String stageOutcome = stageElement.getAttribute("outcome");

            // Create a panel for the stage
            JPanel stageInfoPanel = new JPanel();
            stageInfoPanel.setLayout(new BoxLayout(stageInfoPanel, BoxLayout.Y_AXIS));
            stageInfoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            // Add the stage information to the panel
            stageInfoPanel.add(new JLabel("Stage ID: " + stageId));
            stageInfoPanel.add(new JLabel("Status: " + stageStatus));
            stageInfoPanel.add(new JLabel("Compliance: " + stageCompliance));
            stageInfoPanel.add(new JLabel("Outcome: " + stageOutcome));

            // Add the DataFlowGuards to the panel
            NodeList dataFlowGuardList = stageElement.getElementsByTagName("ca:DataFlowGuard");
            for (int j = 0; j < dataFlowGuardList.getLength(); j++) {
                Element dataFlowGuardElement = (Element) dataFlowGuardList.item(j);
                String dataFlowGuardId = dataFlowGuardElement.getAttribute("id");
                String dataFlowGuardEventIds = dataFlowGuardElement.getAttribute("eventIds");
                String dataFlowGuardExpression = dataFlowGuardElement.getAttribute("expression");
                String dataFlowGuardStatus = dataFlowGuardElement.getAttribute("status");

                stageInfoPanel.add(new JLabel("DataFlowGuard ID: " + dataFlowGuardId));
                stageInfoPanel.add(new JLabel("Event IDs: " + dataFlowGuardEventIds));
                stageInfoPanel.add(new JLabel("Expression: " + dataFlowGuardExpression));
                stageInfoPanel.add(new JLabel("Status: " + dataFlowGuardStatus));
            }

            // Add the milestones to the panel
            NodeList milestoneList = stageElement.getElementsByTagName("ca:Milestone");
            for (int k = 0; k < milestoneList.getLength(); k++) {
                Element milestoneElement = (Element) milestoneList.item(k);
                String milestoneId = milestoneElement.getAttribute("id");
                String milestoneStatus = milestoneElement.getAttribute("status");

                stageInfoPanel.add(new JLabel("Milestone ID: " + milestoneId));
                stageInfoPanel.add(new JLabel("Status: " + milestoneStatus));
            }

            // Add the substages to the panel
            NodeList subStageList = stageElement.getElementsByTagName("ca:SubStage");
            for (int m = 0; m < subStageList.getLength(); m++) {
                Element subStageElement = (Element) subStageList.item(m);
                String subStageId = subStageElement.getAttribute("id");
                String subStageStatus = subStageElement.getAttribute("status");

                stageInfoPanel.add(new JLabel("SubStage ID: " + subStageId));
                stageInfoPanel.add(new JLabel("Status: " + subStageStatus));
            }

            // Add the ProcessFlowGuards to the panel
            NodeList processFlowGuardList = stageElement.getElementsByTagName("ca:ProcessFlowGuard");
            for (int n = 0; n < processFlowGuardList.getLength(); n++) {
                Element processFlowGuardElement = (Element) processFlowGuardList.item(n);
                String processFlowGuardExpression = processFlowGuardElement.getAttribute("expression");
                String processFlowGuardStatus = processFlowGuardElement.getAttribute("status");

                stageInfoPanel.add(new JLabel("ProcessFlowGuard Expression: " + processFlowGuardExpression));
                stageInfoPanel.add(new JLabel("Status: " + processFlowGuardStatus));
            }

            // Add the FaultLoggers to the panel
            NodeList faultLoggerList = stageElement.getElementsByTagName("ca:FaultLogger");
            for (int p = 0; p < faultLoggerList.getLength(); p++) {
                Element faultLoggerElement = (Element) faultLoggerList.item(p);
                String faultLoggerId = faultLoggerElement.getAttribute("id");
                String faultLoggerExpression = faultLoggerElement.getAttribute("expression");
                String faultLoggerStatus = faultLoggerElement.getAttribute("status");

                stageInfoPanel.add(new JLabel("FaultLogger ID: " + faultLoggerId));
                stageInfoPanel.add(new JLabel("Expression: " + faultLoggerExpression));
                stageInfoPanel.add(new JLabel("Status: " + faultLoggerStatus));
            }

            stagePanel.add(stageInfoPanel);
        }

    }

    private String formatXML(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return null;
    }
}
