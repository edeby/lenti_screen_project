/**
 * Harmonize channel names for running cellpose later
 * @author Olivier Burri, EPFL - SV - PTECH - PTBIOP
 * For Estelle Deby, UPKARTHAUS 
 * Date: 20240209
 */
 
setImageType('FLUORESCENCE')
if (getCurrentServer().nChannels() == 1) {
    setChannelNames( "Brightfield" )
    setChannelColors( getColorRGB( 255, 255, 255 ) )
} else {
    setChannelNames( "Brightfield", "RFP" )
    setChannelColors( getColorRGB( 255, 255, 255 ), getColorRGB( 255, 0, 0 ) )
}

