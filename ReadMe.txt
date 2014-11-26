IT WORKS!!!

How it works:

	#1 Next to the button label "Connect" there is a TextField.
	#2 You type your target IP/Name in there and press enter or click connect.
	#3 If the connection is sucsessful the name/IP of the target should apeare in the panel to the right.
	#4 To select a intended person to send data to the check the box next to his name.
	#5 Now in the bigger TextField you type the message you want to send and hit enter to send.

Extra info
***********

If you want the program to use a spesific port run it with the port in the command line
If you want it to generate log files add the suffix '-l' to the command line


Changelog - Build 0003 11/14
****************************

Revised ioManager allowing for more trustworthy transmission
Added encryption protocols, only PGP for now but it seems to work

Bugs - Build 0003
*****************

If the client is trying to connect to a client who has not yet hosted and then hosts the server port crashes