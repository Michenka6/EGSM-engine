package Model;

import Rework.GuardStatus;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;


@XmlAccessorType(XmlAccessType.FIELD)
public class DataFlowGuard {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String eventIds;
    @XmlAttribute
    private String expression;
    @XmlAttribute
    private String language;
    private GuardStatus status;

    public void refresh() {
        setStatus(GuardStatus.On);
    }

    public GuardStatus getStatus() {
        return status;
    }

    public void setStatus(GuardStatus status) {
        this.status = status;
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

    public String getEventIds() {
        return eventIds;
    }

    public void setEventIds(String eventIds) {
        this.eventIds = eventIds;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void updateExpression() {
        this.expression = GSM.replaceFunctionArguments(GSM.replaceXPATH(this.expression));
    }
}
