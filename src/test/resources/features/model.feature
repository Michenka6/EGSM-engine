Feature: EGSM engine

Scenario: Load models in GSM
  Given a modelPath
  And an infoModelPath
  When gsm is instantiated
  Then It should store them

Scenario: Initialize a GSM
  Given a loaded model
  When gsm is initialized
  Then all stages and substages are unOpened, regular and onTime
  And all milestones are fresh
  And all allowed events should be pulled
  And all DFG should be on
  And all PFG should be on
  And all Conditions should be off

Scenario: Trigger an unknown event
  Given an initialized model
  When an unknown event is triggered
  Then nothing happens

Scenario: Triggering many recognized events
  Given an initialized model
  When many events are triggered
  Then all should be logged

Scenario: Observing an event occurring
  Given an initialized model
  When many events are triggered
  Then model should be able to tell last one occurring

Scenario: Making XPath work with java
  Given an initialized model
  And an xPath expression
  And necessary infoModel settings
  When it is transformed
  Then it should hold

Scenario: Checking JEXL isEventOccurring
  Given an initialized model
  And a Jexl expression
  When it is triggered
  Then it should hold

Scenario: Open a stage
  Given an initialized model
  When an event that opens a stage triggers
  Then a DFG should be off
  And the stage should turn open

Scenario: Jexl check if milestone is achieved
  Given an initialized model
  When a false jexl expression about milestones
  Then it should not hold

Scenario: Check PFG triggering
  Given an initialized model
  When an event that opens a stage triggers
  Then pfg should pass

Scenario: Close parent stage, substages should also close
  Given an initialized model
  When a parent stage/substage closes
  Then all child substages close as well

Scenario: Achieve milestone
  Given an alternative initialized model
  Given an initialized model
  When conditions are met to fulfill a milestone
  Then it should be achieved