/**
 * Cellpose Detection script
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 */

// As this is the first step that will produce objects, we make sure that nothing else is on the currently open image.
clearAllObjects()

// Segment the whole image
createAllFullImageAnnotations( true )

// This is the model which was trained for detecting Organoids. It should be located in this project's 'models' folder
def modelName = "cellpose_residual_on_style_on_concatenation_off_train_2023_11_16_15_43_29.265325"
def pathModel = buildFilePath( PROJECT_BASE_DIR, "models", modelName )


// Run cellpose
def cellpose = Cellpose2D.builder( pathModel )
        .pixelSize( 1.3211 * 2 )    // Resolution for detection in um
        .channels( 'Brightfield' )  // Select detection channel(s)
        .cellprobThreshold(-3 )     // Threshold for the mask detection, defaults to 0.0
        .diameter( 30 )             // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .classify( "Small" )        // PathClass to give newly created objects
        .measureShape()             // Add shape measurements
        .measureIntensity()         // Add cell measurements (in all compartments)
        .simplify( 0 )              // Simplification 1.6 by default, set to 0 to get the cellpose masks as precisely as possible
        .build()

// Run detection for the selected objects
def imageData = getCurrentImageData()

def pathObjects = getAnnotationObjects() // To process all annotations. For working in batch mode

if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage( "Cellpose", "Please select a parent object!" )
    return
}

cellpose.detectObjects( imageData, pathObjects )

// Run the same model on a downsampled version of the data to detect larger objects

def detectionsSmall = getDetectionObjects()

def cellpose2 = Cellpose2D.builder( pathModel )
        .pixelSize( 1.3211 * 6 )    // Resolution for detection in um
        .channels( 'Brightfield' )  // Select detection channel(s)
        .cellprobThreshold( -3 )    // Threshold for the mask detection, defaults to 0.0
        .diameter( 30 )             // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .classify( "Large" )        // PathClass to give newly created objects
        .measureShape()             // Add shape measurements
        .measureIntensity()         // Add cell measurements (in all compartments)
        .simplify( 0 )              // Simplification 1.6 by default, set to 0 to get the cellpose masks as precisely as possible
        .build()


cellpose2.detectObjects(imageData, pathObjects)

def detectionsLarge = getDetectionObjects()

// In case there is an overlap in the results of the two models, remove the larger ones
def overlapLarge = detectionsLarge.findAll{ large-> detectionsSmall.any{ small -> large.getROI().contains( small.getROI().getCentroidX(), small.getROI().getCentroidY() ) } }

removeObjects( overlapLarge, false )

// Re-add the small objects as these were removed by the second Cellpose run
addObjects( detectionsSmall )

println "Cellpose detection done"

// Remove the full image annotation as we do not need it anymore
def rectangles = getAllObjects().findAll{ it.getROI() instanceof qupath.lib.roi.RectangleROI }
removeObjects( rectangles, true )

import qupath.ext.biop.cellpose.Cellpose2D