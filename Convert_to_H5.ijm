//ensures all operations are performed in the proper order
setBatchMode(true);

//directory containing the images. Use \\ on windows
dir = "C:\\Users\\Documents\\FIJI\\";

// Get a list of all  files in the directory
list = getFileList(dir);

//File type to convert to and suffix
newsuffix = ".h5";

h5_ending = "/Data";

//output directory
dir_out = "C:\\Users\\Documents\\output\\";


// Loop through all h5 files in the directory
for (i = 0; i < list.length; i++) {
	
	//Create full path
    filePath = dir + list[i];
    run("Bio-Formats Importer", "open=filePath");

    // Get the image title (needed for saving and chooping off the old file type)
    title_temp = getTitle();
    
    //removes any character in the old file type (image.tif > image)
    dot = indexOf(title_temp, ".");

	//checks if a . is found
	if (dot != -1)
    title = substring(title_temp, 0, dot);
    
    run("Export HDF5", "exportpath=[" + dir_out + title + newsuffix + "] " + "datasetname=[" + h5_ending + "] " + "compressionlevel=0");
    close();
    
}

//ends batch mode when the code is complete
setBatchMode(false);

