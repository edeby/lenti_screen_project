/**
 * If the manual annotations were done AFTER the thickness script
 * This script simply computes the necessary statistics
 * 
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 */
 
def px = getCurrentServer().getPixelCalibration().getAveragedPixelSizeMicrons()
def um = GeneralTools.micrometerSymbol()
def manualClass = getPathClass("Thickness: Manual")


def objects = getAnnotationObjects().findAll{ it.getPathClass() == getPathClass("cystic") }
//objects = getSelectedObjects()

// In case there are organoids with only manual lines, we need to give them their own ID that does not clash with previous measurements
def nextID = getDetectionObjects().max{ it.measurements["Organoid ID"] }.measurements["Organoid ID"] + 1

// Get manual lines
def manual = getAnnotationObjects().findAll{ it.getPathClass() == getPathClass("manual line") }

// Clear previous manual detections
def prevManual = getDetectionObjects().findAll{ it.getPathClass() == manualClass }
removeObjects( prevManual, false )

objects.eachWithIndex { o, idx ->

    def mainROI = o.getROI()
    
    def children = o.getChildObjects()
    if( children.size() > 0 ) {
        id = children[0].measurements['Organoid ID']
    } else {
        id = nextID++
    }
    
    // Check if any manual annotations were done
    def manualRegions = manual.findAll{ mainROI.contains( it.getROI().getCentroidX(), it.getROI().getCentroidY() ) }
    // Add them to the ROI
    println  manualRegions
    if (manualRegions.size() > 0 ) {
        def annotatedManual = manualRegions.collect{ PathObjects.createDetectionObject( it.getROI(), manualClass ) }
        annotatedManual.each{  
            it.measurements["Thickness $um"] = it.getROI().getLength() * px
            it.measurements["Organoid ID"] = id
        }
        o.addChildObjects( annotatedManual )
        o.measurements["Average Thickness: Manual $um"] = annotatedManual.sum{ it.measurements["Thickness $um"] } / annotatedManual.size()
            
    }
}