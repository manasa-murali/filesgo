# filesgo

### Repository to access files from public directories using MediaStore API

* ### Architecture followed - MVVM
* ### Language - Kotlin
* ### Libraries
    * HILT - Dependency Injection
    * [Coroutines - Flow](https://github.com/Kotlin/kotlinx.coroutines) - Reactive Approach & Multithreading
    * [Glide](https://github.com/bumptech/glide) - Image loading
    * [Ted Permissions](https://github.com/ParkSangGwon/TedPermission) - Runtime Permissions
    
### Preview

![Preview](https://github.com/manasa-murali/filesgo/blob/master/loadsortsearch.gif)

#### Walk-through

`IRepository#loadFilesFromStorage()` - Loads files from storage on `Fetch Files` - Button Click

`IRepository#writeToFile()` - Writes the search result as .txt file using MedisStore on `WRITE` button click
      <p>Does nothing when no results found or search not enabled</p>
    
### UI
* [FileSearchFragment](https://github.com/manasa-murali/filesgo/blob/master/app/src/main/java/com/example/filesgo/view/FileSearchFragment.kt) - List all files, search result and write to text option
* [DetailsFragment](https://github.com/manasa-murali/filesgo/blob/master/app/src/main/java/com/example/filesgo/view/DetailsFragment.kt) - Displays Additional details regarding the file. If it is an image/GIF , it loads the Preview
* Additional - Sort Option to order list Alphabetically, Chronologically and by Extension. Also maintains sort order while writing search results to file 
* Search option to Find occurences of file names for matching String on `Search` Icon click
      <p>Also displays files found count as Text View (inside the app) and Notification</p>
      <p>Highlights matching search string on the file name as Background Span</p>
      <p>If search string is empty, it does nothing. To replace search results with original list of files click on `Clear` icon or swipe to refresh list </p>
      
