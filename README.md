# pinterest
A Pinterest Clone and fast Repository

Simply get the project and test or build the project.
There are tasks in gradle task, but it is better to use IDE to run tests.

`git clone https://github.com/metao1/pinterest`
`gradle test`
`gradle build`

The project consists a demo pinterest project, simply uses AsyncPinterestHandler library, included in this project.
The main purpose of the AsyncPinterestHandler is to load images or any file into repository. 
The AsyncPinterestHandler is resposible to store data in a repository, for sake of performance, it caches the data into memory.

# I didn't have time to complete all the tasks, but I intend to improve it

The AsyncPinterestHandler uses chunks (MAX number of chunks is configurable into codes- for now 4 chunks as default)
The Chunks use for parallel downloding big files from the net.


