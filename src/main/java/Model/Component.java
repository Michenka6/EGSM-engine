package Model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Component {
    @XmlAttribute
    private String id;
    @XmlElement(name = "InformationModel", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private InformationModel informationModel;
    @XmlElement(name = "GuardedStageModel", namespace = "http://siena.ibm" +
            ".com/model/CompositeApplication")
    private GuardedStageModel guardedStageModel;

    public GuardedStageModel getGuardedStageModel() {
        return guardedStageModel;
    }

    public void setGuardedStageModel(GuardedStageModel guardedStageModel) {
        this.guardedStageModel = guardedStageModel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InformationModel getInformationModel() {
        return informationModel;
    }

    public void setInformationModel(InformationModel informationModel) {
        this.informationModel = informationModel;
    }



    // Getters and setters
}