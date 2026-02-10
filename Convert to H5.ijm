//ensures all operations are performed in the proper order
setBatchMode(true);

//directory containing the images. Use \\ on windows
dir = "C:\\Users\\bfisler\\.openjfx\\Documents\\Depslice_test(delete)\\";

// Get a list of all  files in the directory
list = getFileList(dir);

//File type to convert to and suffix (ex. PNG and .png)
newsuffix = ".h5";

h5_ending = "/Data";

//output directory
dir_out = "C:\\Users\\bfisler\\.openjfx\\Documents\\pngs\\";


// Loop through all h5 files in the directory
for (i = 0; i < list.length; i++) {
	
	// Build full path
    filePath = dir + list[i];
    open(filePath);

    // Get the image title (needed for saving and chooping off the old file type)
    title_temp = getTitle();
    
    //removes any character in the old file type (image.tif > image)
    dot = indexOf(title_temp, ".");
	if (dot != -1)
    title = substring(title_temp, 0, dot);
    
    run("Export HDF5", "exportpath=[" + dir_out + title + newsuffix + "] " + "datasetname=[" + h5_ending + "] " + "compressionlevel=0");
    close();
    
}

//ends batch mode when the code is complete
setBatchMode(false);

