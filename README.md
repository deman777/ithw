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