<a href="https://android-arsenal.com/details/1/3581/" rel="Android Arsenal TagsEditText">![TagsEditText](https://img.shields.io/badge/Android%20Arsenal-TagsEditText-green.svg?style=true%29%5D%28https://android-arsenal.com/details/1/3581)

Welcome to TagEditView!
===================

Android EditText view for view tags 

## How to use ##


Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			maven { url "https://jitpack.io" }
		}
	}

Step 2. Add the dependency

	dependencies {
        compile 'com.github.mabbas007:TagsEditText:v0.90'
	}

Step 3. Add TagsEditText to your layout file

    <mabbas007.tagsedittext.TagsEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        TagsEditText:tagsTextColor="@android:color/white"
        TagsEditText:tagsBackgroundColor="@android:color/holo_green_light"
        TagsEditText:tagsCloseImage="@drawable/tag_close"/>
        
## Screen shots ##
![enter image description here](http://i.imgur.com/ZJYlsNL.png?3)