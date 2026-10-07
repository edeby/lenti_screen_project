/**
 * In case there are images with NO annotations or detections, make sure that it still has a line in the exported results
 * 
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 */
 
def emptyClass = getPathClass( "Empty" )

def emptyObjects = getAllObjects().findAll{ it.getPathClass() == emptyClass }

removeObjects( emptyObjects, false )

if ( getAllObjects().findAll{ it.getPathClass() }.size() == 1 ) {

    
    print "${getProjectEntry().getImageName()} has no annotations or detections"
    
    // Create empty annotation and detection so that this iamge shows up in the exported stats too
    def eAnnot = PathObjects.createAnnotationObject(ROIs.createEmptyROI(), emptyClass)
    def eDet = PathObjects.createDetectionObject(ROIs.createEmptyROI(), emptyClass)
    
    addObjects(eAnnot, eDet)
}