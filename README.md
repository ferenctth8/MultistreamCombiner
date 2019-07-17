# MultistreamCombiner
This a possible implementation for the MultistreamCombiner application.

In order to run the app, its source code will need to be implemented into an IDE of the developer's choice.
The next step will consist in running the appllication server - i.e. the source code located inside the class MultistreamServer.
After starting this entity, it will be possible to launch the 2 clients located inside classes MultistreamClientA and MultistreamClientB.

In order to supply the input to this application, the user will need to type it via the input console where the client applications are 
running - these 2 entities were configured to accept input data coming on multiple console lines, that is the user will need to press Enter 
to continue providing data.

Once the user will types in "Done for now" (this can be introduced under any possible format based lower and uppercase letters as case
insensitivity has been incorporated in the processing part for facilitating such a feature) and presses Enter, this event will potentially 
mean that all the input lines supplied up to the given moment will be processed and sent over to the server. The server should respond back
with the current set of operation results existing on its side which are updated using the given input data.

One more important feature the user will need to pay attention to is the introduction of "Quit" keyword (again case insensitivity is built 
into the application to facilitate an easier processing). This keyword, once sent over to the server, will determine the given client to 
gracefully disconnect from the server and obtain the last operation results based on the last flow of input data supplied from its side.
