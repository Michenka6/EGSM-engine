package Model;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "CompositeApplicationType", namespace = "http://siena.ibm.com/model/CompositeApplication")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompositeApplicationType {
    @XmlAttribute
    private String version;
    @XmlAttribute
    private String name;
    @XmlElement(name = "EventModel", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private EventModel eventModel;
    @XmlElement(name = "Component", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private Component component;

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEventModel(EventModel eventModel) {
        this.eventModel = eventModel;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public EventModel getEventModel() {
        return eventModel;
    }

    public Component getComponent() {
        return component;
    }



    // Getters and setters
}

