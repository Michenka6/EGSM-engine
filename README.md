# EGSM-engine

GSM class in the model package is the main manager that should be instantiated to interat with the engine.
You need to create it with filepaths to EGSM model and infoModel
Then initialize it
To trigger events use the trigger function


```
GSM gsm = new GSM("pathToModel", "pathToInfoModel");
gsm.init();
gsm.trigger("Truck_e");
```
