# About the project

I wrote the entire program in Java. Developing the Minions I used spring boot, a package for developing RESTful web services. To run the program, we first load the minion servers (can use 1 up to 10 minions), and then load the Master with the minions adress and port, and a file with the hashes needed to be solved (see instructions below).

I tried to design this program so that each Minions work will be clear and simple, so every time a Minion is called to calculate, he always gets 1 hash to calculate and he compares 10^8 hashes(changing the last 7 digits, which means the range of the possible passwords is split to 10).

The Master then crack each hash seperatly, sending "calculate" instructions to the Minions with their range to calculate using http requests. If we have less then 10 minions, or if we lost connection with 1 of the minions, minions that finished working on their range will start working on the left ranges.
In the end, the master produces a file with the results of the cracked passwords.

I believe that making this program more efficient and fast was possible by making the minions compare to more then 1 hash from the input file, but it would harm the principle of a fast, small and simple Minions handle by the master, so I decided to keep it that way.


# Instructions

prerequisites:
maven
Java 8

Running:

Minions:
- go to  ../passCracker/Minion/minions_boot_<LINUX \ WINDOWS>   The default of these files is loading 10 minions in ports 8090 - 8099. if you wish to deter the minions ports, open the file in a text editor and change ports (you can also delete some of the minions by simply deleting a row). the ports currently have to have consecutive port numbers.  
- run chmod +x minions_boot_<LINUX \ WINDOWS> 
- run minions_boot_<LINUX \ WINDOWS> to load minions

Master:
- place the hashes file in ../passCracker/Master/src
- compile the files using: javac MD5.java & javac Master.java & javac MinionHolder.java & javac MyCallable.java
- run master using: java Master http://localhost <num of ports> <first port> <input file.txt>
- example for running arguments:
	
for 5 minions with ports 8090 8091 8092 8093 8094 running locally, run the next command:

java Master http://localhost 5 8090 ./hashes.txt

- the results will appear in this folder as 'Cracked Password.txt'



