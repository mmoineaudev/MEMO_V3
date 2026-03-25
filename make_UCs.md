# Creating a checklist for active journaling from user input.

The user provided an input. The current task is to understand this input and the overall goal of the user : what does he want to create ? 

## How to create the checklist ? 

* In order to minimize efforts for achieving the user goal, you need to create a file called ```AGENTIC_RESOURCES/checklist.md``` that will contain a breakdown of tasks needed to achieve the user's goal describe in input.
* The format of this task checklist is by use cases, they can be high level or very technical. Unneeded items depending on the task context are to be ignored. Format is : 

```
# Use Case: <number> <the name should be the goal as a short active verb phrase>

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: 
* Scope: 
* Level: 
* Preconditions: 
* Success End Condition: 
* Failed End Condition: 
* Primary Actor: 
* Trigger: 

### MAIN SUCCESS SCENARIO

<put here the steps of the scenario from trigger to goal delivery, and any cleanup after>

<step #> <action description>

### EXTENSIONS

<put here there extensions, one at a time, each refering to the step of the main scenario>

<step altered> <condition> : <action or sub.use case>

<step altered> <condition> : <action or sub.use case>

### SUB-VARIATIONS

<put here the sub-variations that will cause eventual bifurcation in the scenario>

<step or variation # > <list of sub-variations>

<step or variation # > <list of sub-variations>

### RELATED INFORMATION (optional)

* Priority: <how critical to your system >
* Performance Target: <the amount of time this use case should take>
* Frequency: <how often it is expected to happen>
```

* Stop when the entire input of the user is properly represented in ```AGENTIC_RESOURCES/checklist.md```
* Success criteria of the creation of the checklist:
  * The produced checklist should be sufficient to create the entire solution expected by the user.
  * Each use case must have at least one concrete action item (implementation or test).
  * All user input features must map to at least one use case - cross-reference requirements to ensure complete coverage.
* At the end of the listing of all ordered use cases, add a section on top of the file stating : 
  * Overall goal of the user
  * Main architectural and technical guidelines
  * Scope definition of the solution
  * Cross-reference matrix: map each user input feature to its corresponding use case(s)
  * Add any details that would help an agent contextualize the current project efficiently.
