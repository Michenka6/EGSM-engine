package Model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class InformationModel {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String rootDataItemId;
    @XmlElement(name = "DataItem", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private DataItem dataItem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRootDataItemId() {
        return rootDataItemId;
    }

    public void setRootDataItemId(String rootDataItemId) {
        this.rootDataItemId = rootDataItemId;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }
// Getters and setters
}