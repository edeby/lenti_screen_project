/**
 * Detections are transformed to annotations to simplify downstream processing
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 */
def allCystic = getAllObjects()

allCystic.each{  
   if( it instanceof qupath.lib.objects.PathDetectionObject ) {
       def object = PathObjects.createAnnotationObject(it.getROI(), it.getPathClass())
       addObject( object )
       removeObject( it, false )
   }
}