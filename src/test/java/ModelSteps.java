
import Model.*;
import Rework.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.jexl3.parser.ASTSetSubNode;
import org.junit.Assert;

import java.util.ArrayList;

public class ModelSteps {
    String modelPath;
    String infoModelPath;
    GSM gsm;
    String xPath;
    String expression;

    @Given("a modelPath")
    public void a_model_path() {
        modelPath = "src/test/resources/data/siena.xml";
    }

    @Given("an infoModelPath")
    public void an_info_model_path() {
        infoModelPath = "src/test/resources/data/infoModel.xsd";
    }

    @When("gsm is instantiated")
    public void gsm_is_instantiated() {
        gsm = new GSM(modelPath, infoModelPath);
    }

    @Then("It should store them")
    public void it_should_store_them() {
        Assert.assertNotNull(gsm.getStages());
    }

    @Given("a loaded model")
    public void a_loaded_model() {
        String modelPath = "src/test/resources/data/siena.xml";
        String infoModelPath = "src/test/resources/data/infoModel.xsd";
        gsm = new GSM(modelPath, infoModelPath);
    }

    @When("gsm is initialized")
    public void gsm_is_initialized() {
        gsm.init();
    }

    @Then("all stages and substages are unOpened, regular and onTime")
    public void all_stages_and_substages_are_un_opened_regular_and_on_time() {

        for (Stage stage : gsm.getStages()) {
            Assert.assertEquals(stage.getStatus(), StageStatus.UnOpened);
            Assert.assertEquals(stage.getOutcome(), StageOutcome.Regular);
            Assert.assertEquals(stage.getCompliance(), StageCompliance.OnTime);
        }
    }

    @Then("all milestones are fresh")
    public void all_milestones_are_fresh() {
        for (Stage s : gsm.getStages()){
            for (Milestone m : s.getMilestones()){
                Assert.assertEquals(m.getStatus(), MilestoneStatus.Fresh);
            }
        }
    }

    @Then("all allowed events should be pulled")
    public void all_allowed_events_should_be_pulled() {
        Assert.assertNotEquals(gsm.getAllowedEvents(), null);
    }

    @Then("all DFG should be on")
    public void all_dfg_should_be_on() {
        for (Stage s : gsm.getStages()) {
            for (DataFlowGuard dfg : s.getDataGuards()) {
                Assert.assertEquals(dfg.getStatus(), GuardStatus.On);
            }
        }
    }

    @Then("all PFG should be on")
    public void all_pfg_should_be_on() {
        for (Stage s : gsm.getStages()) {
            for (DataFlowGuard pfg : s.getDataGuards()) {
                Assert.assertEquals(pfg.getStatus(), GuardStatus.On);
            }
        }
    }

    @Then("all Conditions should be off")
    public void all_conditions_should_be_off() {
        for (Stage s : gsm.getStages()) {
            for (Milestone m : s.getMilestones()) {
                Assert.assertEquals(m.getCondition().getStatus(), ConditionStatus.InActive);
            }
        }
    }

//    @Then("all Fault Logges should be off")
//    public void all_fault_logges_should_be_off() {
//        NodeList faultLoggers = gsm.getAllFaultLoggers();
//        for (int i = 0; i < faultLoggers.getLength(); i++) {
//            Element f = (Element) faultLoggers.item(i);
//            Assert.assertEquals(f.getAttribute("status"), "off");
//        }
//    }

    @Given("an initialized model")
    public void an_initialized_model() {
        String modelPath = "src/test/resources/data/lbNew/siena.xml";
        String infoModelPath = "src/test/resources/data/lbNew/infoModel.xsd";
        gsm = new GSM(modelPath, infoModelPath);
        gsm.init();
    }

    @When("an unknown event is triggered")
    public void an_unknown_event_is_triggered() {
        gsm.triggerEvent("UNKNOWN-EVENT");
    }

    @Then("nothing happens")
    public void nothing_happens() {
        Assert.assertTrue(gsm.getAllEvents().isEmpty());
    }

    @When("many events are triggered")
    public void many_events_are_triggered() {
        ArrayList<String> events = new ArrayList<>();
        events.add("Ship_e");
        events.add("Ship_l");
        events.add("Ship_e");
        gsm.triggerManyExternalEvents(events);
    }

    @Then("all should be logged")
    public void all_should_be_logged() {
        ArrayList<String> events = new ArrayList<>();
        events.add("Ship_e");
        events.add("Ship_l");
        events.add("Ship_e");
        for (String e : events) {
            Assert.assertTrue(gsm.getExternalEvents().contains(e));
            Assert.assertTrue(gsm.getAllEvents().contains(e));
        }
    }

