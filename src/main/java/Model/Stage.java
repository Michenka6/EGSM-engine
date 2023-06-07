package Model;

import Rework.MilestoneStatus;
import Rework.StageCompliance;
import Rework.StageOutcome;
import Rework.StageStatus;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.List;
import java.util.Map;


@XmlAccessorType(XmlAccessType.FIELD)
public class Stage {
    @XmlAttribute
    private String id;
    @XmlAttribute
    private String name;
    @XmlElement(name = "DataFlowGuard", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private List<DataFlowGuard> dataGuards;
    @XmlElement(name = "Milestone", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private List<Milestone> milestones;
    @XmlElement(name = "SubStage", namespace = "http://siena.ibm.com/model/CompositeApplication")
    private List<SubStage> subStages;
    private StageStatus status;
    private StageOutcome outcome;
    private StageCompliance compliance;

    public void close() {
        setStatus(StageStatus.Closed);

        if (subStages == null)
            return;

        for (SubStage s : subStages) {
            s.close();
        }
    }

    public void addToIdMap(Map<String, Object> idMap) {
        idMap.put(this.id, this);
        for (DataFlowGuard dfg : dataGuards) {
            dfg.updateExpression();
            idMap.put(dfg.getId(), dfg);
        }
        for (Milestone m : milestones) {
            m.updateExpression();
            idMap.put(m.getId(), m);
            idMap.put(m.getCondition().getId(), m.getCondition());
        }
        if (subStages == null)
            return;
        for (SubStage s : subStages) {
            s.addToIdMap(idMap);
        }
    }

    public void refresh() {
        setCompliance(StageCompliance.OnTime);
        setOutcome(StageOutcome.Regular);
        setStatus(StageStatus.UnOpened);

        for (Milestone m : milestones) {
            m.refresh();
        }

        for (DataFlowGuard dfg : dataGuards) {
            dfg.refresh();
        }

        for (SubStage s : subStages) {
            s.refresh();
        }
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

    public List<DataFlowGuard> getDataGuards() {
        return dataGuards;
    }

    public void setDataGuards(List<DataFlowGuard> dataGuards) {
        this.dataGuards = dataGuards;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<SubStage> getSubStages() {
        return subStages;
    }

    public void setSubStages(List<SubStage> subStages) {
        this.subStages = subStages;
    }

    public StageStatus getStatus() {
        return status;
    }

    public void setStatus(StageStatus status) {
        this.status = status;
    }

    public StageOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(StageOutcome outcome) {
        this.outcome = outcome;
    }

    public StageCompliance getCompliance() {
        return compliance;
    }

    public void setCompliance(StageCompliance compliance) {
        this.compliance = compliance;
    }
}
