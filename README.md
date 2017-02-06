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

# Usage

### Setting options for our Repository (RAM SIZE,SERVICE TYPE,etc) 
```java  
Repository<List<Model>> repository = new Repository<List<Model>>("ServiceRepo") {
    static final int RAM_SIZE = 4 * 1024 * 1024;//4MiB

    @Override
    public RepositoryType repositoryType() {
        return RepositoryType.JSON;//Repostiroy type (Restfull Webservice)
    }

    @Override
    public int ramSize() {
        return RAM_SIZE;
    }
};
```

The AsyncPinterestHandler uses chunks (MAX number of chunks is configurable into codes- for now 4 chunks as default)
The Chunks use for parallel downloading big files from the net.

### Adding a task to download the service data into our repository
```java
Repository<Bitmap> repository = new Repository<Bitmap>("ImageRepo") {
    static final int RAM_SIZE = 400 * 1024 * 1024;//400MiB

    @Override
    public RepositoryType repositoryType() {
        return RepositoryType.BITMAP;//Repository type
    }

    @Override
    public int ramSize() {
        return RAM_SIZE;
    }
};
repository.addDownload(JSON_API_URL_ADDRESS
    , new RepositoryCallback<List<Model>>() {
    @Override
    public void onDownloadFinished(String urlAddress, List<Model> response) {
        //Maybe set data to Adapters
    }
                    
    public void onError(Throwable error) {
        //Raise an error message
    }
                    
    public void onDownloadProgress(String urlAddress, double progress) {
        // Showing progress if the service supports
    }
});
```                
### Downloading each Image Separately after having them in  Adapter              
```java

repository.addDownload(IMAGE_URL_ADDRESS
    , new RepositoryCallback<Bitmap>() {
        @Override
        public void onDownloadFinished(String urlAddress, Bitmap imageBitmap) {
            // Load bitmap into our ImageView
        }
                    
        public void onError(Throwable error) {
            //Raise an error message
        }
                    
        public void onDownloadProgress(String urlAddress, double progress) {
            // Showing progress for each image 
        }
});
```

#### Repository Types for option:

Repository types could be either of the followings:  

```java 
public enum RepositoryType {
        JSON(1), // A Restfull JSON WebService
        XML(2),// A SOAP WebService
        NORMAL(3),//byte array as the output
        BITMAP(4),//Bitmap conversion of an Image
        STRING(5);//String conversion on an Object       
    }
```