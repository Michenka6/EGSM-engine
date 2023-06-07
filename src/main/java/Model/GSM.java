package Model;

import Model.*;

import Rework.GuardStatus;
import Rework.MilestoneStatus;
import Rework.StageCompliance;
import Rework.StageStatus;
import org.apache.commons.jexl3.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GSM {
    private List<Stage> stages;
    private List<String> allowedEvents;
    private Map<String, String> infoModel;
    private Map<String, Object> idMap;
    private List<String> externalEvents;
    private List<String> allEvents;
    private JexlContext jexlContext;
    private JexlEngine jexlEngine;

    public GSM(String caPath, String infoPath) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.infoModel = new HashMap<>();

            NodeList elements = builder.parse(infoPath).getDocumentElement().getElementsByTagName("xs:element");
            for (int i = 0; i < elements.getLength(); i++) {
                Element e = (Element) elements.item(i);
                infoModel.put(e.getAttribute("name"), "N/A");
            }

            File file = new File(caPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(CompositeApplicationType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            CompositeApplicationType ca = (CompositeApplicationType) unmarshaller.unmarshal(file);

            stages = ca.getComponent().getGuardedStageModel().getStages();
            allowedEvents = new ArrayList<>();
            for (Event e : ca.getEventModel().getEvents()) {
                allowedEvents.add(e.getId());
            }

            externalEvents = new ArrayList<>();
            allEvents = new ArrayList<>();
            idMap = new HashMap<>();

        } catch (Exception e) {
            System.out.println("Error: exception found - " + e);
        }
    }

    public void init() {
        for (Stage s : stages) {
            s.refresh();
            s.addToIdMap(idMap);
        }

        this.jexlEngine = new JexlBuilder().create();
        this.jexlContext = new MapContext();
    }

    private void trigger(String event) {
        if (!isValidEvent(event)) {
            return;
        }
        allEvents.add(event);

        for (Stage s : stages) {
            triggerStage(s, event);
        }
    }

    private void triggerStage(Stage s, String event) {
        switch (s.getStatus()) {
            case UnOpened:
                for (DataFlowGuard dfg : s.getDataGuards()) {
                    if (checkDFG(dfg, event)) {
                        s.setStatus(StageStatus.Open);
                    }
                }
                if (s.getStatus().equals(StageStatus.Open)) {
                    if (s.getSubStages() == null)
                        break;
                    for (SubStage sub : s.getSubStages()) {
                        triggerSubStage(sub, event);
                    }
                }
                break;
            case Open:
                boolean b = true;
                for (DataFlowGuard dfg : s.getDataGuards()) {
                    b = b && dfg.getStatus().equals(GuardStatus.Off);
                }
                if (b) {
                    for (Milestone m : s.getMilestones()) {
                        if (checkMilestone(m, event)) {
                            s.close();
                        }
                    }
                }
                break;
            case Closed:
                for (Milestone m : s.getMilestones()) {
                    if (checkCondition(m.getCondition(), event)) {
                        m.setStatus(MilestoneStatus.Invalidated);
                        s.refresh();
                    }
                }
                break;
        }
    }

    private boolean checkCondition(Condition condition, String event) {
        return evaluateExpression(condition.getExpression(), event);
    }

    private boolean checkMilestone(Milestone milestone, String event) {
        if (!milestone.getEventIds().equals(event))
            return false;

        milestone.setStatus(MilestoneStatus.Achieved);
        return true;
    }

    private void triggerSubStage(SubStage s, String event) {
        if (s == null)
            return;

        switch (s.getStatus()) {
            case UnOpened:
                if (s.getDataGuards() == null)
                    return;
                for (DataFlowGuard dfg : s.getDataGuards()) {
                    if (checkDFG(dfg, event)) {
                        s.setStatus(StageStatus.Open);
                    }
                }
                for (ProcessFlowGuard pfg : s.getProcessGuards()) {
                    if (checkPFG(pfg)) {
                        pfg.setStatus(GuardStatus.Off);
                    } else {
                        s.setCompliance(StageCompliance.OutOfOrder);
                    }
                }

                if (s.getSubStages() == null)
                    break;
                if (s.getStatus().equals(StageStatus.Open)) {
                    for (SubStage sub : s.getSubStages()) {
                        triggerSubStage(sub, event);
                    }
                }
                break;
            case Open:
                break;
            case Closed:
                break;
        }
    }

    public void closeStage(String id) {
        Stage s = (Stage) idMap.get(id);
        if (s == null)
            return;

        s.close();
    }

    public boolean isStageOpen(String id) {
        Stage s = (Stage) idMap.get(id);
        if (s != null) {
            return s.getStatus().equals(StageStatus.Open);
        }

        SubStage sub = (SubStage) idMap.get(id);
        if (sub == null)
            return false;
        return sub.getStatus().equals(StageStatus.Open);
    }

    public boolean checkDFG(DataFlowGuard dfg, String event) {
        boolean e = dfg.getEventIds().equals(event);
        boolean exp = evaluateExpression(dfg.getExpression(), dfg.getLanguage());
        if (e && exp) {
            dfg.setStatus(GuardStatus.Off);
            return true;
        }
        return false;
    }

    private boolean checkPFG(ProcessFlowGuard pfg) {
        boolean b = evaluateExpression(pfg.getExpression(), pfg.getLanguage());
        return b;
    }


    public void triggerEvent(String event) {
        externalEvents.add(event);
        trigger(event);
    }

    public void triggerManyExternalEvents(ArrayList<String> events) {
        for (String e : events) {
            this.externalEvents.add(e);
            trigger(e);
        }
    }

    public boolean isMilestoneAchieved(String id) {
        Milestone m = (Milestone) idMap.get(id);
        return m.getStatus().equals(MilestoneStatus.Achieved);
    }

    public boolean isEventOccurring(String event) {
        String last = externalEvents.get(allEvents.size() - 1);

        return last.equals(event);
    }

    public void setInfoElement(String element, String value) {
        infoModel.put(element, value);
    }

    public boolean evaluateExpression(String expression, String language) {
        this.jexlContext.set("Model.GSM", this);

        for (String key : infoModel.keySet()) {
            String regex = "\\b" + key + "\\b(?![a-zA-Z0-9_])";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(expression);
            expression = matcher.replaceAll("'" +infoModel.get(key) + "'");

//            System.out.println(key + "  " + infoModel.get(key));
//            System.out.println(expression);
        }

        try {
            JexlExpression e = jexlEngine.createExpression(expression);
            Object o = e.evaluate(this.jexlContext);
            return (boolean) o;
        } catch (JexlException e) {
            System.out.println("ERROR: " + e);
        }

        return false;
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

    public static String replaceXPATH(String input) {
        input = input.replaceAll("\\[(.*?)\\]", "'$1'");
        input = input.replaceAll("and", "&&").replaceAll("or", "||").replaceAll("not", "!");

        String startMarker = "{infoModel./infoModel/";
        String endMarker = "/status}";
        StringBuilder output = new StringBuilder(input);
        int startIndex = output.indexOf(startMarker);

        while (startIndex != -1) {
            int endIndex = output.indexOf(endMarker, startIndex + startMarker.length());

            if (endIndex != -1) {
                String extractedString = output.substring(startIndex + startMarker.length(), endIndex);
                output.replace(startIndex, endIndex + endMarker.length(),
                        " " + extractedString + " ");
            }

            startIndex = output.indexOf(startMarker, startIndex + 1);
        }

        return output.toString();
    }

    private boolean isValidEvent(String s) {
        return allowedEvents.contains(s);
    }

    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    public void setAllowedEvents(List<String> allowedEvents) {
        this.allowedEvents = allowedEvents;
    }

    public Map<String, Object> getIdMap() {
        return idMap;
    }

    public void setIdMap(Map<String, Object> idMap) {
        this.idMap = idMap;
    }

    public List<String> getExternalEvents() {
        return externalEvents;
    }

    public void setExternalEvents(List<String> externalEvents) {
        this.externalEvents = externalEvents;
    }

    public List<String> getAllEvents() {
        return allEvents;
    }

    public void setAllEvents(List<String> allEvents) {
        this.allEvents = allEvents;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public List<String> getAllowedEvents() {
        return allowedEvents;
    }

}
