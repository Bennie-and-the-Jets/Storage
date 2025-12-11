//ensures all operations are performed in the proper order
setBatchMode(true);

//specifies whcih h5 file to use
h5_suffix = "/Data"

//path to ilastik project
ilastik_project = "C:\\Users\\.openjfx\\Documents\\Xi_ilastik.ilp" 

//directory containing the images
dir = "C:\\Users\\.openjfx\\Documents\\Ilastik\\"  

// Get a list of all  files in the directory
list = getFileList(dir);

// Loop through all h5 files in the directory
for (i = 0; i < list.length; i++) {
	if (endsWith(list[i], ".h5") || endsWith(list[i], ".hdf5")) {
	
		//save the file name and path
	    filePath = dir + list[i];
	   
		run("Import HDF5", "select=[" + filePath + "] datasetname="+ h5_suffix +" axisorder=tzyxc");
		id_hdf5 = getImageID();
	
		//runs the ilastik pixel classifier
		run("Run Pixel Classification Prediction", "projectfilename="+ ilastik_project +" inputimage="+ list[i] +" pixelclassificationtype=Probabilities");
	
		//saves the image as a tiff
		saveAs("Tiff", "C:\\Users\\.openjfx\\Documents\\Ilastik\\results\\" + list[i] + ".tif");
		id_tiff = getImageID();
	
	    // Close the hdf5 and tiff image after processing
	    selectImage(id_hdf5);
        close();
        selectImage(id_tiff);
        close();
	}
}

//ends batch mode when the code is complete
setBatchMode(false);

