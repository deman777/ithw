# Running
Run in command line:
```
chmod u+x ./sbt
chmod u+x ./sbt-dist/bin/sbt
```
Then
```
./sbt
```
sbt downloads project dependencies. The > prompt indicates sbt has started in interactive mode.
At the sbt prompt, enter
```
reStart
```  
sbt builds the project and runs application

Errors will be written to file
```
data/errors.txt
```
It contains results from previous run, if you do not want to wait 14 minutes.

# Architecture
`Main` - entry point class. It creates main actor `Master`
## Actors
* `Master` orchestrates all other specific actors and passes events between them
    * `Clock` heart of the system. It simulates time by `Tick` events.
    * `MainEmitter` gather events from `MovementsEmitter` and `TurbineStatusUpdatesEmitter` and emits them to `Master`. 
    * `Processors` receive all events and forward to `Turbines` or `People`
        * `Turbines` manages fleet of turbines creates them on demand and forwards events to correct `Turbine`
        * `Peaple` creates `Person` on demand and forwards events to dedicated ones
    * `Reminders` are used indirectly by turbines to schedule some reminders
    * `Logger` logs errors to a file
## Process
* Each emitter reads events from it's file, notifies MainEmitter when data is read. 
* When all emitters are ready MainEmitter asks Master to start whole process.
* Master asks Clock to start ticking.
* Clock ticks to `Master`
* Master forwards ticks to MainEmitter and Reminders
* MainEmitter reacts on Ticks
* Master forwards events from MainEmitter
* MainEmitter forwards ticks to it's children
* Child emitters send events to MainEmitter
* MainEmitter forwards then to Master
* Master forwards to Processors
    * Movement events to to both Turbines and People
    * TurbineStatusUpdate events go only to Turbines
* When Turbine or Person detects tom issues it sends along parents hierarchy and Master ErrorEvents to Logger
* Logger writes them to file

Also there is some tiny logging to console. To understand what is happening.    