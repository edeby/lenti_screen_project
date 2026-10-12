/* = CODE DESCRIPTION =
 * This code traces lines across the edge of blob-like annotations in order to estimate their thickness
 * 2 methods for auto-thickness estimation
 * Local minima detection
 * Gaussian fitting and full width at half maximum estimation
 *
 * Both protocols need a line to be drawn across the edge of the organoid. 
 * In order to estimate the edge of the organoid, we use the cellpose detected annotation. 
 * From that coordinate we create a line that starts at the center of the organoid and traverses its edge. 
 * For local minima detection, the pixel intensities across the line are computed. 
 * In the case of cystic organoids, there should be two distinguishable dark peaks: when the line passes 
 * through the start of the ridge and when it exits the ridge. If we manage to detect the two peaks, 
 * the script then calculates the peak to peak distance
 *
 * In the case that the ridge is too thin or does not present clear minima, 
 * an alternative solution involving gaussian fitting can be used. 
 * The intensities of the line profile crossing the ridge can be fitted to a gaussian function. 
 * The Full Width at Half Maximum (FWHM) can then be used to get an estimate of the ridge thickness. 
 * This is done using ImageJ’s built in Curve Fitter tool. 
 *
 *
 * == INPUTS ==
 * An image with "cystic" type annotations
 *
 * == OUTPUTS ==
 * Line Detections inside each annotation with their length matching the detected thickness
 *
 *
 * = AUTHOR INFORMATION =
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 *
 * = COPYRIGHT =
 * (c) All rights reserved. ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE, Switzerland, BioImaging And Optics Platform (BIOP), 2024
 *
 * Licensed under the BSD-3-Clause License:
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *    in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 // Total lines to draw around the center of the organoid
def nLines = 18

// Local Minima detector tolerance
def peakTolerance = 5

// Fraction of max size of the object away from center of organoid to start drawing line (start radius)
def offsetFrac = 0.3

// In case there is one peak and we attempt a gaussian fit, reject gaussian fits below this R2 value
def r2Thr = 0.7


// START OF SCRIPT

def objects = getAnnotationObjects().findAll{ it.getPathClass() == getPathClass("cystic") }

clearDetections()

// Get manual lines
def manual = getAnnotationObjects().findAll{ it.getPathClass() == getPathClass("manual line") }

def lineThickness = 3
def downsample = 1.0
def px = getCurrentServer().getPixelCalibration().getAveragedPixelSizeMicrons()
def um = GeneralTools.micrometerSymbol()

// Get Image in ImageJ but expand ROI
def rm = RoiManager.getInstance() ?: new RoiManager()
rm.reset()
IJ.run("Close All")
//Line.setWidth(2)

objects.eachWithIndex { o, idx ->

    def mainROI = o.getROI()
    
    // Check if any manual annotations were done
    def manualRegions = manual.findAll{ mainROI.contains( it.getROI().getCentroidX(), it.getROI().getCentroidY() ) }
    // Add them to the ROI
    if (manualRegions.size() > 0 ) {
        def annotatedManual = manualRegions.collect{ PathObjects.createDetectionObject( it.getROI(), getPathClass("Thickness: Manual") ) }
        annotatedManual.each{ it.measurements["Thickness $um"] = it.getROI().getLength() * px }
        o.addChildObjects( annotatedManual )
    }
    
    // Make it larger so that we get a large enough image into ImageJ
    def expandedROI = GeometryTools.geometryToROI( mainROI.getGeometry().buffer(10), o.getROI().getImagePlane() )
    
    def region = RegionRequest.createInstance( getCurrentServerPath(), downsample, expandedROI )
    
    def image = IJTools.convertToImagePlus( getCurrentServer(), region )
    
    def originalRoi = IJTools.convertToIJRoi( mainROI, image )
    
    image.getImage().show()
    image.getImage().setRoi( originalRoi )

    // Get distance map to find out edges
    def distanceIp = image.getImage().createRoiMask()
    
    def distanceImp = new ImagePlus( "EDM", new EDM().make16bitEDM( distanceIp ) )
         
    // Get ROI Center to draw lines from   
    def  w = originalRoi.getBounds().width
    def  h = originalRoi.getBounds().height
    def xc = originalRoi.getBounds().x + w/2
    def yc = originalRoi.getBounds().y + h/2

    def offset = offsetFrac * ( w + h ) / 2
    
    // Draw N lines
    for( i = 0; i < nLines; i++ ) {
        def angle = i / nLines * 2 * Math.PI
        
        // Use distance map to find center (Where it goes to 0)
        def edgePosition = getEdgePosition( distanceImp, xc, yc, angle, lineThickness )

        def line = makeLineRoi( xc, yc, angle, offset, edgePosition + 2, lineThickness )
        rm.addRoi( line )
        
        // Get line profile
        line.setImage(image.getImage())
        def vals = line.getPixels()

        // Find the minima crossing
        def peakPositions = MaximumFinder.findMinima( vals, peakTolerance, true ) as List
        
        def thicknessLine = makeLineRoi(xc, yc, angle, edgePosition-2.5 , edgePosition-1.5, lineThickness )
        def mode = "Undetected"
        Map<String, Double> measurements = new LinkedHashMap<String, Double>()
        measurements.put( "Thickness $um", 0 )
        // Add ID of parent to make sure that we have a way to group them by organoid
        measurements.put( "Organoid ID", idx+1 )
            
        
        if ( peakPositions.size() > 1 ) {
            // The peaks should be ordered by index, the first is always the one closest to the edge
            def sortedPeaks = peakPositions.toSorted().reverse().take(2)
            
            
            thicknessLine = makeLineRoi(xc, yc, angle, sortedPeaks[0] + offset , sortedPeaks[1]+ offset, lineThickness )
            mode = "Peaks"
            measurements.put( "Thickness $um", ( sortedPeaks[0] - sortedPeaks[1]) * px )
            measurements.put( "Peak 1 value", vals[sortedPeaks[0]] )
            measurements.put( "Peak 2 value", vals[sortedPeaks[1]] )
            
        } else {
            if( peakPositions.size() == 1 ) {
                //Gaussian fitting beacuse we only found one peak
                def fwhm = getFWHM( vals )
                if( fwhm.r2 > r2Thr && (fwhm.fwhm < Math.max( distanceImp.getWidth(), distanceImp.getHeight() )) ) {
                    mode = "Gaussian fit"
                    thicknessLine = makeLineRoi(xc, yc, angle, fwhm.center + fwhm.fwhm/2 + offset , fwhm.center - fwhm.fwhm/2 + offset, lineThickness )
                    measurements.put( "Thickness $um", fwhm.fwhm * px )
                    measurements.put( "Center position", fwhm.center )
                    measurements.put( "R^2", fwhm.r2 )        
                } else {
                   // neither worked
                   mode = "Failed gaussian"
                }
            }
        }
        
        // Draw a line and color it based on the method
        def qpLine = IJTools.convertToDetection( thicknessLine, downsample, image.getImage() )
        qpLine.setPathClass( getPathClass( "Thickness: " + mode ) )
        measurements.each{ k, v -> qpLine.measurements.put( k,v ) }
        o.addChildObject( qpLine )
    }
    
    // Get stats for each category into the current object
    def children = o.getChildObjects()
    def pathClasses = children.collect{ it.getPathClass()}.toUnique()
    
    
    pathClasses.each{ pcls ->
        def subChildren = children.findAll{ it.getPathClass() == pcls }
        o.measurements["Average $pcls $um"] = subChildren.sum{ it.measurements["Thickness $um"] } / subChildren.size()
    }

    // Cleanup
    distanceImp.close()
    image.getImage().close()
}

fireHierarchyUpdate()

// Looking for where the edge of the ROI is with respect to the center of the ROI
def getEdgePosition( def distanceImp, def x, def y, def angle, def lineThickness ) {
    
    def line = makeLineRoi( x, y, angle, 0, Math.max( distanceImp.getWidth(), distanceImp.getHeight() ) / 1.3, lineThickness ) 
    line.setImage( distanceImp )
    def vals = line.getPixels() as ArrayList
    
    println "" + [vals, vals.getIndices()].transpose().find{ v, i -> v == 0 }
    return [vals, vals.getIndices()].transpose().find{ v, i -> v == 0 }[1]
    
}

// Make a line from a center, an angle, a starting distance from the center and and end distance from the center
def makeLineRoi( def x, def y, def angle, def start, def end, def thickness) {
    
    def p1x = start * Math.cos( angle ) + x
    p1y = start * Math.sin( angle ) + y

    p2x = end * Math.cos( angle ) + x
    p2y = end * Math.sin( angle ) + y
    
    def line = new Line(p1x, p1y, p2x, p2y)
    line.setStrokeWidth( thickness )
    return line
}

// Compute the FWHM from a gaussian fit
def getFWHM( def values ) {
    def inverted = values.collect{ 255 - it } as double[]
    def cf = new CurveFitter( values.getIndices().collect() as double[], inverted )
    cf.doFit(CurveFitter.GAUSSIAN)
    def p = cf.getParams()
    def d = p[3]
    def c = p[2]
    def fwhm = (2 * Math.sqrt( 2 * Math.log(2) ) ) * d;
    def r2 = cf.getRSquared()
    
    return [center:c, fwhm:fwhm, r2: r2]
}

// Required imports
import ij.IJ
import ij.ImagePlus
import ij.gui.Line
import ij.plugin.frame.RoiManager
import ij.plugin.filter.MaximumFinder 
import ij.plugin.filter.EDM
import ij.measure.CurveFitter

