# pinterest
A Pinterest Clone and fast Repository

Simply get the project and test or build the project.
There are tasks in gradle task, but it is better to use IDE to run tests.

`git clone https://github.com/metao1/pinterest`

`./gradlew test`

`./gradlew build`

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

### Adding and defining our service data into our repository

#### Here a Restful service 
```java
repository.addSerivce(JSON_API_URL_ADDRESS
    , new RepositoryCallback<Model>() {
    @Override
    public void onDownloadFinished(String urlAddress,Model response) {
        //Maybe set data to Adapters
        // The Model can be any thing including List<Model> etc
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
repository.downloadBitmapIntoViewHolder(IMAGE_URL_ADDRESS
    , ImageViewHolder /* A ViewHolder*/);

 ...

class ImageViewHolder extends RecyclerView.ViewHolder implements RepositoryCallbackInterface<Bitmap>{
     @Override
       public void onDownloadFinished(String urlAddress, Bitmap bitmap) {
           imageView.setImageBitmap(bitmap);
           /**
            * To tell the adapter that we done with data
            */
           progressBar.setVisibility(View.GONE);
       }
   
       @Override
       public void onError(Throwable throwable) {
           this.errorUrl = errorUrl;   
           progressBar.setVisibility(View.GONE);
       }
   
       @Override
       public void onDownloadProgress(String urlAddress, double progress) {
           int intProgress = (int) (progress * 100.0 / (long) 100);
           if (intProgress < 1) {
               intProgress = intProgress + 1;
           }
           if (progressBar.getProgress() < intProgress) {
               progressBar.setProgress(intProgress);
           }
       }
}
```

#### Image can also download into ImageView directly

```java
repository.downloadBitmap(IMAGE_URL_ADDRESS, imageViewObject);
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