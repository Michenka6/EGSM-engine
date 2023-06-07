package Manager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.jexl3.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import java.io.FileWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GSM1 {
    private Document model;
    private Document infoModel;

    private ArrayList<String> allowedEvents;
    private ArrayList<String> externalEvents;
    private ArrayList<String> allEvents;


    public GSM1(String modelPath, String infoModelPath) {

        this.externalEvents = new ArrayList<>();
        this.allEvents = new ArrayList<>();
        this.allowedEvents = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.model = builder.parse(modelPath);

            this.infoModel = builder.newDocument();

            NodeList events = model.getDocumentElement().getElementsByTagName("ca:Event");
            for (int i = 0; i < events.getLength(); i++) {
                Element event = (Element) events.item(i);
                allowedEvents.add(event.getAttribute("id"));
            }

            Element rootElement = this.infoModel.createElement("infoModel");
            this.infoModel.appendChild(rootElement);

            NodeList elements = builder.parse(infoModelPath).getDocumentElement().getElementsByTagName("xs:element");
            for (int i = 0; i < elements.getLength(); i++) {
                Element e = (Element) elements.item(i);
                String name = e.getAttribute("name");

                Element element = this.infoModel.createElement(name);


                Element timestamp = this.infoModel.createElement("timestamp");
                timestamp.setTextContent("N/A");
                element.appendChild(timestamp);

                Element status = this.infoModel.createElement("status");
                status.setTextContent("N/A");
                element.appendChild(status);

                rootElement.appendChild(element);
            }
        } catch (Exception e) {
            System.out.println("ERROR: Can't load in the data!");
            ;
            System.out.println("Exception: " + e);
        }
    }

    public void init() {
        Element root = this.model.getDocumentElement();
        root.normalize();

        NodeList stages = root.getElementsByTagName("ca:Stage");
        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);
            refreshStage(stage);
        }

        NodeList milestones = root.getElementsByTagName("ca:Milestone");
        for (int i = 0; i < milestones.getLength(); i++) {
            Element m = (Element) milestones.item(i);
            m.setAttribute("status", "fresh");
        }

        NodeList events = root.getElementsByTagName("ca:Event");
        for (int i = 0; i < events.getLength(); i++) {
            Element event = (Element) events.item(i);
            this.allowedEvents.add(event.getAttribute("id"));
        }

        NodeList dfgs = getAllDFGs();
        for (int i = 0; i < dfgs.getLength(); i++) {
            Element d = (Element) dfgs.item(i);
            d.setAttribute("status", "on");
        }

        NodeList pfgs = getAllPFGs();
        for (int i = 0; i < pfgs.getLength(); i++) {
            Element p = (Element) pfgs.item(i);
            p.setAttribute("status", "on");
        }

        NodeList conditions = getAllConditions();
        for (int i = 0; i < conditions.getLength(); i++) {
            Element c = (Element) conditions.item(i);
            c.setAttribute("status", "off");
        }

        NodeList faultLoggers = getAllFaultLoggers();
        for (int i = 0; i < faultLoggers.getLength(); i++) {
            Element f = (Element) faultLoggers.item(i);
            f.setAttribute("status", "off");
        }

    }

    private void refreshStage(Element stage) {
        stage.setAttribute("status", "unOpened");
        stage.setAttribute("outcome", "regular");
        stage.setAttribute("compliance", "onTime");

        NodeList subs = stage.getElementsByTagName("ca:SubStage");
        for (int i = 0; i < subs.getLength(); i++) {
            Element sub = (Element) subs.item(i);
            sub.setAttribute("status", "unOpened");
            sub.setAttribute("outcome", "regular");
            sub.setAttribute("compliance", "onTime");
        }
    }

    public void triggerEvent(String event) {
        if (!isValidEvent(event)) {
            System.out.println("Invalid Event: " + event);
            return;
        }
        this.allEvents.add(event);

        NodeList stages = getAllStages();
        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);
            triggerStage(stage, event);
        }
    }

    private void pulse() {
        NodeList stages = getAllStages();
        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);
            triggerStage(stage, "");
        }
    }

    private void triggerStage(Element stage, String event) {
        switch (stage.getAttribute("status")) {
            case "unOpened":
                NodeList dfgs = stage.getElementsByTagName("ca:DataFlowGuard");
                for (int i = 0; i < dfgs.getLength(); i++) {
                    Element dfg = (Element) dfgs.item(i);

                    if (checkDFG(dfg, event)) {
                        Element dfgParent = (Element) dfg.getParentNode();
                        dfg.setAttribute("status", "off");
                        dfgParent.setAttribute("status", "open");

                        NodeList pfgs = dfgParent.getElementsByTagName("ca:ProcessFlowGuard");
                        for (int j = 0; j < pfgs.getLength(); j++) {
                            Element pfg = (Element) pfgs.item(j);
                            if (checkPFG(pfg)) {
                                pfg.setAttribute("status", "off");
                            }
                        }


                    }
                }
                break;
            case "open":
//                ArrayList<Element> opened = getAllOpenStagesAndSubStages();
//                for (Element s : opened) {
//                    if (checkFL(s, event)) {
//                        s.setAttribute("outcome", "faulty");
//                    }
//                }
//
//                NodeList children = stage.getChildNodes();
//                boolean b = true;
//                for (int i = 0; i < children.getLength(); i++) {
//                    if (!(children.item(i).getNodeType() == Node.ELEMENT_NODE)){
//                        continue;
//                    }
//                    Element g = (Element) children.item(i);
//                    if(g.getTagName().equals("ca:DataFlowGuard")){
//                        checkDFG(g, event);
//                        b = g.getAttribute("status").equals("off") && b;
//                    }
//                }
//
//                if (!b)
//                    break;
//
//                for (int i = 0; i < children.getLength(); i++) {
//                    if (!(children.item(i).getNodeType() == Node.ELEMENT_NODE)){
//                        continue;
//                    }
//                    Element g = (Element) children.item(i);
//                    if (g.getTagName().equals("ca:Milestone") && checkMilestone(g,event)){
//                        closeStage(stage);
//                    }
//                }

                break;
            case "closed":
                break;
        }

    }

    private boolean checkMilestone(Element milestone, String event) {
        if (!milestone.getAttribute("eventIds").equals(event))
            return false;

        milestone.setAttribute("status","achieved");
        return true;
    }

    private boolean checkFL(Element s, String event) {
        NodeList fls = s.getElementsByTagName("ca:FaultLogger");
        for (int i = 0; i < fls.getLength(); i++) {
            Element fl = (Element) fls.item(i);
            if (fl.getAttribute("eventIds").equals(event) && evaluateExpression(fl.getAttribute("expression"), fl.getAttribute("langugae"))) {
                fl.setAttribute("status", "on");
                return true;
            }
        }

        return false;
    }

    private ArrayList<Element> getAllOpenStagesAndSubStages() {
        ArrayList<Element> result = new ArrayList<>();

        NodeList stages = getAllStages();
        NodeList subs = getAllSubStages();

        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);
            if (stage.getAttribute("status").equals("open"))
                result.add(stage);
        }

        for (int i = 0; i < subs.getLength(); i++) {
            Element stage = (Element) subs.item(i);
            if (stage.getAttribute("status").equals("open"))
                result.add(stage);
        }
        return result;
    }

    private boolean checkPFG(Element pfg) {
        boolean b = evaluateExpression(pfg.getAttribute("expression"), pfg.getAttribute("language"));
        return b;
    }

    private boolean checkDFG(Element dfg, String event) {
        boolean e = dfg.getAttribute("eventIds").equals(event);
        boolean exp = evaluateExpression(dfg.getAttribute("expression"), dfg.getAttribute("language"));
        return e && exp;
    }

    public boolean evaluateExpression(String expression, String language) {
        JexlEngine jexlEngine = new JexlBuilder().create();
        JexlContext jexlContext = new MapContext();

        expression = replaceFunctionArguments(expression);

        String jexlExpression = replaceXPATH(expression);

        jexlContext.set("Model.GSM", this);

        try {
            JexlExpression e = jexlEngine.createExpression(jexlExpression);
            Object o = e.evaluate(jexlContext);
            return (boolean) o;
        } catch (JexlException e) {
            System.out.println("ERROR: " + e);
        }

        return false;
    }

    public String replaceXPATH(String expression) {
        if (!expression.contains("{")) {
            String result = expression.replaceAll("\\[([^]]*)\\]", "['$1']").replaceAll("\\[([^]]*)\\]", "$1").replaceAll("\\band\\b", "&&").replaceAll("\\bor\\b", "||").replaceAll("\\bnot\\b", "!");
            return result;
        }

        String xpathExpression = expression.substring(expression.indexOf('{') + 1, expression.indexOf('}')).replace("infoModel.", "");

        XPath xpath = XPathFactory.newInstance().newXPath();
        String xpathResult = null;
        try {
            xpathResult = (String) xpath.evaluate(xpathExpression, this.infoModel, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            System.out.println("ERROR: " + e);
        }

        String jexlExpression = expression.replace("{" + "infoModel." + xpathExpression + "}", "[" + xpathResult + "]");


        return replaceXPATH(jexlExpression);
    }

    public boolean isEventOccurring(String event) {
        String last = allEvents.get(allEvents.size() - 1);

        return last.equals(event);
    }

    public boolean isMilestoneAchieved(String mId) {
        NodeList milestones = getAllMilestones();
        for (int i = 0; i < milestones.getLength(); i++) {
            Element m = (Element) milestones.item(i);
            if (m.getAttribute("status").equals("achieved") && m.getAttribute("id").equals(mId))
                return true;
        }
        return false;
    }

    private void closeStage(Element stage) {
        NodeList subs = stage.getElementsByTagName("ca:SubStage");
        stage.setAttribute("status", "closed");

        for (int i = 0; i < subs.getLength(); i++) {
            Element sub = (Element) subs.item(i);
            sub.setAttribute("status", "closed");
        }
    }

    public void closeStage(String stageId) {
        NodeList stages = getAllStages();
        NodeList subs = getAllSubStages();

        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                closeStage(stage);
                return;
            }
        }

        for (int i = 0; i < subs.getLength(); i++) {
            Element stage = (Element) subs.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                closeStage(stage);
                return;
            }
        }
    }

    public boolean isStageOpen(String stageId) {
        NodeList stages = getAllStages();
        NodeList subs = getAllSubStages();

        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                return stage.getAttribute("status").equals("open");
            }
        }

        for (int i = 0; i < subs.getLength(); i++) {
            Element stage = (Element) subs.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                return stage.getAttribute("status").equals("open");
            }
        }

        return false;
    }

    public String getStageAttribute(String stageId, String attribute) {
        NodeList stages = getAllStages();
        NodeList subs = getAllSubStages();

        for (int i = 0; i < stages.getLength(); i++) {
            Element stage = (Element) stages.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                return stage.getAttribute(attribute);
            }
        }

        for (int i = 0; i < subs.getLength(); i++) {
            Element stage = (Element) subs.item(i);

            if (stage.getAttribute("id").equals(stageId)) {
                return stage.getAttribute(attribute);
            }
        }

        return "";
    }

    public void setInfoElement(String element, String value) {
        Element e = (Element) this.infoModel.getElementsByTagName(element).item(0);
        Element t = (Element) e.getElementsByTagName("timestamp").item(0);
        t.setTextContent(LocalTime.now().toString());

        Element s = (Element) e.getElementsByTagName("status").item(0);
        s.setTextContent(value);
    }

    public void triggerManyExternalEvents(ArrayList<String> events) {
        for (String e : events) {
            this.externalEvents.add(e);
            triggerEvent(e);
        }
    }

    private boolean isValidEvent(String s) {
        return allowedEvents.contains(s);
    }

    public Document getInfoModel() {
        return infoModel;
    }

    public Document getModel() {
        return model;
    }

    public NodeList getAllStages() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:Stage");
    }

    public NodeList getAllSubStages() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:SubStage");
    }

    public NodeList getAllMilestones() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:Milestone");
    }


    public NodeList getAllFaultLoggers() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:FaultLogger");
    }

    public NodeList getAllConditions() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:Condition");
    }

    public NodeList getAllPFGs() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:ProcessFlowGuard");
    }

    public NodeList getAllDFGs() {
        Element root = this.model.getDocumentElement();
        return root.getElementsByTagName("ca:DataFlowGuard");
    }

    public ArrayList<String> getAllowedEvents() {
        return allowedEvents;
    }

    public ArrayList<String> getExternalEvents() {
        return this.externalEvents;
    }

    public ArrayList<String> getAllEvents() {
        return this.allEvents;
    }


    public void writeModel(boolean b) {
        FileWriter fileWriter;
        try {
            String content = "N/A";
            if (b) {
                content = GSM1.documentToString(model);
            } else {
                content = GSM1.documentToString(infoModel);
            }

            fileWriter = new FileWriter("src/test/resources/data/out.xml");
            fileWriter.write(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String documentToString(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");  // Optional: Indent the XML output

            // Convert the Document object to a DOMSource
            DOMSource domSource = new DOMSource(document);

            // Create a StringWriter to hold the XML string
            java.io.StringWriter stringWriter = new java.io.StringWriter();

            // Perform the transformation to write the XML string to the StringWriter
            StreamResult streamResult = new StreamResult(stringWriter);
            transformer.transform(domSource, streamResult);

            // Get the XML string from the StringWriter
            return stringWriter.toString();
        } catch (Exception e) {
        }
        return "";
    }

    public static String replaceFunctionArguments(String expression) {
        String regex = "([a-zA-Z_$][a-zA-Z\\d_$]*)\\((.*?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String arguments = matcher.group(2);
            String modified = "'" + arguments + "'";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(functionName + "(" + modified + ")"));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


}