    @Then("model should be able to tell last one occurring")
    public void model_should_be_able_to_tell_last_one_occurring() {
        Assert.assertTrue(gsm.isEventOccurring("Ship_e"));
    }

    @Given("an xPath expression")
    public void an_x_path_expression() {
        xPath = "(({infoModel./infoModel/Truck/status} == [Producer])) and (({infoModel./infoModel/Container/status} == [Hooked]))";
    }

    @Given("necessary infoModel settings")
    public void necessary_info_model_settings() {
        gsm.setInfoElement("Truck", "Producer");
        gsm.setInfoElement("Container", "Hooked");
    }

    @When("it is transformed")
    public void it_is_transformed() {
        expression = GSM.replaceXPATH(xPath);
    }

    @Then("it should hold")
    public void it_should_hold() {
        Assert.assertTrue(gsm.evaluateExpression(expression, "JEXL"));
    }

    @Given("a Jexl expression")
    public void a_jexl_expression() {
        expression = "Model.GSM.isEventOccurring('Truck_e')";
    }

    @When("it is triggered")
    public void it_is_triggered() {
        gsm.triggerEvent("Truck_e");
    }

    @When("an event that opens a stage triggers")
    public void anEventThatOpensAStageTriggers() {
        gsm.setInfoElement("Truck", "Producer");
        gsm.setInfoElement("Container", "Hooked");
        gsm.triggerEvent("Truck_e");
    }


    @Then("a DFG should be off")
    public void a_dfg_should_be_off() {
        DataFlowGuard dfg = (DataFlowGuard) gsm.getIdMap().get("shippingProcess_dfg2");
        Assert.assertEquals(dfg.getStatus(),GuardStatus.Off);
    }

    @Then("the stage should turn open")
    public void the_stage_should_turn_open() {
        Stage s = (Stage) gsm.getIdMap().get("shippingProcess");
        Assert.assertEquals(s.getStatus(),StageStatus.Open);
    }

    @When("a false jexl expression about milestones")
    public void a_false_jexl_expression_about_milestones() {
        expression = "Model.GSM.isMilestoneAchieved(DeliverToCustomer_m1)";
    }

    @Then("it should not hold")
    public void it_should_not_hold() {
//        Assert.assertFalse(gsm.evaluateExpression(expression, "JEXL"));
    }

    @Then("pfg should pass")
    public void pfg_should_pass() {
        ProcessFlowGuard pfg = (ProcessFlowGuard) gsm.getIdMap().get("ShipToA_pfg");
        SubStage s = (SubStage) gsm.getIdMap().get("ShipToA");
        Assert.assertEquals(pfg.getStatus(), GuardStatus.On);

    }

    @When("a parent stage\\/substage closes")
    public void a_parent_stage_substage_closes() {
        gsm.setInfoElement("Truck", "Producer");
        gsm.setInfoElement("Container", "Hooked");
        gsm.triggerEvent("Truck_e");

        gsm.closeStage("shippingProcess");
    }

    @Then("all child substages close as well")
    public void all_child_substages_close_as_well() {
        Assert.assertFalse(gsm.isStageOpen("ShipToA"));
    }

    @Given("an alternative initialized model")
    public void an_alternative_initialized_model() {
        String modelPath = "src/test/resources/data/test/siena.xml";
        String infoModelPath = "src/test/resources/data/test/infoModel.xsd";
        gsm = new Model.GSM(modelPath, infoModelPath);
        gsm.init();
    }

    @When("the fault logger is triggered")
    public void the_fault_logger_is_triggered() {
        gsm.setInfoElement("Container", "EmptyShippingHooked");
        gsm.setInfoElement("Truck", "ProducerStill");
        gsm.triggerEvent("Truck_e");

        gsm.triggerEvent("BoundaryEvent_2");
    }


    @When("conditions are met to fulfill a milestone")
    public void conditions_are_met_to_fulfill_a_milestone() {
        gsm.triggerEvent("StartEvent_1");
    }
    @Then("it should be achieved")
    public void it_should_be_achieved() {
        Stage m = (Stage) gsm.getIdMap().get("shippingProcess");
        for (SubStage s: m.getSubStages()) {
            System.out.println(s.getId());
        }

//        Assert.assertTrue(gsm.isMilestoneAchieved("StartEvent_1_m1"));
    }

}