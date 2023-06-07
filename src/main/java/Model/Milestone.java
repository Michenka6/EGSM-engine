package Model;

import Rework.ConditionStatus;
import Rework.MilestoneStatus;
import jakarta
        .xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Milestone {
    @XmlAttribute
    private String eventIds;
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String name;
    @XmlElement(name = "Condition", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private Condition condition;
    private MilestoneStatus status;

    public void refresh() {
        setStatus(MilestoneStatus.Fresh);
        condition.setStatus(ConditionStatus.InActive);
    }

    public MilestoneStatus getStatus() {
        return status;
    }

    public void setStatus(MilestoneStatus status) {
        this.status = status;
    }
    // Getters and setters

    public String getEventIds() {
        return eventIds;
    }

    public void setEventIds(String eventIds) {
        this.eventIds = eventIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }


    public void updateExpression() {
        String exp = GSM.replaceFunctionArguments(GSM.replaceXPATH(condition.getExpression()));
        condition.setExpression(exp);
    }
}
