<a href="https://android-arsenal.com/details/1/3581/" rel="Android Arsenal TagsEditText">![TagsEditText](https://img.shields.io/badge/Android%20Arsenal-TagsEditText-green.svg?style=true%29%5D%28https://android-arsenal.com/details/1/3581)
Welcome to TagEditView!
===================

Android EditText view for view tags 

## How to use ##


Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```
Step 2. Add the dependency
```groovy
dependencies {
	compile 'com.github.mabbas007:TagsEditText:v1.0.3'
}
```
Step 3. Add TagsEditText to your layout file
```xml
<mabbas007.tagsedittext.TagsEditText
        android:id="@+id/tagsEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        TagsEditText:allowSpaceInTag="true"
        TagsEditText:tagsCloseImageRight="@drawable/tag_close"
        TagsEditText:tagsBackground="@drawable/square"
        TagsEditText:tagsCloseImageLeft="@drawable/dot"
        TagsEditText:tagsTextColor="@color/blackOlive"
        TagsEditText:tagsTextSize="@dimen/defaultTagsTextSize"
        TagsEditText:tagsCloseImagePadding="@dimen/defaultTagsCloseImagePadding"/>
```  
        
## Screen shots ##
![enter image description here](http://i.imgur.com/ZJYlsNL.png?3)
