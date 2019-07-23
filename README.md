# MultistreamCombiner
This a possible implementation for the MultistreamCombiner application.

In order to run the app, its source code will need to be imported into an IDE of the developer's choice.
The next step will consist in running the appllication server - i.e. the source code located inside the class MultistreamServer.
After starting this entity, it will be possible to launch the 2 clients located inside classes MultistreamClientA and MultistreamClientB.

In order to supply input to this application, the user will need to type it via the input console where the client applications are 
running - these 2 entities were configured to accept input data coming on multiple console lines, that is the user will need to press Enter 
after the introduction of each particular data line in order to continue providing the input data. The core class corresponding to the input data coming from the client side is called CoreData.

Once the user will type in "Done for now" (this can be introduced under any possible format based lower and uppercase letters as case
insensitivity has been incorporated into the processing part for facilitating the detection of this particular feature) and presses Enter, this event will potentially mean that all the input lines supplied up to the given moment will be processed and sent over to the server under the XML format provided in the requirements of the application (https://gist.github.com/m0mus/ba6c5419278239a19175445787420736). The server should respond back with the current set of operation results existing on its side which are updated using the given input data - this means that the response will be the content of the storage repo for each particular time when client input arrived (marked of course under JSON format as given in the requirements). Some extra processing of the input supplied by the client to the server is also performed at the level of the client - here I incorporated some extra validation steps for the elimination of non-numerical input (these input lines will also be processed, but will not be sent over to the server as they are considered invalid). 

One more important feature the user will need to pay attention to is the introduction of "Quit" keyword (again case insensitivity is built 
into the application to facilitate an easier processing). This keyword marks an exception from the client-filtering mechanism described above as it is allowed to be sent over to the server - its equivalent core class is called QuitCommand. Once sent over to the server, it will determine the given client to gracefully disconnect from the server and obtain the last operation results based on the last flow of input data supplied from its side.

The application has been built using the Java NIO library. 
