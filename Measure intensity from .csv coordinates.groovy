/*
Reads in a .csv file and uses that file's coordinates to plot circles in a time series image. 
The circles go through everytimepoint and their intensity measurements are collected and saved.

Any circle that is partially out of bounds will be cropped so that it only gets intensity measurements within the image.
Multiple circles with centers that are too close together will be removed so that only one circle is in that region.
*/

setImageType('FLUORESCENCE')

removeAllObjects()

//must use forward slashes for the path on windows
def filePath = "/path.csv"
def file = new File(filePath)

// Ensure the file exists before processing
if (!file.exists()) {
    println "Path not found."
    return
}


def lines = file.readLines()

//skip the first three rows since they contain headers
def dataLines = lines.drop(4)

def server = getCurrentServer()
def nTimepoints = server.nTimepoints()


// Ellipse dimensions in pixels
double width = 25.0
double height = 25.0


def pixelCalibration = server.getPixelCalibration()
double pixelsize = pixelCalibration.getPixelWidthMicrons()

// Minimum distance between ellipse centers (pixels)
double minDistance = 20.0/pixelsize


// Store all annotations before adding them to QuPath
def finalAnnotations = []

// Store the centers of accepted annotations
def acceptedCenters = []

int skippedAnnotations = 0

for (int t = 0; t < nTimepoints; t++) {

    detection_counter = 0

    // Z = 0, current timepoint = t
    def plane = ImagePlane.getPlane(0, t)

    // Reset accepted centers for each timepoint
    acceptedCenters.clear()

    dataLines.each { line ->

        if (!line.trim())
            return

        def columns = line.split(',', -1)

        // Read coordinates
        double centerX = columns[0].trim().toDouble()
        double centerY = columns[1].trim().toDouble()

        // Check distance from previously accepted annotations
        boolean tooClose = false

        for (def center : acceptedCenters) {

            double dx = centerX - center[0]
            double dy = centerY - center[1]

            double distance = Math.sqrt(dx*dx + dy*dy)

            if (distance < minDistance) {
                tooClose = true
                break
            }
        }

        // Skip annotation if it is too close to another
        if (tooClose) {
            skippedAnnotations++
            return
        }

        // Accept this annotation
        acceptedCenters << [centerX, centerY]

        // Convert center coordinates to top-left
        double x = centerX - width / 2.0
        double y = centerY - height / 2.0

        // Create ellipse ROI
        def ellipse = ROIs.createEllipseROI(
            x,
            y,
            width,
            height,
            plane
        )

        // Create annotation
        def annotation = PathObjects.createAnnotationObject(ellipse)

        annotation.setName(line.trim().take(17))

        finalAnnotations << annotation
    }
}

// Add all annotations to QuPath
getCurrentHierarchy().addObjects(finalAnnotations)


selectAnnotations()

//chops off any part of an annotations that it outside of the image
removeOrClipObjectsOutsideImage()

runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin',
    """{
        "pixelSizeMicrons": ${pixelsize},
        "region": "ROI",
        "tileSizeMicrons": 25.0,
        "channel1": false,
        "channel2": true,
        "doMean": true,
        "doStdDev": true,
        "doMinMax": true,
        "doMedian": true,
        "doHaralick": false,
        "haralickMin": "NaN",
        "haralickMax": "NaN",
        "haralickDistance": 1,
        "haralickBins": 32
    }"""
)

//extracts the name of the image so it can be used when saving the output statistics 
def imageName = GeneralTools.stripExtension(getCurrentImageData().getServer().getMetadata().getName())

//must use forward slashes for the path on windows and end with /
save_path = "/output/path/"

//saves the statistics to a csv 
saveAnnotationMeasurements(save_path+imageName+'.tsv')

//prints the number of unique detections
println("${finalAnnotations.size()/nTimepoints} detections found")

print("Done")

