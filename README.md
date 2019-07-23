## Simple Game Webapp

**This project is still in draft mode**

The webapp is inspired by [Killzone server architecture](https://www.guerrilla-games.com/read/the-server-architecture-behind-killzone-shadow-fall)

and ready to be deployed on amazon ec2 server thanks to spring boot.

### What it does

For the moment the webapp simply starts an Hazelcast cluster and expose two resources *find-match* and *check-find-status*.

You can lunch multiple instance of it , Hazelcast will keep track of each of them creating the cluster.

It tryies to mimic Killzone's server behavior by keeping in a distribuited map which member host which warzone , 

and forwarding the  *find-match command*  to it via Hazelcast's IExecutorService. 

Then with *check-find-status* call is possible to check if the match has been found for the requested user

### TODO 

- Add tests

- Manage rest errors properly

- Automate the deploy on Ec2a

### How to build and deploy

To build the application you just need to run `mvn clean install`. After you will find inside the *target* folder the jar of the application 

and a folder named config which holds 3 files 

- application.properties configures active profile and webapp name

- application-dev.properties for local dev configures tomcat port the warzone name (aws can be ignored)

- application-test.properties for ec2 deployment  configures tomcat port, warzone name, aws access keys

If you want to deploy the app on ec2 or in any other server , you will need to load on it the jar and the config folder. 

To run the app simply run `java -jar applicationname.jar`.

### Deploy on Aws EC2 server

This shows how to deploy on ec2 using the same network flow of Killzone's servers.  

Create and lunch your preferred linux machine , be sure that you have Java >= 8 installed, otherwise simply apt/yum install it.
	![T2 micro instances](/asset/Ec2-1.PNG)

Both the instances need to be in the same security group otherwise hazelcast wont be able to put them in the same cluster.

You will need to create some inbound rules for the security group 

![security rules](/asset/Ec2-2-inbound.PNG)

- HTTP on port 80 and ssh on 22 rules are there by default 
- Custom TCP rule on port range 5701-5708 those are hazelcast cluster ports and needs to be opened.
- Custom TCP rule on port *tomcat port* , insert here your tomcat port defined in application-test.properties

Then you will need to create an *Application Load Balancer*.

![LB Created](/asset/Ec2-3.PNG)


It will face the internet on port 80 (for the moment HTTPS is not enabled on our web application) 

Assign the LB to the same security group of the linux machine , and finally register your linux machine under his balancing. Keep in mind that machines will need  to be running in order to be seen when registering them.


![Registered instance](/asset/Ec2-4.PNG)

This ends EC2 configuration , now you will need to connect to your instances via SSH (use your favourite SSH client) 

To get the files needed for the application i used aws S3 and a simple wget from the linux machine, but feel free to use any method.

**Remember to put your aws key in the application-test.properties**

Launch the application on both instance and you should see this logs

![Registered instance](/asset/hazel1.PNG)

![Registered instance](/asset/hazel2.PNG)


Which means that the applications are up and see each other as member of the same Hazelcast cluster.

If you then try to curl at `loadbalanceraddress/test/find-match` adding the needed parameter you will see that

if the request is dispatched to the wrong warzone, the task will be forwarded to the member who hold the correct one.

![Registered instance](/asset/hazel3.PNG)

Here webapp hosting warzoneB is receiving a request for warzoneA, in the logs of warzoneB

Then the findMatch command will be fully executed on the correct warzone , and thanks to hazelcast distributed map we can 

check the status of the matchfind from any webapp without forwarding the request.


